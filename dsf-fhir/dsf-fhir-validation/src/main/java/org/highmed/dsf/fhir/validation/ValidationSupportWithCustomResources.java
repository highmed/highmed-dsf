package org.highmed.dsf.fhir.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;

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
			structureDefinitions.forEach(this::addOrReplace);
		if (codeSystems != null)
			codeSystems.forEach(this::addOrReplace);
		if (valueSets != null)
			valueSets.forEach(this::addOrReplace);
	}

	@Override
	public FhirContext getFhirContext()
	{
		return context;
	}

	@Override
	public List<IBaseResource> fetchAllConformanceResources()
	{
		return Stream
				.concat(codeSystemsByUrl.values().stream(),
						Stream.concat(fetchAllStructureDefinitions().stream(), valueSetsByUrl.values().stream()))
				.collect(Collectors.toList());
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
		structureDefinitionsByUrl.put(s.getUrl() + "|" + s.getVersion(), s);
	}

	@Override
	public CodeSystem fetchCodeSystem(String url)
	{
		return codeSystemsByUrl.getOrDefault(url, null);
	}

	@Override
	public boolean isCodeSystemSupported(ValidationSupportContext theRootValidationSupport, String url)
	{
		return codeSystemsByUrl.containsKey(url);
	}

	public void addOrReplace(CodeSystem s)
	{
		codeSystemsByUrl.put(s.getUrl(), s);
		codeSystemsByUrl.put(s.getUrl() + "|" + s.getVersion(), s);
	}

	@Override
	public ValueSet fetchValueSet(String url)
	{
		return valueSetsByUrl.getOrDefault(url, null);
	}

	@Override
	public boolean isValueSetSupported(ValidationSupportContext theRootValidationSupport, String url)
	{
		return valueSetsByUrl.containsKey(url);
	}

	public void addOrReplace(ValueSet s)
	{
		valueSetsByUrl.put(s.getUrl(), s);
		valueSetsByUrl.put(s.getUrl() + "|" + s.getVersion(), s);
	}
}
