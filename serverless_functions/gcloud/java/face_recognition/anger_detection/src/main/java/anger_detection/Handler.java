package anger_detection;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;

public class Handler implements HttpFunction {

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

		// request reading, search for image url in request
		String url = httpRequest.getFirstQueryParameter("url").orElse("");
		if (url.equals("")) {
			returnResult(httpResponse.getWriter(), null);
			return;
		}

		// computation
		// image download
		URL connection = new URL(url);
		InputStream inputStream = connection.openStream();
		ByteString image = ByteString.readFrom(inputStream);
		inputStream.close();

		// anger detection
		Boolean bool = detectAnger(image);
		String toRet = null;
		if (bool != null) {
			toRet = bool.toString();
		}

		returnResult(httpResponse.getWriter(), toRet);
	}

	private static Boolean detectAnger(ByteString image) {
		try {
			// prepare request
			Image img = Image.newBuilder().setContent(image).build();
			Feature feat = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();
			AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();

			ImageAnnotatorClient client = ImageAnnotatorClient.create();

			// perform request and analyze results
			for (AnnotateImageResponse response : client.batchAnnotateImages(Collections.singletonList(request))
					.getResponsesList()) {
				if (response.hasError()) {
					client.shutdownNow();
					return null;
				}
				for (FaceAnnotation annotation : response.getFaceAnnotationsList()) {
					if (annotation.getAngerLikelihood().equals(Likelihood.LIKELY) ||
							annotation.getAngerLikelihood().equals(Likelihood.VERY_LIKELY)) {
						client.shutdownNow();
						return true;
					}
				}
			}
			client.shutdownNow();
		} catch (IOException ignored) {
			return null;
		}
		return false;
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
