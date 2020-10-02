exports.lambdaHandler = function (event, context, callback) {

    let url;

    // search for image url in request
    if (event.queryStringParameters && event.queryStringParameters.url) {
        url = event.queryStringParameters.url;
    } else if (event.url) {
        url = event.url;
    } else {
        callback(null, "Error");
    }

    // download image and perform analysis
    const request = require('request').defaults({ encoding: null });
    request.get(url, function (error, response, body) {
        if (!error && response.statusCode === 200) {
            const image = Buffer.from(body).toString('base64');
            detectObjectsAndScenes(image, url, callback);
        } else {
            callback(null, "Error");
        }
    });
}

function retResult(result, url, callback) {
    // prepare and return result
    const ret = {
        'result': result,
        'image': url
    };
    callback(null, ret);
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
    let blob = new Blob([ab], {
        type: "image/jpeg"
    });

    return ab;
}

function detectObjectsAndScenes(image, url, callback) {

    const AWS = require('aws-sdk');

    // prepare request
    image = getBinary(image);

    const client = new AWS.Rekognition();
    const params = {
        Image: {
            Bytes: image
        },
        MaxLabels: 100,
        MinConfidence: 70.0
    };

    // submit request and analyze results
    client.detectLabels(params, function (err, response) {
        if (err) {
            callback(null, "Error: " + err);
        } else {
            let result = "";
            response.Labels.forEach(label => {
                result = result + label.Name.toLowerCase() + ", "
            })
            retResult(result.slice(0, result.length - 2), url, callback);
        }
    });
}