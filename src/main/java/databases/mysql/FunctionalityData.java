package databases.mysql;

import com.sun.istack.internal.Nullable;

/**
 * Collection of functionality (function or composition) information
 */
public class FunctionalityData {
	/**
	 * Information
	 */
	private final String functionalityName;
	private final String region;

	/**
	 * If not null, it can be an ARN or an API ID
	 */
	@Nullable
	private final String id;


	/**
	 * Constructor without id
	 * @param functionalityName name of the functionality
	 * @param region functionality region of deployment
	 */
	public FunctionalityData(String functionalityName, String region) {
		this.functionalityName = functionalityName;
		this.region = region;
		this.id = null;
	}

	/**
	 * All arguments constructor
	 * @param functionalityName name of the functionality
	 * @param region functionality region of deployment
	 * @param id functionality ARN or API id
	 */
	public FunctionalityData(String functionalityName, String region, String id) {
		this.functionalityName = functionalityName;
		this.region = region;
		this.id = id;
	}

	public String getFunctionalityName() {
		return functionalityName;
	}

	public String getRegion() {
		return region;
	}

	public String getId() {
		return id;
	}
}
