package latency_test;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.BufferedWriter;

public class Handler implements HttpFunction {

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

		// test invocation and response timing

		// response creation
		JsonObjectBuilder job1 = Json.createObjectBuilder();
		JsonObjectBuilder job2 = Json.createObjectBuilder();

		job2.add("test", "latency_test");
		job1.add("success", true);
		job1.add("payload", job2.build());

		// response writing
		BufferedWriter writer = httpResponse.getWriter();
		writer.write(job1.build().toString());
	}
}
