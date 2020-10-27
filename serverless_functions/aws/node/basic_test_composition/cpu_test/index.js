exports.lambdaHandler = function (event, context, callback) {

    let n;

    // search for number to factorize in request
    if (event.queryStringParameters && event.queryStringParameters.n) {
        n = event.queryStringParameters.n;
    } else if (event.hasOwnProperty('n')) {
        n = event.n;
    } else {
        n = 71950288374236;
    }
    // check value
    if (n <= 0) {
        n = 71950288374236;
    }

    // measure execution
    let startTime = Date.now();
    let result = factorize(n);
    let endTime = Date.now();
    let executionTime = (endTime - startTime);

    // create response
    const ret = {
        statusCode: 200,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            success: true,
            payload: {
                'test': 'cpu_test',
                'number': n,
                'result': result,
                'milliseconds': executionTime
            }
        })
    };
    callback(null, ret);
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