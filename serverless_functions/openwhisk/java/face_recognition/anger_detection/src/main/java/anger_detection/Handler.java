package anger_detection;

import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;

public class Handler {

	public static JsonObject main(JsonObject args) throws Exception {

		// search for image url in request
		String url;
		if (args.has("body") && args.getAsJsonObject("body").has("url")) {
			url = args.getAsJsonObject("body").get("url").getAsString();
		} else {
			throw new Exception("Missing argument in anger detection");
		}

		// perform image analysis
		boolean result = detectAnger(url);

		// prepare response
		JsonObject response = new JsonObject();
		response.addProperty("value", result);

		// return response
		return response;
	}

	private static boolean detectAnger(String image) throws Exception {

		String requestBody = "{\"url\":\"" + image + "\"}";

		HttpClient client = HttpClientBuilder.create().build();

		try {
			URIBuilder builder = new URIBuilder(AzureConfig.endpoint + "/face/v1.0/detect");

			// request parameters
			builder.setParameter("detectionModel", "detection_01");
			builder.setParameter("returnFaceAttributes", "emotion");
			builder.setParameter("returnFaceId", "false");

			// prepare the URI for the REST API call
			URI uri = builder.build();
			HttpPost request = new HttpPost(uri);

			// request headers
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Ocp-Apim-Subscription-Key", AzureConfig.key);

			// request body
			StringEntity reqEntity = new StringEntity(requestBody);
			request.setEntity(reqEntity);

			// execute the REST API call and get the response entity
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				String jsonString = EntityUtils.toString(entity).trim();
				JSONArray responseArray;
				if (jsonString.startsWith("[")) {
					// it's an array
					responseArray = new JSONArray(jsonString);
				} else if (jsonString.startsWith("{")) {
					// it's a single object
					JSONObject jsonObject = new JSONObject(jsonString);
					responseArray = new JSONArray();
					responseArray.put(jsonObject.toString());
				} else {
					throw new Exception("Error in anger detection response");
				}

				for (int i = 0; i < responseArray.length(); i++) {
					if (responseArray.getJSONObject(i).getJSONObject("faceAttributes").getJSONObject("emotion").has("anger") &&
							responseArray.getJSONObject(i).getJSONObject("faceAttributes").getJSONObject("emotion").getFloat("anger") >= 0.6) {
						return true;
					}
				}
			} else {
				throw new Exception("No result received in anger detection");
			}
			return false;
		} catch (Exception e) {
			throw new Exception("Error in anger detection HTTP request: " + e.getMessage());
		}

	}
}
