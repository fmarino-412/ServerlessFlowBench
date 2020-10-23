package translation_logger;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Random;

@SuppressWarnings("rawtypes")
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
		// search for strings, original language code and logging bucket in request
		String originalSentence;
		if (event.containsKey("original_sentence")) {
			originalSentence = (String)event.get("original_sentence");
		} else {
			returnResult(outputStream, false);
			return;
		}
		String originalLanguageCode;
		if (event.containsKey("original_language_code")) {
			originalLanguageCode = (String)event.get("original_language_code");
		} else {
			returnResult(outputStream, false);
			return;
		}
		String translatedSentence;
		if (event.containsKey("translated_sentence")) {
			translatedSentence = (String)event.get("translated_sentence");
		} else {
			returnResult(outputStream, false);
			return;
		}
		String loggingBucketName;
		if (event.containsKey("logging_bucket_name")) {
			loggingBucketName = (String)event.get("logging_bucket_name");
		} else {
			returnResult(outputStream, false);
			return;
		}

		try {
			logTranslation(originalSentence, originalLanguageCode, translatedSentence, "en",
					loggingBucketName);
		} catch (SdkClientException e) {
			returnResult(outputStream, false);
			return;
		}

		// return response
		returnResult(outputStream, true);
	}

	private static void logTranslation(String originalSentence, String originalLanguageCode, String translatedSentence,
									   String destinationLanguageCode, String loggingBucketName)
			throws SdkClientException {

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

		AmazonS3 client = AmazonS3ClientBuilder.defaultClient();
		client.putObject(loggingBucketName, filename, body);

	}

	private static void returnResult(@NotNull OutputStream outputStream, boolean ok) {

		// response creation
		String result;
		if (ok) {
			result = "Logged";
		} else {
			result = "Error";
		}

		// response writing
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream,
				StandardCharsets.UTF_8)));
		writer.write(result);
		writer.close();
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
