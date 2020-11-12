package memory_test;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Handler {

	public static JsonObject main(JsonObject args) {

		// request reading, search for array dimension in request
		long n;
		if (args.has("n")) {
			n = args.get("n").getAsLong();
		} else {
			n = 1300000;
		}
		// check value
		if (n <= 0) {
			n = 1300000;
		}

		// computation
		long startTime = System.currentTimeMillis();
		memoryStress(n);
		long executionTime = System.currentTimeMillis() - startTime;

		// response creation
		JsonObject body = new JsonObject();
		body.addProperty("test", "memory_test");
		body.addProperty("dimension", n);
		body.addProperty("milliseconds", executionTime);

		JsonObject response = new JsonObject();
		response.add("body", body);

		// return response
		return response;
	}

	private static void memoryStress(long n) {
		// dynamic append of elements to an array
		List<Long> memoryList = new ArrayList<>();
		for (long i = 0; i < n; i++) {
			memoryList.add(i);
		}
	}
}
