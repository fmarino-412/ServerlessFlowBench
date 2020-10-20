package sentence_translation;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.jetbrains.annotations.NotNull;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.BufferedWriter;
import java.io.IOException;

public class Handler implements HttpFunction {

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

		// request reading, search for string and code in request
		String sentence = httpRequest.getFirstQueryParameter("sentence").orElse("");
		if (sentence.equals("")) {
			returnResult(httpResponse.getWriter(), null, null);
			return;
		}
		String languageCode = httpRequest.getFirstQueryParameter("language_code").orElse("");
		if (languageCode.equals("")) {
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
