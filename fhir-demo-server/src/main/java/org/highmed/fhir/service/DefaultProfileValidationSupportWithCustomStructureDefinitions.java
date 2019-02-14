package org.highmed.fhir.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;

public class DefaultProfileValidationSupportWithCustomStructureDefinitions extends DefaultProfileValidationSupport
{
	private final Map<String, StructureDefinition> customStructureDefinitions = new HashMap<>();

	public DefaultProfileValidationSupportWithCustomStructureDefinitions(FhirContext context,
			StructureDefinition... customStructureDefinition)
	{
		Arrays.asList(customStructureDefinition).forEach(p -> this.customStructureDefinitions.put(p.getUrl(), p));
	}

	@Override
	public List<StructureDefinition> fetchAllStructureDefinitions(FhirContext theContext)
	{
		ArrayList<StructureDefinition> structureDefinitions = new ArrayList<>(
				super.fetchAllStructureDefinitions(theContext));
		structureDefinitions.addAll(customStructureDefinitions.values());

		return structureDefinitions;
	}

	@Override
	public StructureDefinition fetchStructureDefinition(FhirContext theContext, String theUrl)
	{
		StructureDefinition structureDefinition = super.fetchStructureDefinition(theContext, theUrl);

		if (structureDefinition != null)
			return structureDefinition;
		else
			return customStructureDefinitions.getOrDefault(theUrl, null);
	}
}
