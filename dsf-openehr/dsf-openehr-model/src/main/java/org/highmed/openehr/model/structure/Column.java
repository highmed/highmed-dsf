package org.highmed.openehr.model.structure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Column
{
	@JsonProperty("name")
	private final String name;
	@JsonProperty("path")
	private final String path;

	@JsonCreator
	public Column(@JsonProperty("name") String name, @JsonProperty("path") String path)
	{
		this.name = name;
		this.path = path;
	}

	public String getName()
	{
		return name;
	}

	public String getPath()
	{
		return path;
	}
}
