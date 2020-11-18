package image_recognition;

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
			throw new Exception("Missing argument in image recognition");
		}

		// perform image analysis
		String result = detectObjectsAndScenes(url);

		// prepare response
		JsonObject body = new JsonObject();
		body.addProperty("url", url);

		JsonObject response = new JsonObject();
		response.addProperty("value", result.contains("person") ? Boolean.TRUE : Boolean.FALSE);
		response.add("body", body);

		// return response
		return response;
	}

	private static String detectObjectsAndScenes(String image) throws Exception {

		String requestBody = "{\"url\":\"" + image + "\"}";

		HttpClient client = HttpClientBuilder.create().build();

		try {
			URIBuilder builder = new URIBuilder(AzureConfig.endpoint + "vision/v3.1/analyze");

			// request parameters
			builder.setParameter("visualFeatures", "Categories,Description");

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

			// analyze result
			if (entity != null) {
				String jsonString = EntityUtils.toString(entity);
				JSONArray tags = (new JSONObject(jsonString)).getJSONObject("description").getJSONArray("tags");

				StringBuilder resultBuilder = new StringBuilder();
				for (int i = 0; i < tags.length(); i++) {
					resultBuilder.append(tags.get(i).toString()).append(", ");
				}
				resultBuilder.delete(resultBuilder.length() - 3, resultBuilder.length() - 1);
				return resultBuilder.toString();
			} else {
				throw new Exception("No result received in anger detection");
			}
		} catch (Exception e) {
			throw new Exception("Error in anger detection HTTP request: " + e.getMessage());
		}
	}
}