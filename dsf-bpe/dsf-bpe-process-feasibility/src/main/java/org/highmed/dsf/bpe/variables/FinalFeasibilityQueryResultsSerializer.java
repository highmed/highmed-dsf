package org.highmed.dsf.bpe.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResultsValues.FinalFeasibilityQueryResultsValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FinalFeasibilityQueryResultsSerializer extends PrimitiveValueSerializer<FinalFeasibilityQueryResultsValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public FinalFeasibilityQueryResultsSerializer(ObjectMapper objectMapper)
	{
		super(FinalFeasibilityQueryResultsValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(FinalFeasibilityQueryResultsValue value, ValueFields valueFields)
	{
		FinalFeasibilityQueryResults results = value.getValue();
		try
		{
			if (results != null)
				valueFields.setByteArrayValue(objectMapper.writeValueAsBytes(results));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public FinalFeasibilityQueryResultsValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return FinalFeasibilityQueryResultsValues.create((FinalFeasibilityQueryResults) untypedValue.getValue());
	}

	@Override
	public FinalFeasibilityQueryResultsValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			FinalFeasibilityQueryResults results = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, FinalFeasibilityQueryResults.class);
			return FinalFeasibilityQueryResultsValues.create(results);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
