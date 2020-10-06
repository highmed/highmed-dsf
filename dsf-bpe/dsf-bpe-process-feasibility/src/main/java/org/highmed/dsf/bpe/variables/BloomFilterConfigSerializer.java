package org.highmed.dsf.bpe.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.bpe.variables.BloomFilterConfigValues.BloomFilterConfigValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BloomFilterConfigSerializer extends PrimitiveValueSerializer<BloomFilterConfigValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public BloomFilterConfigSerializer(ObjectMapper objectMapper)
	{
		super(BloomFilterConfigValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(BloomFilterConfigValue value, ValueFields valueFields)
	{
		BloomFilterConfig target = value.getValue();
		try
		{
			if (target != null)
				valueFields.setByteArrayValue(objectMapper.writeValueAsBytes(target));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public BloomFilterConfigValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return BloomFilterConfigValues.create((BloomFilterConfig) untypedValue.getValue());
	}

	@Override
	public BloomFilterConfigValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			BloomFilterConfig target = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, BloomFilterConfig.class);
			return BloomFilterConfigValues.create(target);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
