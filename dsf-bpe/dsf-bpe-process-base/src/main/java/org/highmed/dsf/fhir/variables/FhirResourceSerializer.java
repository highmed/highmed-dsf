package org.highmed.dsf.fhir.variables;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.fhir.variables.FhirResourceValues.FhirResourceValue;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

public class FhirResourceSerializer extends PrimitiveValueSerializer<FhirResourceValue> implements InitializingBean
{
	private final FhirContext fhirContext;

	public FhirResourceSerializer(FhirContext fhirContext)
	{
		super(FhirResourceValues.VALUE_TYPE);

		this.fhirContext = fhirContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(fhirContext, "fhirContext");
	}

	@Override
	public void writeValue(FhirResourceValue value, ValueFields valueFields)
	{
		Resource resource = value.getValue();
		try
		{
			if (resource != null)
			{
				String s = newJsonParser().encodeResourceToString(resource);
				valueFields.setTextValue(resource.getClass().getName());
				valueFields.setByteArrayValue(s.getBytes(StandardCharsets.UTF_8));
			}
		}
		catch (DataFormatException e)
		{
			throw new RuntimeException(e);
		}
	}

	private IParser newJsonParser()
	{
		IParser p = fhirContext.newJsonParser();
		p.setStripVersionsFromReferences(false);
		p.setOverrideResourceIdWithBundleEntryFullUrl(false);
		return p;
	}

	@Override
	public FhirResourceValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return FhirResourceValues.create((Resource) untypedValue.getValue());
	}

	@Override
	public FhirResourceValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		String className = valueFields.getTextValue();
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			@SuppressWarnings("unchecked")
			Class<Resource> clazz = (Class<Resource>) Class.forName(className);
			Resource resource = newJsonParser().parseResource(clazz, new ByteArrayInputStream(bytes));

			return FhirResourceValues.create(resource);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
}
