exports.lambdaHandler = function (event, context, callback) {

    let sentence;

    // search for sentence in request
    // noinspection JSUnresolvedVariable
    if (event.queryStringParameters && event.queryStringParameters.hasOwnProperty('sentence')) {
        sentence = event.queryStringParameters.sentence;
    } else if (event.hasOwnProperty('sentence')) {
        sentence = event.sentence;
    } else {
        callback(null, "Error");
    }

    detectDominantLanguage(sentence, callback);
}

function detectDominantLanguage(text, callback) {

    // prepare request
    const AWS = require('aws-sdk');
    const client = new AWS.Comprehend();

    const request = {
        Text: text
    };

    // perform request
    client.detectDominantLanguage(request, function (err, response) {
        if (err) {
            callback(null, "Error");
        } else {
            // analyze result
            let maxLanguage = "";
            let maxScore = 0;
            response.Languages.forEach(language => {
                if (language.Score > maxScore) {
                    maxScore = language.Score;
                    maxLanguage = language.LanguageCode;
                }
            });
            // return result
            const ret = {
                sentence: text,
                language: maxLanguage
            };
            callback(null, ret);
        }
    });
}