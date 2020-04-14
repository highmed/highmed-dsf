package org.highmed.pseudonymization.domain.json;

import java.io.IOException;

import org.hl7.fhir.r4.model.Resource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class ResourceDeserializer extends JsonDeserializer<Resource>
{
	private FhirContext fhirContext;

	public ResourceDeserializer(FhirContext fhirContext)
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
	public Resource deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException
	{
		JsonNode node = jsonParser.getCodec().readTree(jsonParser);
		return (Resource) getParser().parseResource(node.toString());
	}
}
