package org.highmed.dsf.fhir.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class DefaultProfileValidationSupportWithCustomResources extends DefaultProfileValidationSupport
{
	private static final Logger logger = LoggerFactory
			.getLogger(DefaultProfileValidationSupportWithCustomResources.class);

	private final Map<String, StructureDefinition> structureDefinitionsByUrl = new HashMap<>();
	private final Map<String, CodeSystem> codeSystemsByUrl = new HashMap<>();
	private final Map<String, ValueSet> valueSetsByUrl = new HashMap<>();

	public DefaultProfileValidationSupportWithCustomResources()
	{
	}

	public DefaultProfileValidationSupportWithCustomResources(
			Collection<? extends StructureDefinition> structureDefinitions,
			Collection<? extends CodeSystem> codeSystems, Collection<? extends ValueSet> valueSets)
	{
		structureDefinitions.forEach(s -> structureDefinitionsByUrl.put(s.getUrl(), s));
		codeSystems.forEach(s -> codeSystemsByUrl.put(s.getUrl(), s));
		valueSets.forEach(s -> valueSetsByUrl.put(s.getUrl(), s));
	}

	@Override
	public List<StructureDefinition> fetchAllStructureDefinitions(FhirContext context)
	{
		logger.trace("Fetching all StructureDefinitions");

		var sD = new ArrayList<>(structureDefinitionsByUrl.values());
		sD.addAll(super.fetchAllStructureDefinitions(context));
		return sD;
	}

	@Override
	public StructureDefinition fetchStructureDefinition(FhirContext context, String url)
	{
		logger.trace("Fetching StructureDefinition by url: {}", url);

		var sD = structureDefinitionsByUrl.getOrDefault(url, null);

		if (sD != null)
			return sD;
		else
			return super.fetchStructureDefinition(context, url);
	}

	public void addOrReplaceStructureDefinition(StructureDefinition s)
	{
		structureDefinitionsByUrl.put(s.getUrl(), s);
	}

	@Override
	public CodeSystem fetchCodeSystem(FhirContext context, String url)
	{
		logger.trace("Fetching CodeSystem by url: {}", url);

		var cS = codeSystemsByUrl.getOrDefault(url, null);

		if (cS != null)
			return cS;
		else
			return super.fetchCodeSystem(context, url);
	}

	public void addOrReplaceCodeSystem(CodeSystem s)
	{
		codeSystemsByUrl.put(s.getUrl(), s);
	}

	@Override
	public ValueSet fetchValueSet(FhirContext context, String url)
	{
		logger.trace("Fetching ValueSet by url: {}", url);

		var vS = valueSetsByUrl.getOrDefault(url, null);

		if (vS != null)
			return vS;
		else
			return super.fetchValueSet(context, url);
	}

	public void addOrReplaceValueSet(ValueSet s)
	{
		valueSetsByUrl.put(s.getUrl(), s);
	}
}
