package org.highmed.dsf.fhir.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetValues.MultiInstanceTargetValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MultiInstanceTargetSerializer extends PrimitiveValueSerializer<MultiInstanceTargetValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public MultiInstanceTargetSerializer(ObjectMapper objectMapper)
	{
		super(MultiInstanceTargetValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(MultiInstanceTargetValue value, ValueFields valueFields)
	{
		MultiInstanceTarget target = value.getValue();
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
	public MultiInstanceTargetValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return MultiInstanceTargetValues.create((MultiInstanceTarget) untypedValue.getValue());
	}

	@Override
	public MultiInstanceTargetValue readValue(ValueFields valueFields)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			MultiInstanceTarget target = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, MultiInstanceTarget.class);
			return MultiInstanceTargetValues.create(target);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
