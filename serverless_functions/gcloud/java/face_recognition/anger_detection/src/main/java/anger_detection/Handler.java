package anger_detection;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.protobuf.ByteString;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;

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


		return null;
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
