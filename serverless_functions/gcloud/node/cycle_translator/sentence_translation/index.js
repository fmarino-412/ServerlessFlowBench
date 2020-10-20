exports.gcFunctionsHandler = function (req, res) {

    let sentence;
    let languageCode;

    // search for sentence and language code in request
    if (req.query && req.query.sentence) {
        sentence = req.query.sentence;
    } else if (req.sentence) {
        sentence = req.sentence;
    } else {
        res.send(JSON.stringify({
            "result": "Error"
        }));
    }
    if (req.query && req.query.language_code) {
        languageCode = req.query.language_code;
    } else if (req.language_code) {
        languageCode = req.language_code;
    } else {
        res.send(JSON.stringify({
            "result": "Error"
        }));
    }

    translateText(sentence, languageCode, res);
}

function translateText(text, sourceLanguageCode, res) {

    const {Translate} = require('@google-cloud/translate').v2;
    const client = new Translate();

    // prepare and return result
    const options = {
        from: sourceLanguageCode,
        to: 'en',
    };
    client.translate(text, options).then((result) => {

        result = JSON.parse(JSON.stringify(result));

        const ret = {
            "result": "Ok",
            "sentence": text,
            "language": result[0]
        };

        res.send(JSON.stringify(ret));
    }).catch((err) => {
        res.send(JSON.stringify({
            "result": "Error: " + err
        }));
    });
}