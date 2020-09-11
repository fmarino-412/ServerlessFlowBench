package databases.mysql;

import com.sun.istack.internal.Nullable;

public class FunctionalityData {
	private final String functionName;
	private final String region;
	// ARN or API ID
	@Nullable
	private final String id;

	public FunctionalityData(String functionName, String region) {
		this.functionName = functionName;
		this.region = region;
		this.id = null;
	}

	public FunctionalityData(String functionName, String region, String id) {
		this.functionName = functionName;
		this.region = region;
		this.id = id;
	}

	public String getFunctionName() {
		return functionName;
	}

	public String getRegion() {
		return region;
	}

	public String getId() {
		return id;
	}
}
