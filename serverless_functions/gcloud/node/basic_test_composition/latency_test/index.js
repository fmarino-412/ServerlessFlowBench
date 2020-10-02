exports.gcFunctionsHandler = function (req, res) {

    // test invocation and response timing

    // response creation
    res.set("Content-Type", "application/json");
    res.status(200);
    res.send(JSON.stringify({
        success: true,
        payload: {
            "test": "latency_test"
        }
    }));

}