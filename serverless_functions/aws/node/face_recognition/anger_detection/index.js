exports.lambdaHandler = function (event, context, callback) {

    let url;

    if (event.queryStringParameters && event.queryStringParameters.url) {
        url = event.queryStringParameters.url;
    } else if (event.url) {
        url = event.url;
    } else {
        callback(null, "Error");
    }

    const request = require('request').defaults({ encoding: null });
    request.get(url, function (error, response, body) {
        if (!error && response.statusCode === 200) {
            const image = Buffer.from(body).toString('base64');
            detectAnger(image, callback);
        } else {
            callback(null, "Error");
        }
    });
}

// credits: Micheal Dennis @ Stack Overflow
// https://stackoverflow.com/questions/43494736/aws-rekognition-javascript-sdk-using-bytes
function getBinary(base64Image) {
    const atob = require('atob');
    const Blob = require('node-blob');
    const binaryImg = atob(base64Image);
    const length = binaryImg.length;
    const ab = new ArrayBuffer(length);
    const ua = new Uint8Array(ab);
    for (let i = 0; i < length; i++) {
        ua[i] = binaryImg.charCodeAt(i);
    }
    let blob = new Blob([ab], {
        type: "image/jpeg"
    });

    return ab;
}

function detectAnger(image, callback) {

    const AWS = require('aws-sdk');

    image = getBinary(image);

    const client = new AWS.Rekognition();
    const params = {
        Image: {
            Bytes: image
        },
        Attributes: ['ALL']
    };

    client.detectFaces(params, function (err, response) {
        if (err) {
            callback(null, "Error: " + err);
        } else {
            response.FaceDetails.forEach(faceDetail => {
                faceDetail.Emotions.forEach(emotion => {
                    if (emotion.Type === 'ANGRY' && emotion.Confidence >= 60) {
                        callback(null, true.toString());
                    }
                });
            });
            callback(null, false.toString());
        }
    });
}