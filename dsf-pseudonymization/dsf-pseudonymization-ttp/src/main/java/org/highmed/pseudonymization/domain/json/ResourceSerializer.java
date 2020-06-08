package org.highmed.pseudonymization.domain.json;

import java.io.IOException;

import org.hl7.fhir.r4.model.Resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class ResourceSerializer extends JsonSerializer<Resource>
{
	private FhirContext fhirContext;

	public ResourceSerializer(FhirContext fhirContext)
	{
		this.fhirContext = fhirContext;
	}

	private IParser getParser()
	{
		IParser p = fhirContext.newJsonParser();
		p.setStripVersionsFromReferences(false);
		p.setOverrideResourceIdWithBundleEntryFullUrl(false);
		return p;
	}

	@Override
	public void serialize(Resource value, JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		String json = getParser().encodeResourceToString(value);
		gen.writeRawValue(json);
	}
}
