package image_recognition;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.jetbrains.annotations.NotNull;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;

public class Handler implements HttpFunction {

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

		// set up response type
		httpResponse.setContentType("application/json");

		// request reading, search for image url in request
		String url = httpRequest.getFirstQueryParameter("url").orElse("");
		if (url.equals("")) {
			returnResult(httpResponse.getWriter(), null, null);
			return;
		}

		// computation
		// image download
		URL connection = new URL(url);
		InputStream inputStream = connection.openStream();
		ByteString image = ByteString.readFrom(inputStream);
		inputStream.close();

		// objects and scenes detection
		String toRet = detectObjectsAndScenes(image);
		if (toRet != null && isFace(toRet)) {
			toRet = "face";
		} else if (toRet != null) {
			toRet = "other";
		}

		returnResult(httpResponse.getWriter(), toRet, url);
	}

	private static boolean isFace(String input) {
		return input.contains("face") || input.contains("cheek") || input.contains("forehead") ||
				input.contains("eyebrow") || input.contains("nose") || input.contains("lip") || input.contains("mouth")
				|| input.contains("eye") || input.contains("lashes");
	}

	private static String detectObjectsAndScenes(ByteString image) {
		try {
			// prepare request
			StringBuilder resultBuilder = new StringBuilder();
			Image img = Image.newBuilder().setContent(image).build();
			Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
			AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();

			ImageAnnotatorClient client = ImageAnnotatorClient.create();

			// perform request and analyze results
			for (AnnotateImageResponse response : client.batchAnnotateImages(Collections.singletonList(request))
					.getResponsesList()) {
				if (response.hasError()) {
					client.shutdownNow();
					return null;
				}
				for (EntityAnnotation label : response.getLabelAnnotationsList()) {
					resultBuilder.append(label.getDescription().toLowerCase()).append(", ");
				}
				resultBuilder.delete(resultBuilder.length() - 2, resultBuilder.length());
			}

			client.shutdownNow();
			return resultBuilder.toString();
		} catch (IOException e) {
			return null;
		}

	}

	private static void returnResult(@NotNull BufferedWriter outputWriter, String result, String url)
			throws IOException {

		// response creation
		JsonObjectBuilder json = Json.createObjectBuilder();
		if (result == null || url == null) {
			json.add("result", "Error");
		} else {
			json.add("result", result);
			json.add("image", url);
		}

		// response writing
		outputWriter.write(json.build().toString());
	}
}
