exports.lambdaHandler = function (event, context, callback) {

    let n;

    if (event.queryStringParameters && event.queryStringParameters.n) {
        n = event.queryStringParameters.n;
    } else {
        n = 1300000;
    }

    let startTime = Date.now();
    memoryStress(n);
    let endTime = Date.now();
    let executionTime = (endTime - startTime);

    const ret = {
        statusCode: 200,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            success: true,
            payload: {
                'test': 'memory_test',
                'dimension': n,
                'milliseconds': executionTime
            }
        })
    };
    callback(null, ret);
}

function memoryStress(n) {
    let memoryList = [];
    for (let i = 0; i < n; i++) {
        memoryList.push(i);
    }
}