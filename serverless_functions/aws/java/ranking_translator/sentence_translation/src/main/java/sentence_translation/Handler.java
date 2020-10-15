package sentence_translation;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@SuppressWarnings("rawtypes")
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
		String languageCode;
		if (event.containsKey("language_code")) {
			languageCode = (String)event.get("language_code");
		} else {
			returnResult(outputStream, null, null);
			return;
		}

		// translate string and return result
		returnResult(outputStream, sentence, translateText(sentence, languageCode));
	}

	private static String translateText(String text, String languageCode) {

		// prepare request
		AmazonTranslate client = AmazonTranslateClient.builder()
				.withCredentials(new AWSStaticCredentialsProvider(
						DefaultAWSCredentialsProviderChain.getInstance().getCredentials()))
				.withRegion(System.getenv("AWS_REGION"))
				.build();

		TranslateTextRequest request = new TranslateTextRequest()
				.withText(text)
				.withSourceLanguageCode(languageCode)
				.withTargetLanguageCode("en");

		// return result
		return client.translateText(request).getTranslatedText();
	}

	private static void returnResult(@NotNull OutputStream outputStream, String originalSentence, String sentence) {

		// response creation
		String result;
		if (originalSentence == null || sentence == null) {
			result = "Error";
		} else {
			JsonObjectBuilder job = Json.createObjectBuilder();
			job.add("original_sentence", originalSentence);
			job.add("sentence", sentence);
			result = job.build().toString();
		}

		// response writing
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream,
				StandardCharsets.UTF_8)));
		writer.write(result);
		writer.close();
	}
}
