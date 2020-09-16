package cmd.functionality_commands.security;


import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.auth.oauth2.GoogleCredentials;
import utility.PropertiesManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleAuthClient {

	private static GoogleAuthClient singletonInstance = null;
	private GoogleCredentials credential = null;

	public static GoogleAuthClient getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new GoogleAuthClient();
		}
		return singletonInstance;
	}

	private GoogleAuthClient() {
	}

	private GoogleCredentials authenticateApplication() throws GeneralSecurityException, IOException {

		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		return GoogleCredentials.fromStream(new FileInputStream(
				PropertiesManager.getInstance().getProperty(PropertiesManager.GOOGLE_AUTH_JSON)))
				.createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));

	}

	public String getUrlToken() {
		try {
			if (credential == null) {
				credential = authenticateApplication();
			}
			credential.refreshIfExpired();
			return "&token=" + credential.getAccessToken().getTokenValue();
		} catch (GeneralSecurityException | IOException e) {
			System.err.println("Could not authenticate application: " + e.getMessage());
			return "";
		}
	}

	public static void main(String[] args) {
		System.out.println(GoogleAuthClient.getInstance().getUrlToken());
	}
}
