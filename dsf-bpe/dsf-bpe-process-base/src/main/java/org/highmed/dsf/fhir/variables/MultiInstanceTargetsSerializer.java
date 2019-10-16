package org.highmed.dsf.fhir.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetsValues.MultiInstanceTargetsValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MultiInstanceTargetsSerializer extends PrimitiveValueSerializer<MultiInstanceTargetsValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public MultiInstanceTargetsSerializer(ObjectMapper objectMapper)
	{
		super(MultiInstanceTargetsValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(MultiInstanceTargetsValue value, ValueFields valueFields)
	{
		MultiInstanceTargets targets = value.getValue();
		try
		{
			if (targets != null)
				valueFields.setByteArrayValue(objectMapper.writeValueAsBytes(targets));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public MultiInstanceTargetsValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return MultiInstanceTargetsValues.create((MultiInstanceTargets) untypedValue.getValue());
	}

	@Override
	public MultiInstanceTargetsValue readValue(ValueFields valueFields)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			MultiInstanceTargets targets = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, MultiInstanceTargets.class);
			return MultiInstanceTargetsValues.create(targets);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
