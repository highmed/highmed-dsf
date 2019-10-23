package org.highmed.openehr.model.structur;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Column
{
	private final String name;
	private final String path;

	private Column(
			@JsonProperty("name")
					String name,
			@JsonProperty("path")
					String path)
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
