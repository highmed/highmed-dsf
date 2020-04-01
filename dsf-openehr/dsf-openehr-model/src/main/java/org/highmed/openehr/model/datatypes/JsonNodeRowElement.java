package org.highmed.openehr.model.datatypes;

import org.highmed.openehr.model.structure.RowElement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	public static JsonNodeRowElement fromString(String value, ObjectMapper openEhrObjectMapper)
	{
		try
		{
			return new JsonNodeRowElement(openEhrObjectMapper.readTree(value));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}
}
