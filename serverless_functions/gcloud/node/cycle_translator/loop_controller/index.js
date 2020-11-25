exports.gcFunctionsHandler = function (req, res) {

    let sentences;
    let counter;

    // search for list of string and counter in request
    if (req.query && req.query.hasOwnProperty("Sentences")) {
        sentences = req.query.Sentences;
    } else if (req.body && req.body.hasOwnProperty("Sentences")) {
        sentences = req.body["Sentences"];
    } else {
        res.send({
            result: "Error"
        });
        return;
    }
    if (req.query && req.query.hasOwnProperty("NextIterationCounter")) {
        counter = req.query.NextIterationCounter;
    } else if (req.body && req.body.hasOwnProperty("NextIterationCounter")) {
        counter = req.body["NextIterationCounter"];
    } else {
        res.send({
            result: "Error"
        });
        return;
    }

    // prepare and return response
    const ret = {
        result: "Ok",
        Sentences: sentences,
        CurrentSentence: sentences[counter],
        NextIterationCounter: counter + 1,
        EndNext: sentences.length === counter + 1
    };
    res.send(ret);
}