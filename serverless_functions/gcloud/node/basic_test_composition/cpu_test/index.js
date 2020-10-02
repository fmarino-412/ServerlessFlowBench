exports.gcFunctionsHandler = function (req, res) {

    let n;

    // search for number to factorize in request
    if (req.query && req.query.n) {
        n = req.query.n;
    } else if (req.n) {
        n = req.n;
    } else {
        n = 71950288374236;
    }

    // measure execution
    let startTime = Date.now();
    let result = factorize(n);
    let endTime = Date.now();
    let executionTime = (endTime - startTime);

    // create response
    res.set("Content-Type", "application/json");
    res.status(200);
    res.send(JSON.stringify({
        success: true,
        payload: {
            "test": "cpu_test",
            "number": n,
            "result": result,
            "milliseconds": executionTime
        }
    }));

}

function factorize(n) {
    // finds factors for n
    let factors = [];
    // optimized research
    for (let i = 1; i < Math.floor(Math.sqrt(n)) + 1; i++) {
        if (n % i === 0) {
            factors.push(i);
            if (n / i !== i) {
                factors.push(n / i);
            }
        }
    }

    // sort result and return
    factors.sort(function(n1, n2){return n1 - n2;});
    return factors;
}