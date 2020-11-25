exports.lambdaHandler = function (event, context, callback) {

    let sentences;
    let counter;

    // search for list of string and counter in request
    // noinspection JSUnresolvedVariable
    if (event.queryStringParameters && event.queryStringParameters.hasOwnProperty('Sentences')) {
        sentences = event.queryStringParameters.Sentences;
    } else if (event.hasOwnProperty('Sentences')) {
        sentences = event.Sentences;
    } else {
        callback(null, "Error");
    }
    // noinspection JSUnresolvedVariable
    if (event.queryStringParameters && event.queryStringParameters.hasOwnProperty('NextIterationCounter')) {
        counter = event.queryStringParameters.NextIterationCounter;
    } else if (event.hasOwnProperty('NextIterationCounter')) {
        counter = event.NextIterationCounter;
    } else {
        callback(null, "Error");
    }

    // prepare and return response
    const ret = {
        Sentences: sentences,
        CurrentSentence: sentences[counter],
        NextIterationCounter: counter + 1,
        EndNext: sentences.length === counter + 1
    };
    callback(null, ret);
}