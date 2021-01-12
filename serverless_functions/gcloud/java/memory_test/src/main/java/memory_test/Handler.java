package memory_test;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

public class Handler implements HttpFunction {

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

		// set up response type
		httpResponse.setContentType("application/json");

		// request reading, search for array dimension in request
		long n = Long.parseLong(httpRequest.getFirstQueryParameter("n").orElse("1100000"));
		// check value
		if (n <= 0) {
			n = 1100000;
		}

		// computation
		long startTime = System.currentTimeMillis();
		memoryStress(n);
		long executionTime = System.currentTimeMillis() - startTime;

		// response creation
		JsonObjectBuilder job1 = Json.createObjectBuilder();
		JsonObjectBuilder job2 = Json.createObjectBuilder();

		job2.add("test", "memory_test");
		job2.add("dimension", n);
		job2.add("milliseconds", executionTime);
		job1.add("success", true);
		job1.add("payload", job2.build());

		// response writing
		BufferedWriter writer = httpResponse.getWriter();
		writer.write(job1.build().toString());
	}

	private static void memoryStress(long n) {
		// dynamic append of elements to an array
		// noinspection MismatchedQueryAndUpdateOfCollection
		List<Long> memoryList = new ArrayList<>();
		for (long i = 0; i < n; i++) {
			memoryList.add(i);
		}
	}
}
