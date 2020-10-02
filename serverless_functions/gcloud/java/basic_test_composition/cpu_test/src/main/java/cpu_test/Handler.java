package cpu_test;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Handler implements HttpFunction {

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

		// request reading
		long n = Long.parseLong(httpRequest.getFirstQueryParameter("n").orElse("71950288374236"));

		// computation
		long startTime = System.currentTimeMillis();
		String result = factorize(n);
		long executionTime = System.currentTimeMillis() - startTime;

		// response creation
		JsonObjectBuilder job1 = Json.createObjectBuilder();
		JsonObjectBuilder job2 = Json.createObjectBuilder();

		job2.add("test", "cpu_test");
		job2.add("number", n);
		job2.add("result", result);
		job2.add("milliseconds", executionTime);
		job1.add("success", true);
		job1.add("payload", job2.build());

		// response writing
		BufferedWriter writer = httpResponse.getWriter();
		writer.write(job1.build().toString());
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
