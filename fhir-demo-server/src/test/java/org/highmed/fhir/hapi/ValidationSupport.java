package org.highmed.fhir.hapi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;

public class ValidationSupport extends DefaultProfileValidationSupport
{
	public static List<StructureDefinition> readStructureDefinitions(FhirContext context, Path... xmlPaths)
	{
		return Arrays.stream(xmlPaths).map(p -> read(context, p)).collect(Collectors.toList());
	}

	private static StructureDefinition read(FhirContext context, Path xmlPath)
	{
		try (InputStream in = Files.newInputStream(xmlPath))
		{
			return context.newXmlParser().parseResource(StructureDefinition.class, in);
		}
		catch (DataFormatException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private final Map<String, StructureDefinition> customStructureDefinitions = new HashMap<>();

	public ValidationSupport(FhirContext context, Collection<? extends StructureDefinition> customProfiles)
	{
		customProfiles.forEach(p -> this.customStructureDefinitions.put(p.getUrl(), p));
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
