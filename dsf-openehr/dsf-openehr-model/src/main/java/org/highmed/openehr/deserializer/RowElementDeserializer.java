package org.highmed.openehr.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.highmed.openehr.model.datatypes.DvCount;
import org.highmed.openehr.model.datatypes.DvOther;
import org.highmed.openehr.model.structur.RowElement;

import java.io.IOException;

public class RowElementDeserializer extends JsonDeserializer<RowElement<?>>
{
	@Override
	public RowElement<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException
	{
		JsonNode node = jsonParser.getCodec().readTree(jsonParser);

		if (node.isInt())
			return new DvCount(node.asInt());
		else
			return new DvOther(node.toString());
	}
}
