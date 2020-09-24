package latency_test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Handler implements RequestStreamHandler {

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
	{
		// response creation
		JsonObjectBuilder job1 = Json.createObjectBuilder();
		job1.add("isBase64Encoded", false);
		job1.add("statusCode", 200);

		JsonObjectBuilder job2 = Json.createObjectBuilder();
		JsonObjectBuilder job3 = Json.createObjectBuilder();
		JsonObjectBuilder job4 = Json.createObjectBuilder();

		job4.add("test", "latency_test");
		job3.add("success", true);
		job3.add("payload", job4.build().toString());
		job2.add("Content-Type", "application/json");
		job1.add("headers", job2.build());
		job1.add("body", job3.build().toString());

		// response writing
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream,
				StandardCharsets.UTF_8)));
		writer.write(job1.build().toString());
		writer.close();
	}
}