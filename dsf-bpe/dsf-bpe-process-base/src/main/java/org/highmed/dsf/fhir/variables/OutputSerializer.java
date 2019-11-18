package org.highmed.dsf.fhir.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.fhir.variables.OutputValues.OutputValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OutputSerializer extends PrimitiveValueSerializer<OutputValue> implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public OutputSerializer(ObjectMapper objectMapper)
	{
		super(OutputValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(OutputValue value, ValueFields valueFields)
	{
		Output target = value.getValue();
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
	public OutputValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return OutputValues.create((Output) untypedValue.getValue());
	}

	@Override
	public OutputValue readValue(ValueFields valueFields)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			Output target = (bytes == null || bytes.length <= 0) ? null : objectMapper.readValue(bytes, Output.class);
			return OutputValues.create(target);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
