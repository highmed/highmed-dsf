package org.highmed.dsf.fhir.variables;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.fhir.variables.FhirResourcesListValues.FhirResourcesListValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FhirResourcesListSerializer extends PrimitiveValueSerializer<FhirResourcesListValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public FhirResourcesListSerializer(ObjectMapper objectMapper)
	{
		super(FhirResourcesListValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(FhirResourcesListValue value, ValueFields valueFields)
	{
		FhirResourcesList resource = value.getValue();
		try
		{
			if (resource != null)
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				objectMapper.writeValue(out, resource);

				valueFields.setTextValue(resource.getClass().getName());
				valueFields.setByteArrayValue(out.toByteArray());
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public FhirResourcesListValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return FhirResourcesListValues.create((FhirResourcesList) untypedValue.getValue());
	}

	@Override
	public FhirResourcesListValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		String className = valueFields.getTextValue();
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			@SuppressWarnings("unchecked")
			Class<FhirResourcesList> clazz = (Class<FhirResourcesList>) Class.forName(className);
			FhirResourcesList resource = objectMapper.readValue(bytes, clazz);

			return FhirResourcesListValues.create(resource);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
