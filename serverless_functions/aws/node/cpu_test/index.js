exports.lambda_handler = function (event, context, callback) {

    let n;

    if (event.queryStringParameters && event.queryStringParameters.n) {
        n = event.queryStringParameters.n;
    } else {
        n = 71950288374236;
    }

    let start_time = Date.now();
    let result = factorize(n);
    let end_time = Date.now();
    let execution_time = (end_time - start_time);

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
                'milliseconds': execution_time
            }
        })
    };
    callback(null, ret);
}

function factorize(n) {
    // finds factors for n
    let factors = [];
    for (let i = 1; i < Math.floor(Math.sqrt(n)) + 1; i++) {
        if (n % i === 0) {
            factors.push(i);
            if (n / i !== i) {
                factors.push(n / i);
            }
        }
    }

    factors.sort(function(n1, n2){return n1 - n2;});

    return factors;
}