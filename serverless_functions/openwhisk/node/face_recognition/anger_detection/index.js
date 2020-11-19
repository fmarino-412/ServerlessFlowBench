exports.owHandler = function (params) {

    let url;

    // search for image url in request
    if (params.hasOwnProperty("body") && params.body.hasOwnProperty("url")) {
        url = params.body.url;
    } else {
        throw new Error("Missing argument in image recognition");
    }

    // execute request and perform image analysis
    return new Promise(function (resolve, reject) {
        detectAnger(url, function (response) {
            resolve({
                value: response
            });
        });
    });
}

function detectAnger(image, callback) {

    const azure = require('./azureconfig');
    const axios = require('axios').default;

    // prepare and perform request
    axios({
        method: 'post',
        url: azure.endpoint + '/face/v1.0/detect',
        params: {
            detectionModel: "detection_01",
            returnFaceAttributes: "emotion",
            returnFaceId: false
        },
        data: {
            url: image
        },
        headers: {'Ocp-Apim-Subscription-Key': azure.key}
    }).then((response) => {
        response = response.data;
        for (let i = 0; i < response.length; i++) {
            let emotions = response[i].faceAttributes.emotion;
            if (emotions.hasOwnProperty('anger') && emotions.anger >= 0.6) {
                return callback(true);
            }
        }
        return callback(false);
    }).catch((error) => {
        throw new Error(error);
    });
}