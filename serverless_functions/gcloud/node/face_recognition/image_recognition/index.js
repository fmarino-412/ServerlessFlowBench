exports.gcFunctionsHandler = function (req, res) {

    let url;

    // search for image url in request
    if (req.query && req.query.url) {
        url = req.query.url;
    } else if (req.body && req.body["url"]) {
        url = req.body["url"];
    } else {
        res.send("Error");
        return;
    }

    // download image and perform analysis
    const request = require('request').defaults({ encoding: null });
    request.get(url, function (error, response, body) {
        if (!error && response.statusCode === 200) {
            const image = Buffer.from(body).toString('base64');
            detectObjectsAndScenes(image, url, res);
        } else {
            res.send("Error");
        }
    });

}

function retResult(result, url, res) {
    // prepare and return result
    if (result.includes('face')) {
        result = "face";
    } else {
        result = "other";
    }
    const ret = {
        'result': result,
        'image': url
    };
    res.send(ret);
}

function detectObjectsAndScenes(image, url, res) {

    const vision = require('@google-cloud/vision');
    const client = new vision.ImageAnnotatorClient();

    // prepare request
    const request = {
        "image": {
            "content": image
        },
        "features": [
            {
                "type": "LABEL_DETECTION"
            }
        ],
    };

    // submit request and analyze results
    client.annotateImage(request).then((result) => {

        result = JSON.parse(JSON.stringify(result));

        let string = "";
        result.forEach((element) => {
            element.labelAnnotations.forEach((label) => {
                string = string + label.description.toLowerCase() + ", "
            });
        });


        retResult(string.slice(0, string.length - 2), url, res);

    }).catch(() => {res.send("Error");});
}