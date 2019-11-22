package org.highmed.openehr;

import org.highmed.openehr.deserializer.RowElementDeserializer;
import org.highmed.openehr.deserializer.RowElementSerializer;
import org.highmed.openehr.model.datatypes.DoubleRowElement;
import org.highmed.openehr.model.datatypes.IntegerRowElement;
import org.highmed.openehr.model.datatypes.JsonNodeRowElement;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.datatypes.ZonedDateTimeRowElement;
import org.highmed.openehr.model.structure.RowElement;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class OpenEhrObjectMapperFactory
{
	private OpenEhrObjectMapperFactory()
	{
	}

	public static ObjectMapper createObjectMapper()
	{
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);

		SimpleModule module = new SimpleModule();
		module.addDeserializer(RowElement.class, new RowElementDeserializer());
		module.addSerializer(IntegerRowElement.class, new RowElementSerializer());
		module.addSerializer(DoubleRowElement.class, new RowElementSerializer());
		module.addSerializer(StringRowElement.class, new RowElementSerializer());
		module.addSerializer(ZonedDateTimeRowElement.class, new RowElementSerializer());
		module.addSerializer(JsonNodeRowElement.class, new RowElementSerializer());
		objectMapper.registerModule(module);

		return objectMapper;
	}
}
