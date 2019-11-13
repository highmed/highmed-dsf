package org.highmed.dsf.fhir.variables;

import java.io.IOException;
import java.util.Objects;

import org.hl7.fhir.r4.model.Organization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ca.uhn.fhir.context.FhirContext;

public class OrganizationDeserializer extends JsonDeserializer<Organization>
{
	private final FhirContext fhirContext;

	public OrganizationDeserializer(FhirContext fhirContext)
	{
		this.fhirContext = Objects.requireNonNull(fhirContext, "fhirContext");
	}

	@Override
	public Organization deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException
	{
		String string = p.readValueAsTree().toString();
		return fhirContext.newJsonParser().parseResource(Organization.class, string);
	}
}
