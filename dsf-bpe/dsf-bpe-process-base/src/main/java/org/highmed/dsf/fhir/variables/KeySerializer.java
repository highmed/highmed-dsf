package org.highmed.dsf.fhir.variables;

import java.io.IOException;
import java.security.Key;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class KeySerializer extends JsonSerializer<Key>
{
	@Override
	public void serialize(Key value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonGenerationException
	{
		jgen.writeStartObject();
		jgen.writeFieldName("algorithm");
		jgen.writeString(value.getAlgorithm());
		jgen.writeFieldName("value");
		jgen.writeBinary(value.getEncoded());
		jgen.writeEndObject();
	}
}
