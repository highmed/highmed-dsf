package org.highmed.openehr.model.structure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "_href", "_type", "_schema_version", "_created", "_generator", "_executed_aql" })
public class Meta
{
	@JsonProperty("_href")
	private final String href;
	@JsonProperty("_type")
	private final String type;
	@JsonProperty("_schema_version")
	private final String schemaVersion;
	@JsonProperty("_created")
	private final String created;
	@JsonProperty("_generator")
	private final String generator;
	@JsonProperty("_executed_aql")
	private final String executedAql;

	@JsonCreator
	public Meta(@JsonProperty("_href") String href, @JsonProperty("_type") String type,
			@JsonProperty("_schema_version") String schemaVersion, @JsonProperty("_created") String created,
			@JsonProperty("_generator") String generator, @JsonProperty("_executed_aql") String executedAql)
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

	public String getCreated()
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
