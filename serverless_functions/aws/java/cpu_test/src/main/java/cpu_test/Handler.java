package cpu_test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Handler implements RequestStreamHandler {

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {

		// request reading
		HashMap event;
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			event = gson.fromJson(reader, HashMap.class);
		} catch (JsonSyntaxException ignored) {
			event = new HashMap();
		}
		long n;
		if (event.containsKey("n")) {
			n = Long.parseLong((String)event.get("n"));
		} else {
			n = 71950288374236L;
		}

		// computation
		long startTime = System.currentTimeMillis();
		String result = factorize(n);
		long executionTime = System.currentTimeMillis() - startTime;

		// response creation
		JsonObjectBuilder job1 = Json.createObjectBuilder();
		job1.add("isBase64Encoded", false);
		job1.add("statusCode", 200);

		JsonObjectBuilder job2 = Json.createObjectBuilder();
		JsonObjectBuilder job3 = Json.createObjectBuilder();
		JsonObjectBuilder job4 = Json.createObjectBuilder();

		job4.add("test", "cpu_test");
		job4.add("number", n);
		job4.add("result", result);
		job4.add("milliseconds", executionTime);
		job3.add("success", true);
		job3.add("payload", job4.build());
		job2.add("Content-Type", "application/json");
		job1.add("headers", job2.build());
		job1.add("body", job3.build().toString());

		// response writing
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream,
				StandardCharsets.UTF_8)));
		writer.write(job1.build().toString());
		writer.close();
	}

	private static String factorize(long n) {
		// finds factors for n
		List<Long> factors = new ArrayList<>();
		for (long i = 1; i < Math.floor(Math.sqrt(n) + 1); i++) {
			if (n % i == 0) {
				factors.add(i);
				if (n / i != i) {
					factors.add(n / i);
				}
			}
		}
		Collections.sort(factors);
		return factors.stream().map(Object::toString).collect(Collectors.joining(", "));
	}
}
