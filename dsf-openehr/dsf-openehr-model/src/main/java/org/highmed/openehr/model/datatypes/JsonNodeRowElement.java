package org.highmed.openehr.model.datatypes;

import org.highmed.openehr.model.structure.RowElement;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodeRowElement implements RowElement
{
	private final JsonNode value;

	public JsonNodeRowElement(JsonNode value)
	{
		this.value = value;
	}

	public JsonNode getValue()
	{
		return value;
	}

	@Override
	public String getValueAsString()
	{
		return getValue() == null ? null : getValue().toString();
	}
}
