package databases.mysql;

import com.sun.istack.internal.Nullable;

public class FunctionData {
	private final String functionName;
	private final String region;
	@Nullable
	private final String apiId;

	public FunctionData(String functionName, String region) {
		this.functionName = functionName;
		this.region = region;
		this.apiId = null;
	}

	public FunctionData(String functionName, String region, String apiId) {
		this.functionName = functionName;
		this.region = region;
		this.apiId = apiId;
	}

	public String getFunctionName() {
		return functionName;
	}

	public String getRegion() {
		return region;
	}

	public String getApiId() {
		return apiId;
	}
}
