package org.highmed.dsf.fhir.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FeasibilityQueryResultSerializer
		extends PrimitiveValueSerializer<FeasibilityQueryResultValues.FeasibilityQueryResultValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public FeasibilityQueryResultSerializer(ObjectMapper objectMapper)
	{
		super(FeasibilityQueryResultValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(FeasibilityQueryResultValues.FeasibilityQueryResultValue value, ValueFields valueFields)
	{
		FeasibilityQueryResult target = value.getValue();
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
	public FeasibilityQueryResultValues.FeasibilityQueryResultValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return FeasibilityQueryResultValues.create((FeasibilityQueryResult) untypedValue.getValue());
	}

	@Override
	public FeasibilityQueryResultValues.FeasibilityQueryResultValue readValue(ValueFields valueFields)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			FeasibilityQueryResult target = (bytes == null || bytes.length <= 0) ?
					null :
					objectMapper.readValue(bytes, FeasibilityQueryResult.class);
			return FeasibilityQueryResultValues.create(target);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
