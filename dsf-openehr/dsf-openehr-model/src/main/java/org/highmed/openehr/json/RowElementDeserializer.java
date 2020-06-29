package org.highmed.openehr.json;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.highmed.openehr.model.datatypes.DoubleRowElement;
import org.highmed.openehr.model.datatypes.IntegerRowElement;
import org.highmed.openehr.model.datatypes.JsonNodeRowElement;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.datatypes.ZonedDateTimeRowElement;
import org.highmed.openehr.model.structure.RowElement;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class RowElementDeserializer extends JsonDeserializer<RowElement>
{
	@Override
	public RowElement deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException
	{
		JsonNode node = jsonParser.getCodec().readTree(jsonParser);

		if (node.isIntegralNumber())
			return new IntegerRowElement(node.asInt());
		else if (node.isFloatingPointNumber())
			return new DoubleRowElement(node.asDouble());
		else if (node.isTextual())
		{
			ZonedDateTimeRowElement dateTime = getZonedDateTime(node.asText());
			if (dateTime != null)
				return dateTime;
			else
				return new StringRowElement(node.asText());
		}
		else
			return new JsonNodeRowElement(node);
	}

	private ZonedDateTimeRowElement getZonedDateTime(String value)
	{
		// TODO control flow by exception
		try
		{
			return new ZonedDateTimeRowElement(ZonedDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		}
		catch (DateTimeParseException e)
		{
			return null;
		}
	}
}
