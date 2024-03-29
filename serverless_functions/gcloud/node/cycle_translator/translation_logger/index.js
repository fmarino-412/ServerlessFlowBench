exports.gcFunctionsHandler = function (req, res) {

    let originalSentence;
    let originalLanguageCode;
    let translatedSentence;
    let loggingBucketName;

    // search for strings, original language code and logging bucket in request
    if (req.query && req.query.hasOwnProperty("original_sentence")) {
        originalSentence = req.query.original_sentence;
    } else if (req.body && req.body.hasOwnProperty("original_sentence")) {
        originalSentence = req.body["original_sentence"];
    } else {
        res.send("Error");
        return;
    }
    if (req.query && req.query.hasOwnProperty("original_language_code")) {
        originalLanguageCode = req.query.original_language_code;
    } else if (req.body && req.body.hasOwnProperty("original_language_code")) {
        originalLanguageCode = req.body["original_language_code"];
    } else {
        res.send("Error");
        return;
    }
    if (req.query && req.query.hasOwnProperty("translated_sentence")) {
        translatedSentence = req.query.translated_sentence;
    } else if (req.body && req.body.hasOwnProperty("translated_sentence")) {
        translatedSentence = req.body["translated_sentence"];
    } else {
        res.send("Error");
        return;
    }
    if (req.query && req.query.hasOwnProperty("logging_bucket_name")) {
        loggingBucketName = req.query.logging_bucket_name;
    } else if (req.body && req.body.hasOwnProperty("logging_bucket_name")) {
        loggingBucketName = req.body["logging_bucket_name"];
    } else {
        res.send("Error");
        return;
    }

    logTranslation(originalSentence, originalLanguageCode, translatedSentence, "en",
        loggingBucketName, res);
}

function logTranslation(originalSentence, originalLanguageCode, translatedSentence, destinationLanguageCode,
                        loggingBucketName, res) {

    // timestamp
    const now = new Date();
    let date = "" + now.getFullYear() + "-" + checkZero(now.getMonth()) + "-" + checkZero(now.getDay());
    let time = "" + checkZero(now.getHours()) + ":" + checkZero(now.getMinutes()) + ":" + checkZero(now.getSeconds()) + "." + now.getMilliseconds();

    // create filename
    let filename = "Translation_" + date + "_" + time + makeId() + ".log";

    // create body
    let body = "Translation info:" + "\n\n" + "original sentence: " + originalSentence + "\n" +
        "original language: " + originalLanguageCode + "\n" + "translated sentence: " + translatedSentence +
        "\n" + "destination language: " + destinationLanguageCode + "\n" + "log date: " +
        date + "\n" + "log time: " + time;

    // prepare body
    const stream = require('stream');
    let bufferStream = new stream.PassThrough();
    bufferStream.end(body);

    // connect Google Cloud Storage
    const {Storage} = require("@google-cloud/storage");
    const cloudStorage = new Storage();
    let file = cloudStorage.bucket(loggingBucketName).file(filename);
    bufferStream.pipe(file.createWriteStream({
        metadata: {
            contentType: 'text/plain',
            metadata: {
                custom: 'metadata'
            }
        },
        public: false
    }))
    .on('error', () => {res.send("Error");})
    .on('finish', () => {res.send("Logged");});
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