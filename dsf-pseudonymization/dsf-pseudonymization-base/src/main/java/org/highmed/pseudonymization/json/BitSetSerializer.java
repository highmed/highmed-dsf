package org.highmed.pseudonymization.json;

import java.io.IOException;
import java.util.Base64;
import java.util.BitSet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class BitSetSerializer extends JsonSerializer<BitSet>
{
	@Override
	public void serialize(BitSet value, JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		String string = Base64.getEncoder().encodeToString(value.toByteArray());
		gen.writeString(string);
	}
}
