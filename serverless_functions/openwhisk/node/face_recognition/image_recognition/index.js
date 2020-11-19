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
        detectObjectsAndScenes(url, function (response) {
            resolve({
                value: response,
                body: {
                    "url": url
                }
            });
        });
    });
}

function detectObjectsAndScenes(image, callback) {

    const ComputerVisionClient = require('@azure/cognitiveservices-computervision').ComputerVisionClient;
    const ApiKeyCredentials = require('@azure/ms-rest-js').ApiKeyCredentials;
    const azure = require('./azureconfig');

    // prepare and perform request
    const client = new ComputerVisionClient(new ApiKeyCredentials(
        { inHeader: { 'Ocp-Apim-Subscription-Key': azure.key } }), azure.endpoint);
    client.analyzeImage(image,  { visualFeatures: ['Tags'] }).then((result) => {
        // analyze result
        let tags = result.tags;
        let resultString = tags.map(tag => (`${tag.name}`)).join(', ');
        let value = resultString.includes("person");

        // response creation and return
        return callback(value);
    }).catch((err) => {throw new Error(err)});
}