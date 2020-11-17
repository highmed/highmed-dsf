package org.highmed.dsf.fhir.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.fhir.variables.TargetValues.TargetValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TargetSerializer extends PrimitiveValueSerializer<TargetValue> implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public TargetSerializer(ObjectMapper objectMapper)
	{
		super(TargetValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(TargetValue value, ValueFields valueFields)
	{
		Target target = value.getValue();
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
	public TargetValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return TargetValues.create((Target) untypedValue.getValue());
	}

	@Override
	public TargetValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			Target target = (bytes == null || bytes.length <= 0) ? null : objectMapper.readValue(bytes, Target.class);
			return TargetValues.create(target);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
