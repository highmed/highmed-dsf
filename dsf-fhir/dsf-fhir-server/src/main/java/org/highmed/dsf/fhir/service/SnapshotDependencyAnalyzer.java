package org.highmed.dsf.fhir.service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotDependencyAnalyzer
{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotDependencyAnalyzer.class);

	public SnapshotDependencies analyzeSnapshotDependencies(StructureDefinition structureDefinition)
	{
		Objects.requireNonNull(structureDefinition, "structureDefinition");
		if (!structureDefinition.hasSnapshot())
			throw new IllegalArgumentException("StructureDefinition with snapshot expected");

		logger.debug("Analyzing profile dependencies of StructureDefinition if id {} and url {}",
				structureDefinition.getIdElement().getIdPart(), structureDefinition.getUrl());

		Set<String> profiles = new HashSet<>(), targetProfiles = new HashSet<>();
		for (ElementDefinition element : structureDefinition.getSnapshot().getElement())
		{
			if (element.getType().stream().filter(t -> !t.getProfile().isEmpty() || !t.getTargetProfile().isEmpty())
					.findAny().isPresent())
			{
				for (TypeRefComponent type : element.getType())
				{
					for (CanonicalType profile : type.getProfile())
						profiles.add(profile.getValue());

					for (CanonicalType targetProfile : type.getTargetProfile())
						targetProfiles.add(targetProfile.getValue());
				}
			}
		}

		List<String> profilesList = profiles.stream().sorted().collect(Collectors.toList());
		logger.debug("Profile dependencies: {}", targetProfiles);
		List<String> targetProfilesList = targetProfiles.stream().sorted().collect(Collectors.toList());
		logger.debug("TargetProfile dependencies: {}", targetProfilesList);

		return new SnapshotDependencies(profilesList, targetProfilesList);
	}
}
