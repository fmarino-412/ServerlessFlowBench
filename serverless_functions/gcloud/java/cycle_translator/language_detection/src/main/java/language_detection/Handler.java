package language_detection;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import org.jetbrains.annotations.NotNull;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.BufferedWriter;
import java.io.IOException;

public class Handler implements HttpFunction {

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

		// request reading, search for image url in request
		String sentence = httpRequest.getFirstQueryParameter("sentence").orElse("");
		if (sentence.equals("")) {
			returnResult(httpResponse.getWriter(), null, null);
			return;
		}

		returnResult(httpResponse.getWriter(), sentence, detectDominantLanguage(sentence));
	}

	private static String detectDominantLanguage(String text) {

		// prepare and perform request
		Translate translate = TranslateOptions.getDefaultInstance().getService();
		Detection detection = translate.detect(text);

		// return result
		return detection.getLanguage();
	}

	private static void returnResult(@NotNull BufferedWriter outputWriter, String sentence, String languageCode)
			throws IOException {

		JsonObjectBuilder job = Json.createObjectBuilder();

		// response creation
		if (sentence == null || languageCode == null) {
			job.add("result", "Error");
		} else {
			job.add("result", "Ok");
			job.add("sentence", sentence);
			job.add("language", languageCode);
		}

		// response writing
		outputWriter.write(job.build().toString());
	}
}
