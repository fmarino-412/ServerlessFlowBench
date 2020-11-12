package latency_test;

import com.google.gson.JsonObject;

public class Handler {

	public static JsonObject main(JsonObject args) {

		// test invocation and response timing

		// response creation
		JsonObject body = new JsonObject();
		body.addProperty("test", "latency_test");
		JsonObject response = new JsonObject();
		response.add("body", body);

		// return response
		return response;
	}
}
