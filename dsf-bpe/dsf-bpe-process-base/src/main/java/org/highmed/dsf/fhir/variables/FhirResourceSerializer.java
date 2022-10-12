package org.highmed.dsf.fhir.variables;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.fhir.variables.FhirResourceValues.FhirResourceValue;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

public class FhirResourceSerializer extends PrimitiveValueSerializer<FhirResourceValue> implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(FhirResourceSerializer.class);

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
			Resource resource;
			if (className != null)
			{
				@SuppressWarnings("unchecked")
				Class<Resource> clazz = (Class<Resource>) Class.forName(className);
				resource = newJsonParser().parseResource(clazz, new ByteArrayInputStream(bytes));
			}
			else
			{
				logger.warn("ClassName from DB null, trying to parse FHIR resource without type information");
				resource = (Resource) newJsonParser().parseResource(new ByteArrayInputStream(bytes));
			}

			return FhirResourceValues.create(resource);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
}
