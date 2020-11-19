package image_recognition;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

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
		// search for image url in request
		String url;
		if (event.containsKey("url")) {
			url = (String)event.get("url");
		} else {
			returnResult(outputStream, null, null);
			return;
		}

		try {
			// computation
			// image download
			URL connection = new URL(url);
			BufferedImage bufferedImage = ImageIO.read(connection);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);

			// objects and scenes detection
			String toRet = detectObjectsAndScenes(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()));
			if (toRet.contains("face")) {
				toRet = "face";
			} else {
				toRet = "other";
			}

			returnResult(outputStream, toRet, url);
		} catch (IOException ignored) {
			returnResult(outputStream, null, null);
		}
	}

	private static String detectObjectsAndScenes(ByteBuffer image) {

		// prepare request
		AmazonRekognition client = AmazonRekognitionClientBuilder.defaultClient();

		DetectLabelsRequest request = new DetectLabelsRequest()
				.withImage(new Image().withBytes(image))
				.withMaxLabels(100)
				.withMinConfidence(70f);

		// perform request and analyze results
		DetectLabelsResult result = client.detectLabels(request);

		StringBuilder resultBuilder = new StringBuilder();
		for (Label label : result.getLabels()) {
			resultBuilder.append(label.getName().toLowerCase()).append(", ");
		}
		resultBuilder.delete(resultBuilder.length() - 3, resultBuilder.length() - 1);
		return resultBuilder.toString();
	}

	private static void returnResult(@NotNull OutputStream outputStream, String result, String url) {

		// response creation
		if (result == null || url == null) {
			result = "Error";
		} else {
			JsonObjectBuilder job = Json.createObjectBuilder();
			job.add("result", result);
			job.add("image", url);
			result = job.build().toString();
		}

		// response writing
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream,
				StandardCharsets.UTF_8)));
		writer.write(result);
		writer.close();
	}
}
