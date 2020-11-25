exports.gcFunctionsHandler = function (req, res) {

    let sentence;
    let languageCode;

    // search for sentence and language code in request
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
    if (req.query && req.query.hasOwnProperty("language_code")) {
        languageCode = req.query.language_code;
    } else if (req.body && req.body.hasOwnProperty("language_code")) {
        languageCode = req.body["language_code"];
    } else {
        res.send({
            result: "Error"
        });
        return;
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
            result: "Ok",
            sentence: text,
            language: result[0]
        };

        res.send(ret);
    }).catch(() => {
        res.send({
            result: "Error"
        });
    });
}