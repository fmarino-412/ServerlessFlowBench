exports.owHandler = function (params) {

    // test invocation and response timing

    // response creation and return
    return {
        body: {
            "test": "latency_test"
        }
    };
}