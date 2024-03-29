exports.lambdaHandler = function (event, context, callback) {

    let originalSentence;
    let originalLanguageCode;
    let translatedSentence;
    let loggingBucketName;

    // search for strings, original language code and logging bucket in request
    // noinspection JSUnresolvedVariable
    if (event.queryStringParameters && event.queryStringParameters.hasOwnProperty('original_sentence')) {
        originalSentence = event.queryStringParameters.original_sentence;
    } else if (event.hasOwnProperty('original_sentence')) {
        originalSentence = event.original_sentence;
    } else {
        callback(null, "Error");
    }
    // noinspection JSUnresolvedVariable
    if (event.queryStringParameters && event.queryStringParameters.hasOwnProperty('original_language_code')) {
        originalLanguageCode = event.queryStringParameters.original_language_code;
    } else if (event.hasOwnProperty('original_language_code')) {
        originalLanguageCode = event.original_language_code;
    } else {
        callback(null, "Error");
    }
    // noinspection JSUnresolvedVariable
    if (event.queryStringParameters && event.queryStringParameters.hasOwnProperty('translated_sentence')) {
        translatedSentence = event.queryStringParameters.translated_sentence;
    } else if (event.hasOwnProperty('translated_sentence')) {
        translatedSentence = event.translated_sentence;
    } else {
        callback(null, "Error");
    }
    // noinspection JSUnresolvedVariable
    if (event.queryStringParameters && event.queryStringParameters.hasOwnProperty('logging_bucket_name')) {
        loggingBucketName = event.queryStringParameters.logging_bucket_name;
    } else if (event.hasOwnProperty('logging_bucket_name')) {
        loggingBucketName = event.logging_bucket_name;
    } else {
        callback(null, "Error");
    }

    logTranslation(originalSentence, originalLanguageCode, translatedSentence, "en",
        loggingBucketName, callback);
    callback(null, "Logged");
}

function logTranslation(originalSentence, originalLanguageCode, translatedSentence, destinationLanguageCode,
                        loggingBucketName, callback) {

    // timestamp
    const now = new Date();
    let date = "" + now.getFullYear() + "-" + checkZero(now.getMonth()) + "-" + checkZero(now.getDay());
    let time = "" + checkZero(now.getHours()) + ":" + checkZero(now.getMinutes()) + ":" + checkZero(now.getSeconds()) +
        "." + now.getMilliseconds();

    // create filename
    let filename = "Translation_" + date + "_" + time + makeId() + ".log";

    // create body
    let body = "Translation info:" + "\n\n" + "original sentence: " + originalSentence + "\n" +
        "original language: " + originalLanguageCode + "\n" + "translated sentence: " + translatedSentence +
        "\n" + "destination language: " + destinationLanguageCode + "\n" + "log date: " +
        date + "\n" + "log time: " + time;

    // prepare request and perform insertion
    const AWS = require('aws-sdk');
    let S3 = new AWS.S3({region: process.env.AWS_REGION});
    const object = {
        Bucket: loggingBucketName,
        Key: filename,
        Body: body,
        ContentType: 'text/plain'
    };
    S3.putObject(object, function(err, _) {
        if (err) {
            callback(null, "Error");
        }
    });
}

function checkZero(data) {
    if(data.toString().length === 1){
        data = "0" + data.toString();
    }
    return data;
}

function makeId() {
    let result = '';
    // noinspection SpellCheckingInspection
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    const charactersLength = characters.length;
    for (let i = 0; i < 8; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return "[NodeRuntime_" + result + "]";
}