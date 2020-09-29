exports.lambda_handler = function (event, context, callback) {

    let n;

    if (event.queryStringParameters && event.queryStringParameters.n) {
        n = event.queryStringParameters.n;
    } else {
        n = 1300000;
    }

    let start_time = Date.now();
    memory_stress(n);
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
                'test': 'memory_test',
                'dimension': n,
                'milliseconds': execution_time
            }
        })
    };
    callback(null, ret);
}

function memory_stress(n) {
    let memory_list = [];
    for (let i = 0; i < n; i++) {
        memory_list.push(i);
    }
}