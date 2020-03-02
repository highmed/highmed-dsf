package org.highmed.pseudonymization.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.primitives.Longs;

public class BitSetDeserializer extends JsonDeserializer<BitSet>
{

	@Override
	public BitSet deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException
	{
		ArrayList<Long> l = new ArrayList<>();
		JsonToken token;
		while (!JsonToken.END_ARRAY.equals(token = jsonParser.nextValue()))
		{
			if (token.isNumeric())
			{
				l.add(jsonParser.getLongValue());
			}
		}
		return BitSet.valueOf(Longs.toArray(l));
	}
}
