package loop_controller;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.BufferedWriter;
import java.io.IOException;

public class Handler implements HttpFunction {

	private static final Gson gson = new Gson();

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

		// set up response type
		httpResponse.setContentType("application/json");

		// search for list of string and counter in request
		JsonElement requestParsed = gson.fromJson(httpRequest.getReader(), JsonElement.class);
		JsonObject requestJson;

		if (requestParsed != null && requestParsed.isJsonObject()) {
			requestJson = requestParsed.getAsJsonObject();
		} else {
			returnResult(httpResponse.getWriter(), null, null);
			return;
		}

		JsonArray sentences;
		if (requestJson != null && requestJson.has("Sentences")) {
			sentences = requestJson.get("Sentences").getAsJsonArray();
		} else {
			returnResult(httpResponse.getWriter(), null, null);
			return;
		}

		int counter;
		if (requestJson.has("NextIterationCounter")) {
			counter = requestJson.get("NextIterationCounter").getAsInt();
		} else {
			returnResult(httpResponse.getWriter(), null, null);
			return;
		}

		// return response
		returnResult(httpResponse.getWriter(), sentences, counter);

	}

	private static void returnResult(@NotNull BufferedWriter outputWriter, JsonArray sentences, Integer counter)
			throws IOException {

		JsonObjectBuilder job = Json.createObjectBuilder();

		// response creation
		if (sentences == null || counter == null) {
			job.add("result", "Error");
		} else {

			// parse array
			JsonArrayBuilder array = Json.createArrayBuilder();
			sentences.forEach(jsonElement -> array.add(jsonElement.getAsString()));

			job.add("result", "Ok");
			job.add("Sentences", array);
			job.add("CurrentSentence", sentences.get(counter).getAsString());
			job.add("NextIterationCounter", counter + 1);
			job.add("EndNext", sentences.size() == counter + 1);
		}

		// response writing
		outputWriter.write(job.build().toString());
	}
}
