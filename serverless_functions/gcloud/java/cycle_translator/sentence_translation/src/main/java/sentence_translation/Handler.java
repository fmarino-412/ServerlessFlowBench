package sentence_translation;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.BufferedWriter;
import java.io.IOException;

public class Handler implements HttpFunction {

	private static final Gson gson = new Gson();

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

		// set up response type
		httpResponse.setContentType("application/json");

		// request reading, search for string and code in request
		JsonElement requestParsed = gson.fromJson(httpRequest.getReader(), JsonElement.class);
		JsonObject requestJson;

		if (requestParsed != null && requestParsed.isJsonObject()) {
			requestJson = requestParsed.getAsJsonObject();
		} else {
			returnResult(httpResponse.getWriter(), null, null);
			return;
		}

		String sentence;
		if (requestJson != null && requestJson.has("sentence")) {
			sentence = requestJson.get("sentence").getAsString();
		} else {
			returnResult(httpResponse.getWriter(), null, null);
			return;
		}

		String languageCode;
		if (requestJson.has("language_code")) {
			languageCode = requestJson.get("language_code").getAsString();
		} else {
			returnResult(httpResponse.getWriter(), null, null);
			return;
		}

		returnResult(httpResponse.getWriter(), sentence, translateText(sentence, languageCode));
	}

	private static String translateText(String text, String sourceLanguageCode) {

		// prepare and perform request
		Translate translate = TranslateOptions.getDefaultInstance().getService();
		Translation translation = translate.translate(text,
				Translate.TranslateOption.sourceLanguage(sourceLanguageCode),
				Translate.TranslateOption.targetLanguage("en"));

		// return result
		return translation.getTranslatedText();
	}

	private static void returnResult(@NotNull BufferedWriter outputWriter,
									 String originalSentence, String translatedSentence) throws IOException {

		JsonObjectBuilder job = Json.createObjectBuilder();

		// response creation
		if (originalSentence == null || translatedSentence == null) {
			job.add("result", "Error");
		} else {
			job.add("result", "Ok");
			job.add("original_sentence", originalSentence);
			job.add("sentence", translatedSentence);
		}

		// response writing
		outputWriter.write(job.build().toString());
	}
}
