exports.gcFunctionsHandler = function (req, res) {

    let sentence;

    // search for sentence in request
    if (req.query && req.query.hasOwnProperty("sentence")) {
        sentence = req.query.sentence;
    } else if (req.body && req.body.hasOwnProperty("sentence")) {
        sentence = req.body["sentence"];
    } else {
        res.send({
            result: "Error"
        });
        return;
    }

    detectDominantLanguage(sentence, res);
}

function detectDominantLanguage(text, res) {

    const {Translate} = require('@google-cloud/translate').v2;
    const client = new Translate();

    // prepare and return result
    client.detect(text).then((result) => {

        result = JSON.parse(JSON.stringify(result));

        const ret = {
            result: "Ok",
            sentence: text,
            language: result[0].language
        };

        res.send(ret);
    }).catch(() => {
        res.send({
            result: "Error"
        });
    });
}