package org.highmed.dsf.bpe.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResultsValues.FeasibilityQueryResultsValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FeasibilityQueryResultsSerializer extends PrimitiveValueSerializer<FeasibilityQueryResultsValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public FeasibilityQueryResultsSerializer(ObjectMapper objectMapper)
	{
		super(FeasibilityQueryResultsValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(FeasibilityQueryResultsValue value, ValueFields valueFields)
	{
		FeasibilityQueryResults results = value.getValue();
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
	public FeasibilityQueryResultsValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return FeasibilityQueryResultsValues.create((FeasibilityQueryResults) untypedValue.getValue());
	}

	@Override
	public FeasibilityQueryResultsValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			FeasibilityQueryResults results = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, FeasibilityQueryResults.class);
			return FeasibilityQueryResultsValues.create(results);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
