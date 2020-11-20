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
            detectAnger(image, url, res);
        } else {
            res.send("Error");
        }
    });

}

function detectAnger(image, url, res) {

    const vision = require('@google-cloud/vision');
    const client = new vision.ImageAnnotatorClient();

    // prepare request
    const request = {
        "image": {
            "content": image
        },
        "features": [
            {
                "type": "FACE_DETECTION"
            }
        ],
    };

    // submit request and analyze results
    client.annotateImage(request).then((result) => {

        result = JSON.parse(JSON.stringify(result));
        for (let i = 0; i < result.length; i++) {
            for (let j = 0; j < result[i].faceAnnotations.length; j++) {
                let face = result[i].faceAnnotations[j];
                if (face.angerLikelihood === "VERY_LIKELY" || face.angerLikelihood === "LIKELY") {
                    res.send("True");
                    return;
                }
            }
        }
        res.send("False");

    }).catch(() => {res.send("Error");});
}