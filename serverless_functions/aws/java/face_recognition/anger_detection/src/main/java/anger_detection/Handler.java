package anger_detection;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
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
			returnResult(outputStream, null);
			return;
		}

		try {
			// computation
			// image download
			URL connection = new URL(url);
			BufferedImage bufferedImage = ImageIO.read(connection);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
			byte[] image = byteArrayOutputStream.toByteArray();
			byteArrayOutputStream.close();

			// anger detection
			returnResult(outputStream, detectAnger(ByteBuffer.wrap(image)));
		} catch (IOException ignored) {
			returnResult(outputStream, null);
		}
	}

	private static Boolean detectAnger(ByteBuffer image) {

		// prepare request
		AmazonRekognition client = AmazonRekognitionClientBuilder.defaultClient();

		DetectFacesRequest request = new DetectFacesRequest()
				.withImage(new Image().withBytes(image))
				.withAttributes("ALL");

		// perform request and analyze results
		DetectFacesResult result = client.detectFaces(request);
		for (FaceDetail detail : result.getFaceDetails()) {
			for (Emotion emotion : detail.getEmotions()) {
				if (emotion.getType().equals("ANGRY") && emotion.getConfidence() >= 60) {
					client.shutdown();
					return true;
				}
			}

		}
		client.shutdown();
		return false;
	}

	private static void returnResult(@NotNull OutputStream outputStream, Boolean detectionResult) {

		// response creation
		String result;
		if (detectionResult == null) {
			result = "Error";
		} else {
			result = detectionResult.toString();
		}

		// response writing
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream,
				StandardCharsets.UTF_8)));
		writer.write(result);
		writer.close();
	}
}
