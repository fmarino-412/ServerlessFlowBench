// noinspection JSUnresolvedVariable
exports.owHandler = function (params) {

    let n;

    // search for array dimension in request
    if (params.hasOwnProperty("n")) {
        n = params.n;
    } else {
        n = 1100000;
    }

    // check value
    if (n <= 0) {
        n = 1100000;
    }

    // measure computation
    let startTime = Date.now();
    memoryStress(n);
    let endTime = Date.now();
    let executionTime = (endTime - startTime);

    // response creation and return
    return {
        body: {
            "test": "memory_test",
            "dimension": n,
            "milliseconds": executionTime
        }
    };
}

function memoryStress(n) {
    // dynamically append elements to a list
    // noinspection JSMismatchedCollectionQueryUpdate
    let memoryList = [];
    for (let i = 0; i < n; i++) {
        memoryList.push(i);
    }
}