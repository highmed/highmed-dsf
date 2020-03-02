package org.highmed.pseudonymization.util;

import java.io.IOException;
import java.util.BitSet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class BitSetSerializer extends JsonSerializer<BitSet>
{

	@Override
	public void serialize(BitSet bitSet, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException
	{
		jsonGenerator.writeStartArray();
		for (long l : bitSet.toLongArray())
		{
			jsonGenerator.writeNumber(l);
		}
		jsonGenerator.writeEndArray();
	}

	@Override
	public Class<BitSet> handledType()
	{
		return BitSet.class;
	}
}