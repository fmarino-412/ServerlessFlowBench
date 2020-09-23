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

import java.util.HashMap;

public class Handler implements RequestStreamHandler {

	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
	{
		// request reading
		HashMap event;
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		try {
			event = gson.fromJson(reader, HashMap.class);
		} catch (JsonSyntaxException ignored) {
			event = new HashMap();
		}

		int n;

		if (event.containsKey("n")) {
			n = (int) event.get("n");
		} else {
			//n = 71950288374236;
		}

		// response creation
		JsonObjectBuilder job1 = Json.createObjectBuilder();
		job1.add("isBase64Encoded", false);
		job1.add("statusCode", 200);

		JsonObjectBuilder job2 = Json.createObjectBuilder();
		JsonObjectBuilder job3 = Json.createObjectBuilder();
		JsonObjectBuilder job4 = Json.createObjectBuilder();

		job4.add("test", "latency_test function");
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
