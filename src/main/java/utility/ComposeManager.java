package utility;

import cmd.CommandUtility;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for Docker compose file management.
 */
public class ComposeManager {

	// singleton instance
	private static ComposeManager singletonInstance = null;

	// composition file loaded in a Java Map
	private Map<String, Object> composition = null;

	/**
	 * Containers' names
	 */
	private static final String MYSQL = "mysql-db";
	private static final String GRAFANA = "grafana";
	private static final String INFLUX = "influx-db";

	/**
	 * Docker compose file path
	 */
	private static final String COMPOSE_FILE =
			PropertiesManager.getInstance().getProperty(PropertiesManager.DOCKER_COMPOSE_DIR) +
					CommandUtility.getPathSep() + "docker-compose.yml";


	/**
	 * Singleton instance getter
	 * @return ComposeManager run-wide unique instance
	 */
	public static ComposeManager getInstance() {

		if (singletonInstance == null) {
			singletonInstance = new ComposeManager();
		}
		return singletonInstance;
	}

	/**
	 * Private default constructor. Only getInstance() method can access it
	 */
	private ComposeManager() {
	}

	private void initComposition() throws FileNotFoundException {
		if (composition == null) {
			Yaml yaml = new Yaml();
			File composeFile = new File(COMPOSE_FILE);
			InputStream inputStream = new FileInputStream(composeFile);
			composition = yaml.load(inputStream);
		}
	}

	/**
	 * Getter for MySQL docker image
	 * @return name of the docker image as string
	 */
	public String getMysql() {
		try {
			initComposition();
			// noinspection rawtypes
			return (String)((HashMap)((HashMap)composition.get("services")).get(MYSQL)).get("image");
		} catch (FileNotFoundException e) {
			System.err.println("Could not load composition property: " + e.getMessage());
			return "";
		}

	}

	/**
	 * Getter for InfluxDB docker image
	 * @return name of the docker image as string
	 */
	public String getInflux() {
		try {
			initComposition();
			// noinspection rawtypes
			return (String)((HashMap)((HashMap)composition.get("services")).get(INFLUX)).get("image");
		} catch (FileNotFoundException e) {
			System.err.println("Could not load composition property: " + e.getMessage());
			return "";
		}
	}

	/**
	 * Getter for Grafana docker image
	 * @return name of the docker image as string
	 */
	public String getGrafana() {
		try {
			initComposition();
			// noinspection rawtypes
			return (String)((HashMap)((HashMap)composition.get("services")).get(GRAFANA)).get("image");
		} catch (FileNotFoundException e) {
			System.err.println("Could not load composition property: " + e.getMessage());
			return "";
		}
	}

	/**
	 * Getter for local volumes definitions
	 * @return list of Docker composition directory defined subdirectories
	 */
	public List<String> getLocalVolumes() {
		try {
			initComposition();

			List<String> volumes = new ArrayList<>();

			// local directory volume pattern
			Pattern pattern = Pattern.compile("^(\\.)(/.*)(:.*)");

			// noinspection unchecked,rawtypes
			((HashMap)composition.get("services")).forEach((serviceName, serviceContent) -> {
				//noinspection unchecked,rawtypes
				((List)((HashMap)serviceContent).get("volumes")).forEach(volume -> {
					if (((String) volume).contains("./")) {
						// check local directory definition
						Matcher matcher = pattern.matcher((String)volume);
						if (matcher.find()) {
							// add local directory definition to local volumes list
							volumes.add(matcher.group(2));
						}
					}
				});

			});
			return volumes;
		} catch (FileNotFoundException e) {
			System.err.println("Could not load composition property: " + e.getMessage());
			return null;
		}
	}
}
