package org.highmed.openehr.model.structur;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Meta
{
	private final String href;
	private final String type;
	private final String schemaVersion;
	private final String created;
	private final String generator;
	private final String executedAql;

	@JsonCreator
	public Meta(
			@JsonProperty("_href")
					String href,
			@JsonProperty("_type")
					String type,
			@JsonProperty("_schema_version")
					String schemaVersion,
			@JsonProperty("_created")
					String created,
			@JsonProperty("_generator")
					String generator,
			@JsonProperty("_executed_aql")
					String executedAql)
	{
		this.href = href;
		this.type = type;
		this.schemaVersion = schemaVersion;
		this.created = created;
		this.generator = generator;
		this.executedAql = executedAql;
	}

	public String getHref()
	{
		return href;
	}

	public String getType()
	{
		return type;
	}

	public String getSchemaVersion()
	{
		return schemaVersion;
	}

	public String geCreated()
	{
		return created;
	}

	public String getGenerator()
	{
		return generator;
	}

	public String getExecutedAql()
	{
		return executedAql;
	}
}
