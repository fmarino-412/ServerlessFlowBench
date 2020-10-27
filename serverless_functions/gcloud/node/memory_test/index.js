exports.gcFunctionsHandler = function (req, res) {

    let n;

    // search for array dimension in request
    if (req.query && req.query.hasOwnProperty("n")) {
        n = req.query.n;
    } else if (req.body && req.body.hasOwnProperty("n")) {
        n = req.body["n"];
    } else {
        n = 1300000;
    }

    // check value
    if (n <= 0) {
        n = 1300000;
    }

    // measure computation
    let startTime = Date.now();
    memoryStress(n);
    let endTime = Date.now();
    let executionTime = (endTime - startTime);

    // response creation
    res.send({
        success: true,
        payload: {
            "test": "memory_test",
            "dimension": n,
            "milliseconds": executionTime
        }
    });
}

function memoryStress(n) {
    // dynamically append elements to a list
    let memoryList = [];
    for (let i = 0; i < n; i++) {
        memoryList.push(i);
    }
}