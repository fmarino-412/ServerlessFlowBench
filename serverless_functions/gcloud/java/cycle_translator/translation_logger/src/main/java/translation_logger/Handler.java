package translation_logger;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.storage.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Handler implements HttpFunction {

	private static final Gson gson = new Gson();

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

		// search for strings, original language code and logging bucket in request
		JsonElement requestParsed = gson.fromJson(httpRequest.getReader(), JsonElement.class);
		JsonObject requestJson;

		if (requestParsed != null && requestParsed.isJsonObject()) {
			requestJson = requestParsed.getAsJsonObject();
		} else {
			returnResult(httpResponse.getWriter(), false);
			return;
		}

		String originalSentence;
		if (requestJson != null && requestJson.has("original_sentence")) {
			originalSentence = requestJson.get("original_sentence").getAsString();
		} else {
			returnResult(httpResponse.getWriter(), false);
			return;
		}

		String originalLanguageCode;
		if (requestJson.has("original_language_code")) {
			originalLanguageCode = requestJson.get("original_language_code").getAsString();
		} else {
			returnResult(httpResponse.getWriter(), false);
			return;
		}

		String translatedSentence;
		if (requestJson.has("translated_sentence")) {
			translatedSentence = requestJson.get("translated_sentence").getAsString();
		} else {
			returnResult(httpResponse.getWriter(), false);
			return;
		}

		String loggingBucketName;
		if (requestJson.has("logging_bucket_name")) {
			loggingBucketName = requestJson.get("logging_bucket_name").getAsString();
		} else {
			returnResult(httpResponse.getWriter(), false);
			return;
		}

		logTranslation(originalSentence, originalLanguageCode, translatedSentence, "en",
				loggingBucketName);

		// return response
		returnResult(httpResponse.getWriter(), true);
	}

	private static void logTranslation(String originalSentence, String originalLanguageCode, String translatedSentence,
									   String destinationLanguageCode, String loggingBucketName) throws IOException {

		// timestamp
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		String timestamp = formatter.format(LocalDateTime.now());

		// create filename
		String filename = "Translation " + timestamp + makeId() + ".log";
		filename = filename.replace(" ", "_");

		// create body
		String body = "Translation info:" + "\n\n" + "original sentence: " + originalSentence + "\n" +
				"original language: " + originalLanguageCode + "\n" + "translated sentence: " + translatedSentence +
				"\n" + "destination language: " + destinationLanguageCode + "\n" + "log date: " +
				timestamp.split(" ")[0] + "\n" + "log time: " + timestamp.split(" ")[1];

		// connect Google Cloud Storage
		GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
		Bucket bucket = storage.get(loggingBucketName);

		// create file
		Blob blob = bucket.create(filename, body.getBytes(StandardCharsets.UTF_8), "text/plain");
		// make file private
		blob.createAcl(Acl.of(Acl.User.ofAllAuthenticatedUsers(), Acl.Role.READER));
	}

	private static void returnResult(@NotNull BufferedWriter outputWriter, boolean ok) throws IOException {

		// response creation
		String result;
		if (ok) {
			result = "Logged";
		} else {
			result = "Error";
		}

		// response writing
		outputWriter.write(result);
	}

	private static String makeId() {

		StringBuilder result = new StringBuilder();
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		for (int i = 0; i < 8; i++) {
			result.append(characters.charAt(Math.abs(new Random().nextInt()) % characters.length()));
		}
		return "[JavaRuntime_" + result.toString() + "]";
	}
}
