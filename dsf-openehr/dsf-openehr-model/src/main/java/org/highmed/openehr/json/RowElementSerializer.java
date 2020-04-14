package org.highmed.openehr.json;

import java.io.IOException;

import org.highmed.openehr.model.datatypes.DoubleRowElement;
import org.highmed.openehr.model.datatypes.IntegerRowElement;
import org.highmed.openehr.model.datatypes.JsonNodeRowElement;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.datatypes.ZonedDateTimeRowElement;
import org.highmed.openehr.model.structure.RowElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class RowElementSerializer extends JsonSerializer<RowElement>
{
	@Override
	public void serialize(RowElement value, JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		if (value instanceof IntegerRowElement)
			gen.writeNumber(((IntegerRowElement) value).getValue());
		else if (value instanceof DoubleRowElement)
			gen.writeNumber(((DoubleRowElement) value).getValue());
		else if (value instanceof StringRowElement)
			gen.writeString(((StringRowElement) value).getValue());
		else if (value instanceof ZonedDateTimeRowElement)
			gen.writeString(((ZonedDateTimeRowElement) value).getValueAsString());
		else if (value instanceof JsonNodeRowElement)
			gen.writeRawValue(((JsonNodeRowElement) value).getValue().toString());
		else
			throw new IOException("Unsupported RowElement " + value.getClass().getName());
	}
}
