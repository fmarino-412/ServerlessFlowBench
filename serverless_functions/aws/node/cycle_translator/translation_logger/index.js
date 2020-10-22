exports.lambdaHandler = function (event, context, callback) {

    let originalSentence;
    let originalLanguageCode;
    let translatedSentence;
    let loggingBucketName;

    // search for strings, original language code and logging bucket in request
    if (event.queryStringParameters && event.queryStringParameters.original_sentence) {
        originalSentence = event.queryStringParameters.original_sentence;
    } else if (event.original_sentence) {
        originalSentence = event.original_sentence;
    } else {
        callback(null, "Error");
    }
    if (event.queryStringParameters && event.queryStringParameters.original_language_code) {
        originalLanguageCode = event.queryStringParameters.original_language_code;
    } else if (event.original_language_code) {
        originalLanguageCode = event.original_language_code;
    } else {
        callback(null, "Error");
    }
    if (event.queryStringParameters && event.queryStringParameters.translated_sentence) {
        translatedSentence = event.queryStringParameters.translated_sentence;
    } else if (event.translated_sentence) {
        translatedSentence = event.translated_sentence;
    } else {
        callback(null, "Error");
    }
    if (event.queryStringParameters && event.queryStringParameters.logging_bucket_name) {
        loggingBucketName = event.queryStringParameters.logging_bucket_name;
    } else if (event.logging_bucket_name) {
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
    let time = "" + checkZero(now.getHours()) + ":" + checkZero(now.getMinutes()) + ":" + checkZero(now.getSeconds()) + "." + now.getMilliseconds();

    // create key
    let key = "Translation_" + date + "_" + time + ".log";

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
        Key: key,
        Body: body,
        ContentType: 'text/plain'
    };
    S3.putObject(object, function(err, response) {
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