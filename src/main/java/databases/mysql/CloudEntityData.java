package databases.mysql;

import com.sun.istack.internal.Nullable;

/**
 * Collection of cloud entity (function, composition, table or instance) information
 */
public class CloudEntityData {
	/**
	 * Information
	 */
	private final String entityName;
	@Nullable
	private final String region;

	/**
	 * If not null, it can be an ARN, an API ID or an Instance ID
	 */
	@Nullable
	private final String id;


	/**
	 * Constructor without id
	 * @param entityName name of the entity
	 * @param region entity region of deployment
	 */
	public CloudEntityData(String entityName, String region) {
		this.entityName = entityName;
		this.region = region;
		this.id = null;
	}

	/**
	 * All arguments constructor
	 * @param entityName name of the entity
	 * @param region entity region of deployment
	 * @param id functionality ARN or API id
	 */
	public CloudEntityData(String entityName, String region, String id) {
		this.entityName = entityName;
		this.region = region;
		this.id = id;
	}

	/**
	 * Constructor for only id information
	 * @param id entity id
	 */
	public CloudEntityData(String id) {
		this.entityName = "BigTable_instance";
		this.region = null;
		this.id = id;
	}

	public String getEntityName() {
		return entityName;
	}

	public String getRegion() {
		return region;
	}

	public String getId() {
		return id;
	}
}
