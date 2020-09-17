package databases.mysql;

import com.sun.istack.internal.Nullable;

public class FunctionalityData {
	private final String functionalityName;
	private final String region;
	// ARN or API ID
	@Nullable
	private final String id;

	public FunctionalityData(String functionalityName, String region) {
		this.functionalityName = functionalityName;
		this.region = region;
		this.id = null;
	}

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
