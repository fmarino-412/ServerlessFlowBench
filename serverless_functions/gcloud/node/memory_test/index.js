exports.gcFunctionsHandler = function (req, res) {

    let n;

    if (req.query && req.query.n) {
        n = req.query.n;
    } else if (req.n) {
        n = req.n;
    } else {
        n = 1300000;
    }

    let startTime = Date.now();
    memoryStress(n);
    let endTime = Date.now();
    let executionTime = (endTime - startTime);

    res.set("Content-Type", "application/json");
    res.status(200);
    res.send(JSON.stringify({
        success: true,
        payload: {
            "test": "memory_test",
            "dimension": n,
            "milliseconds": executionTime
        }
    }));
}

function memoryStress(n) {
    let memoryList = [];
    for (let i = 0; i < n; i++) {
        memoryList.push(i);
    }
}