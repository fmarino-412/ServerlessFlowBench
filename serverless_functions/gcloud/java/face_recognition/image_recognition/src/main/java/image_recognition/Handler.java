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
import java.net.URL;
import java.util.Collections;

public class Handler implements HttpFunction {
	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

		// request reading
		String url = httpRequest.getFirstQueryParameter("url").orElse("");
		if (url.equals("")) {
			returnResult(httpResponse.getWriter(), null);
			return;
		}

		// computation
		URL connection = new URL(url);
		ByteString image = ByteString.readFrom(connection.openStream());

		String toRet = detectObjectsAndScenes(image);
		if (toRet != null && toRet.contains("face")) {
			toRet = "face";
		} else if (toRet != null) {
			toRet = "other";
		}
		returnResult(httpResponse.getWriter(), toRet);
	}

	private static String detectObjectsAndScenes(ByteString image) {
		try {
			JsonObjectBuilder result = Json.createObjectBuilder();
			Image img = Image.newBuilder().setContent(image).build();
			Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
			AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();

			ImageAnnotatorClient client = ImageAnnotatorClient.create();

			for (AnnotateImageResponse response : client.batchAnnotateImages(Collections.singletonList(request))
					.getResponsesList()) {
				if (response.hasError()) {
					return null;
				}
				for (EntityAnnotation label : response.getLabelAnnotationsList()) {
					result.add(label.getDescription().toLowerCase(), label.getScore() * 100f);
				}
			}

			return result.build().toString();
		} catch (IOException e) {
			return null;
		}

	}

	private static void returnResult(@NotNull BufferedWriter outputWriter, String result)
			throws IOException {

		// response creation
		if (result == null) {
			result = "Error";
		}

		// response writing
		outputWriter.write(result);
	}
}
