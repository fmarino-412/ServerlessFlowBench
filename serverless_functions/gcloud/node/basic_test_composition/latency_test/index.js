exports.gcFunctionsHandler = function (req, res) {

    // test invocation and response timing

    // response creation
    res.send({
        success: true,
        payload: {
            "test": "latency_test"
        }
    });

}