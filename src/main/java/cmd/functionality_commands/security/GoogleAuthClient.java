package cmd.functionality_commands.security;


import com.google.auth.oauth2.GoogleCredentials;
import utility.PropertiesManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

/**
 * Client to perform authentication on Google Cloud Platform as an application service.
 * This class ensure token generation to allow a Google Cloud Functions handler to use Google Cloud Platform
 * Workflows [BETA] Execution REST APIs.
 */
public class GoogleAuthClient {

	// singleton instance: no need to request credentials at each call
	private static GoogleAuthClient singletonInstance = null;
	private GoogleCredentials credential = null;

	/**
	 * Singleton instance getter
	 * @return GoogleAuthClient run-wide unique instance
	 */
	public static GoogleAuthClient getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new GoogleAuthClient();
		}
		return singletonInstance;
	}

	/**
	 * Private default constructor. Only getInstance() method can access it
	 */
	private GoogleAuthClient() {
	}

	/**
	 * Performs a new authentication flow to obtain credentials
	 * @return application service credentials
	 * @throws IOException for any problem reading credentials internal file
	 */
	private GoogleCredentials authenticateApplication() throws IOException {
		return GoogleCredentials.fromStream(new FileInputStream(
				PropertiesManager.getInstance().getProperty(PropertiesManager.GOOGLE_AUTH_JSON)))
				.createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));

	}

	/**
	 * Get the access token
	 * @return string containing the refreshed access token, "" if any error occurred
	 */
	public String getUrlToken() {
		try {
			if (credential == null) {
				// create new credentials if current instance is null
				credential = authenticateApplication();
			}
			// refresh token
			credential.refreshIfExpired();
			return "&token=" + credential.getAccessToken().getTokenValue();
		} catch (IOException e) {
			System.err.println("Could not authenticate application: " + e.getMessage());
			return "";
		}
	}
}
