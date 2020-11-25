exports.lambdaHandler = function (event, context, callback) {

    let url;

    // search for image url in request
    // noinspection JSUnresolvedVariable
    if (event.queryStringParameters && event.queryStringParameters.hasOwnProperty('url')) {
        url = event.queryStringParameters.url;
    } else if (event.hasOwnProperty('url')) {
        url = event.url;
    } else {
        callback(null, "Error");
    }

    // download image and perform analysis
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
    // turn from base64 image representation to binary
    const atob = require('atob');
    const Blob = require('node-blob');
    const binaryImg = atob(base64Image);
    const length = binaryImg.length;
    const ab = new ArrayBuffer(length);
    const ua = new Uint8Array(ab);
    for (let i = 0; i < length; i++) {
        ua[i] = binaryImg.charCodeAt(i);
    }
    // noinspection JSUnusedLocalSymbols
    let blob = new Blob([ab], {
        type: "image/jpeg"
    });

    return ab;
}

function detectAnger(image, callback) {

    const AWS = require('aws-sdk');

    // prepare request
    image = getBinary(image);

    const client = new AWS.Rekognition();
    const params = {
        Image: {
            Bytes: image
        },
        Attributes: ['ALL']
    };

    // submit request and analyze results
    client.detectFaces(params, function (err, response) {
        if (err) {
            callback(null, "Error");
        } else {
            for (let i = 0; i < response.FaceDetails.length; i++) {
                for (let j = 0; j < response.FaceDetails[i].Emotions.length; j++) {
                    let emotion = response.FaceDetails[i].Emotions[j];
                    if (emotion.Type === 'ANGRY' && emotion.Confidence >= 60) {
                        callback(null, true.toString());
                        return;
                    }
                }
            }
            callback(null, false.toString());
        }
    });
}