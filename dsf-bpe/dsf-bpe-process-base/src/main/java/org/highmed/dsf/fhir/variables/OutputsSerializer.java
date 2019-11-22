package org.highmed.dsf.fhir.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.fhir.variables.OutputsValues.OutputsValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OutputsSerializer extends PrimitiveValueSerializer<OutputsValue> implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public OutputsSerializer(ObjectMapper objectMapper)
	{
		super(OutputsValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(OutputsValue value, ValueFields valueFields)
	{
		Outputs targets = value.getValue();
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
	public OutputsValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return OutputsValues.create((Outputs) untypedValue.getValue());
	}

	@Override
	public OutputsValue readValue(ValueFields valueFields)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			Outputs targets = (bytes == null || bytes.length <= 0) ?
					null :
					objectMapper.readValue(bytes, Outputs.class);
			return OutputsValues.create(targets);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
