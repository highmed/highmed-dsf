package org.highmed.dsf.fhir.variables;

import java.io.IOException;
import java.util.Objects;

import org.hl7.fhir.r4.model.Resource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class FhirResourceJacksonDeserializer extends JsonDeserializer<Resource>
{
	private final FhirContext fhirContext;

	public FhirResourceJacksonDeserializer(FhirContext fhirContext)
	{
		this.fhirContext = Objects.requireNonNull(fhirContext, "fhirContext");
	}

	@Override
	public Resource deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		String string = p.readValueAsTree().toString();
		return (Resource) newJsonParser().parseResource(string);
	}

	private IParser newJsonParser()
	{
		IParser p = fhirContext.newJsonParser();
		p.setStripVersionsFromReferences(false);
		p.setOverrideResourceIdWithBundleEntryFullUrl(false);
		return p;
	}
}
