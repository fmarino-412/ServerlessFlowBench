exports.lambdaHandler = function (event, context, callback) {

    // test invocation and response timing

    // response creation
    const ret = {
        statusCode: 200,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            success: true,
            payload: {
                'test': 'latency_test'
            }
        })
    };

    callback(null, ret);
}