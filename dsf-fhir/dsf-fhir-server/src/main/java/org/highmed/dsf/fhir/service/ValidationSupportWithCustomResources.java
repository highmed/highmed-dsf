package org.highmed.dsf.fhir.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;

public class ValidationSupportWithCustomResources implements IValidationSupport
{
	private final FhirContext context;

	private final Map<String, StructureDefinition> structureDefinitionsByUrl = new HashMap<>();
	private final Map<String, CodeSystem> codeSystemsByUrl = new HashMap<>();
	private final Map<String, ValueSet> valueSetsByUrl = new HashMap<>();

	public ValidationSupportWithCustomResources(FhirContext context)
	{
		this(context, null, null, null);
	}

	public ValidationSupportWithCustomResources(FhirContext context,
			Collection<? extends StructureDefinition> structureDefinitions,
			Collection<? extends CodeSystem> codeSystems, Collection<? extends ValueSet> valueSets)
	{
		this.context = context;

		if (structureDefinitions != null)
			structureDefinitions.forEach(s -> structureDefinitionsByUrl.put(s.getUrl(), s));
		if (codeSystems != null)
			codeSystems.forEach(s -> codeSystemsByUrl.put(s.getUrl(), s));
		if (valueSets != null)
			valueSets.forEach(s -> valueSetsByUrl.put(s.getUrl(), s));
	}

	@Override
	public FhirContext getFhirContext()
	{
		return context;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<StructureDefinition> fetchAllStructureDefinitions()
	{
		return new ArrayList<>(structureDefinitionsByUrl.values());
	}

	@Override
	public StructureDefinition fetchStructureDefinition(String url)
	{
		return structureDefinitionsByUrl.getOrDefault(url, null);
	}

	public void addOrReplace(StructureDefinition s)
	{
		structureDefinitionsByUrl.put(s.getUrl(), s);
	}

	@Override
	public CodeSystem fetchCodeSystem(String url)
	{
		return codeSystemsByUrl.getOrDefault(url, null);
	}

	public void addOrReplace(CodeSystem s)
	{
		codeSystemsByUrl.put(s.getUrl(), s);
	}

	@Override
	public ValueSet fetchValueSet(String url)
	{
		return valueSetsByUrl.getOrDefault(url, null);
	}

	public void addOrReplace(ValueSet s)
	{
		valueSetsByUrl.put(s.getUrl(), s);
	}
}