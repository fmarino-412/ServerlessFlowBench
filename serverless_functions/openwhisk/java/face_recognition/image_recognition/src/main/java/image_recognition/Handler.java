package image_recognition;

import com.google.gson.JsonObject;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageAnalysis;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageTag;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.VisualFeatureTypes;

import java.util.Collections;

public class Handler {

	public static JsonObject main(JsonObject args) throws Exception {

		// search for image url in request
		String url;
		if (args.has("body") && args.getAsJsonObject("body").has("url")) {
			url = args.getAsJsonObject("body").get("url").getAsString();
		} else {
			throw new Exception("Missing argument in image recognition");
		}

		// perform image analysis
		String result = detectObjectsAndScenes(url);

		// prepare response
		JsonObject body = new JsonObject();
		body.addProperty("url", url);

		JsonObject response = new JsonObject();
		response.addProperty("value", result.contains("person"));
		response.add("body", body);

		// return response
		return response;
	}

	private static String detectObjectsAndScenes(String image) {

		// prepare and perform request
		ComputerVisionClient client = ComputerVisionManager.authenticate(AzureConfig.key)
				.withEndpoint(AzureConfig.endpoint);
		ImageAnalysis analysis = client.computerVision().analyzeImage()
				.withUrl(image)
				.withVisualFeatures(Collections.singletonList(VisualFeatureTypes.TAGS))
				.execute();

		// analyze result
		StringBuilder resultBuilder = new StringBuilder();
		for (ImageTag tag : analysis.tags()) {
			resultBuilder.append(tag.name().toLowerCase()).append(", ");
		}
		resultBuilder.delete(resultBuilder.length() - 3, resultBuilder.length() - 1);
		return resultBuilder.toString();
	}
}