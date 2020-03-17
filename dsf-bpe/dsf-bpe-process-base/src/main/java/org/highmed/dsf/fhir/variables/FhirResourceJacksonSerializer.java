package org.highmed.dsf.fhir.variables;

import java.io.IOException;
import java.util.Objects;

import org.hl7.fhir.r4.model.Resource;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class FhirResourceJacksonSerializer extends JsonSerializer<Resource>
{
	private final FhirContext fhirContext;

	public FhirResourceJacksonSerializer(FhirContext fhirContext)
	{
		this.fhirContext = Objects.requireNonNull(fhirContext, "fhirContext");
	}

	@Override
	public void serialize(Resource value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonGenerationException
	{
		String text = newJsonParser().encodeResourceToString(value);
		jgen.writeRawValue(text);
	}

	private IParser newJsonParser()
	{
		IParser p = fhirContext.newJsonParser();
		p.setStripVersionsFromReferences(false);
		p.setOverrideResourceIdWithBundleEntryFullUrl(false);
		return p;
	}
}
