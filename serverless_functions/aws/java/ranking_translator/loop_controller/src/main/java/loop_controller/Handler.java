package loop_controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings({"rawtypes", "unchecked"})
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
		// extract sentences to recognize from request
		ArrayList<String> sentences;
		if (event.containsKey("Sentences")) {
			sentences = (ArrayList<String>)event.get("Sentences");
		} else {
			returnResult(outputStream, null, null);
			return;
		}
		// extract counter from request
		int counter;
		if (event.containsKey("NextIterationCounter")) {
			counter = (int)Math.round((double)event.get("NextIterationCounter"));
		} else {
			returnResult(outputStream, null, null);
			return;
		}

		// return response
		returnResult(outputStream, sentences, counter);
	}

	private static void returnResult(@NotNull OutputStream outputStream, ArrayList<String> sentences, Integer counter) {

		// response creation
		String result;
		if (sentences == null || counter == null) {
			result = "Error";
		} else {
			JsonArrayBuilder array = Json.createArrayBuilder();
			sentences.forEach(array::add);

			JsonObjectBuilder job = Json.createObjectBuilder();
			job.add("Sentences", array);
			job.add("CurrentSentence", sentences.get(counter));
			job.add("NextIterationCounter", counter + 1);
			job.add("EndNext", sentences.size() == counter + 1);
			result = job.build().toString();
		}

		// response writing
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream,
				StandardCharsets.UTF_8)));
		writer.write(result);
		writer.close();
	}
}
