package org.highmed.dsf.tools.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
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

	public ValidationSupportWithCustomResources(FhirContext context, Bundle bundle)
	{
		this.context = context;

		bundle.getEntry().stream().map(e -> e.getResource())
				.filter(r -> r instanceof StructureDefinition || r instanceof CodeSystem || r instanceof ValueSet)
				.forEach(r ->
				{
					if (r instanceof StructureDefinition)
					{
						StructureDefinition sd = (StructureDefinition) r;

						structureDefinitionsByUrl.put(sd.getUrl(), sd);
						if (sd.hasVersion())
							structureDefinitionsByUrl.put(sd.getUrl() + "|" + sd.getVersion(), sd);
					}
					else if (r instanceof CodeSystem)
					{
						CodeSystem cs = (CodeSystem) r;
						codeSystemsByUrl.put(cs.getUrl(), cs);
						if (cs.hasVersion())
							codeSystemsByUrl.put(cs.getUrl() + "|" + cs.getVersion(), cs);
					}
					else if (r instanceof ValueSet)
					{
						ValueSet vs = (ValueSet) r;
						valueSetsByUrl.put(vs.getUrl(), vs);
						if (vs.hasVersion())
							valueSetsByUrl.put(vs.getUrl() + "|" + vs.getVersion(), vs);
					}
				});
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
