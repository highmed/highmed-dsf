package org.highmed.dsf.fhir.variables;

import java.io.IOException;
import java.util.Objects;

import org.hl7.fhir.r4.model.Organization;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import ca.uhn.fhir.context.FhirContext;

public class OrganizationSerializer extends JsonSerializer<Organization>
{
	private final FhirContext fhirContext;

	public OrganizationSerializer(FhirContext fhirContext)
	{
		this.fhirContext = Objects.requireNonNull(fhirContext, "fhirContext");
	}

	@Override
	public void serialize(Organization value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonGenerationException
	{
		String text = fhirContext.newJsonParser().encodeResourceToString(value);
		jgen.writeRawValue(text);
	}
}
