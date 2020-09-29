exports.lambda_handler = function (event, context, callback) {

    const result = {
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

    callback(null, result);
}