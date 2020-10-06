package org.highmed.dsf.bpe.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResultValues.FinalFeasibilityQueryResultValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FinalFeasibilityQueryResultSerializer extends PrimitiveValueSerializer<FinalFeasibilityQueryResultValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public FinalFeasibilityQueryResultSerializer(ObjectMapper objectMapper)
	{
		super(FinalFeasibilityQueryResultValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(FinalFeasibilityQueryResultValue value, ValueFields valueFields)
	{
		FinalFeasibilityQueryResult result = value.getValue();
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
	public FinalFeasibilityQueryResultValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return FinalFeasibilityQueryResultValues.create((FinalFeasibilityQueryResult) untypedValue.getValue());
	}

	@Override
	public FinalFeasibilityQueryResultValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			FinalFeasibilityQueryResult result = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, FinalFeasibilityQueryResult.class);
			return FinalFeasibilityQueryResultValues.create(result);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
