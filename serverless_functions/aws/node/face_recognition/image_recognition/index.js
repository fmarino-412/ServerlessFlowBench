exports.lambda_handler = function (event, context, callback) {

    let url;

    if (event.queryStringParameters && event.queryStringParameters.url) {
        url = event.queryStringParameters.url;
    } else {
        callback(null, "Error");
    }

    const request = require('request').defaults({ encoding: null });
    let image;
    request.get(url, function (error, response, body) {
        if (!error && response.statusCode === 200) {
            image = Buffer.from(body).toString('base64');
        } else {
            callback(null, "Error");
        }
    });
    // image is null!

    let result = detect_objects_and_scenes(getBinary(image));
    if (result === null) {
        callback(null, "Error");
    } else {
        const ret = {
            'result': result,
            'image': url
        };
        callback(null, ret);
    }
}

function getBinary(base64Image) {
    const binaryImg = atob(base64Image);
    const length = binaryImg.length;
    const ab = new ArrayBuffer(length);
    const ua = new Uint8Array(ab);
    for (let i = 0; i < length; i++) {
        ua[i] = binaryImg.charCodeAt(i);
    }
    return ab;
}

function detect_objects_and_scenes(image) {

    const AWS = require('aws-sdk');
    /*const uuid = require('node-uuid');

    const config = new AWS.Config({
        accessKeyId: process.env.AWS_ACCESS_KEY_ID,
        secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY,
        region: process.env.AWS_REGION
    });*/

    const client = new AWS.Rekognition();
    const params = {
        Image: {
            Bytes: image
        },
        MaxLabels: 100,
        MinConfidence: 70.0
    };

    let result = "";
    client.detectLabels(params, function (err, response) {
        if (err) {
            return null;
        } else {
            response.Labels.forEach(label => {
                result = result + label.Name.toLowerCase() + ", "
            })
        }
    });

    return result.substr(0, -2);
}