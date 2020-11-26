package cpu_test;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Handler {

	public static JsonObject main(JsonObject args) {

		// request reading, extract number to factorize if in request
		long n;
		if (args.has("n")) {
			n = args.get("n").getAsLong();
		} else {
			n = 71950288374236L;
		}
		// check value
		if (n <= 0) {
			n = 71950288374236L;
		}

		// computation
		long startTime = System.currentTimeMillis();
		String result = factorize(n);
		long executionTime = System.currentTimeMillis() - startTime;

		// response creation
		JsonObject body = new JsonObject();
		body.addProperty("test", "cpu_test");
		body.addProperty("number", n);
		body.addProperty("result", result);
		body.addProperty("milliseconds", executionTime);

		JsonObject response = new JsonObject();
		response.add("body", body);

		// return response
		return response;
	}

	private static String factorize(long n) {
		// finds factors for n
		List<Long> factors = new ArrayList<>();
		// optimized searching methods, just half of the cycles
		for (long i = 1; i < Math.floor(Math.sqrt(n) + 1); i++) {
			if (n % i == 0) {
				factors.add(i);
				if (n / i != i) {
					factors.add(n / i);
				}
			}
		}
		// sort list and returns as string
		Collections.sort(factors);
		return factors.stream().map(Object::toString).collect(Collectors.joining(", "));
	}
}
