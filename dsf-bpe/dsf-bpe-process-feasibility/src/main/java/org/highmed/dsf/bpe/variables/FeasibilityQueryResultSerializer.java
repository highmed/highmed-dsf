package org.highmed.dsf.bpe.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResultValues.FeasibilityQueryResultValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FeasibilityQueryResultSerializer extends PrimitiveValueSerializer<FeasibilityQueryResultValue>
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
	public void writeValue(FeasibilityQueryResultValue value, ValueFields valueFields)
	{
		FeasibilityQueryResult result = value.getValue();
		try
		{
			if (result != null)
				valueFields.setByteArrayValue(objectMapper.writeValueAsBytes(result));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public FeasibilityQueryResultValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return FeasibilityQueryResultValues.create((FeasibilityQueryResult) untypedValue.getValue());
	}

	@Override
	public FeasibilityQueryResultValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			FeasibilityQueryResult result = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, FeasibilityQueryResult.class);
			return FeasibilityQueryResultValues.create(result);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
