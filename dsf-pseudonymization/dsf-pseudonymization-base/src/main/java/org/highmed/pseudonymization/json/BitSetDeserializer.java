package org.highmed.pseudonymization.json;

import java.io.IOException;
import java.util.Base64;
import java.util.BitSet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class BitSetDeserializer extends JsonDeserializer<BitSet>
{
	@Override
	public BitSet deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException
	{
		JsonNode node = jsonParser.getCodec().readTree(jsonParser);
		byte[] rbfBytes = Base64.getDecoder().decode(node.asText());
		return BitSet.valueOf(rbfBytes);
	}
}
