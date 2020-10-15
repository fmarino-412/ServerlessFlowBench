exports.lambdaHandler = function (event, context, callback) {

    let sentence;
    let rankingTableName;

    // search for string and table in request
    if (event.queryStringParameters && event.queryStringParameters.sentence) {
        sentence = event.queryStringParameters.sentence;
    } else if (event.sentence) {
        sentence = event.sentence;
    } else {
        callback(null, "Error");
    }
    if (event.queryStringParameters && event.queryStringParameters.ranking_table_name) {
        rankingTableName = event.queryStringParameters.ranking_table_name;
    } else if (event.ranking_table_name) {
        rankingTableName = event.ranking_table_name;
    } else {
        callback(null, "Error");
    }

    // create dynamo client
    const AWS = require('aws-sdk');
    AWS.config.update({
        region: process.env.AWS_REGION
    });
    const dynamoDB = new AWS.DynamoDB.DocumentClient();

    // isolate words
    const regExp = new RegExp("[a-zA-Z]+", 'g');
    sentence.match(regExp).forEach(word => rankWord(word.toLowerCase(), rankingTableName, dynamoDB, callback));
    callback(null, "Updated");
}

function rankWord(word, tableName, client, callback) {

    // prepare request
    const request = {
        TableName: tableName,
        Key: {
            "word": word
        },
        UpdateExpression: "ADD word_counter :word_counter",
        ExpressionAttributeValues: {
            ":word_counter": 1
        },
        ReturnValues: "NONE"
    };
    // perform request
    client.update(request, function (err, response) {
        if (err) {
            callback(null, "Error");
        }
    });
}