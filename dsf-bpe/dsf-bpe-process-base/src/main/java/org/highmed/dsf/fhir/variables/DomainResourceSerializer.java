package org.highmed.dsf.fhir.variables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.fhir.variables.DomainResourceValues.DomainResourceValue;
import org.hl7.fhir.r4.model.DomainResource;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;

public class DomainResourceSerializer extends PrimitiveValueSerializer<DomainResourceValue> implements InitializingBean
{
	private final FhirContext fhirContext;

	public DomainResourceSerializer(FhirContext fhirContext)
	{
		super(DomainResourceValues.VALUE_TYPE);

		this.fhirContext = fhirContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(fhirContext, "fhirContext");
	}

	@Override
	public void writeValue(DomainResourceValue value, ValueFields valueFields)
	{
		DomainResource resource = value.getValue();
		try
		{
			if (resource != null)
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
				fhirContext.newJsonParser().encodeResourceToWriter(resource, writer);

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
	public DomainResourceValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return DomainResourceValues.create((DomainResource) untypedValue.getValue());
	}

	@Override
	public DomainResourceValue readValue(ValueFields valueFields)
	{
		String className = valueFields.getTextValue();
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			@SuppressWarnings("unchecked")
			Class<DomainResource> clazz = (Class<DomainResource>) Class.forName(className);
			DomainResource resource = fhirContext.newJsonParser().parseResource(clazz, new ByteArrayInputStream(bytes));

			return DomainResourceValues.create(resource);
		}
		catch (ClassNotFoundException | DataFormatException e)
		{
			throw new RuntimeException(e);
		}
	}
}
