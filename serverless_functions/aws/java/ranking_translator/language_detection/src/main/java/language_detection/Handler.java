package language_detection;

import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.DetectDominantLanguageRequest;
import com.amazonaws.services.comprehend.model.DetectDominantLanguageResult;
import com.amazonaws.services.comprehend.model.DominantLanguage;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Handler implements RequestStreamHandler {

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

		// request reading
		HashMap event;
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			event = gson.fromJson(reader, HashMap.class);
		} catch (JsonSyntaxException ignored) {
			event = new HashMap();
		}
		// extract string to recognize from request
		String sentence;
		if (event.containsKey("sentence")) {
			sentence = (String)event.get("sentence");
		} else {
			returnResult(outputStream, null, null);
			return;
		}

		// detect dominant language and return result
		returnResult(outputStream, sentence, detectDominantLanguage(sentence));
	}

	private static String detectDominantLanguage(String text) {

		// prepare request
		AmazonComprehend client = AmazonComprehendClientBuilder.defaultClient();

		DetectDominantLanguageRequest request = new DetectDominantLanguageRequest().withText(text);

		// perform request
		DetectDominantLanguageResult result = client.detectDominantLanguage(request);

		// analyze and return results
		String maxLanguage = null;
		float maxScore = 0;
		for (DominantLanguage language : result.getLanguages()) {
			if (language.getScore() > maxScore) {
				maxScore = language.getScore();
				maxLanguage = language.getLanguageCode().toLowerCase();
			}
		}

		return maxLanguage;
	}

	private static void returnResult(@NotNull OutputStream outputStream, String sentence, String language) {

		// response creation
		String result;
		if (sentence == null || language == null) {
			result = "Error";
		} else {
			JsonObjectBuilder job = Json.createObjectBuilder();
			job.add("sentence", sentence);
			job.add("language", language);
			result = job.build().toString();
		}

		// response writing
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream,
				StandardCharsets.UTF_8)));
		writer.write(result);
		writer.close();
	}
}
