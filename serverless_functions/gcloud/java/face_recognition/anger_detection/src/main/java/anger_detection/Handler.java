package anger_detection;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.jetbrains.annotations.NotNull;

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

		Boolean bool = detectAnger(image);
		String toRet = null;
		if (bool != null) {
			toRet = bool.toString();
		}
		returnResult(httpResponse.getWriter(), toRet);
	}

	private static Boolean detectAnger(ByteString image) {
		try {
			Image img = Image.newBuilder().setContent(image).build();
			Feature feat = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();
			AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();

			ImageAnnotatorClient client = ImageAnnotatorClient.create();

			for (AnnotateImageResponse response : client.batchAnnotateImages(Collections.singletonList(request))
					.getResponsesList()) {
				if (response.hasError()) {
					return null;
				}
				for (FaceAnnotation annotation : response.getFaceAnnotationsList()) {
					if (annotation.getAngerLikelihood().equals(Likelihood.LIKELY) ||
							annotation.getAngerLikelihood().equals(Likelihood.VERY_LIKELY)) {
						return true;
					}
				}
			}
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
