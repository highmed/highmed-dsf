package org.highmed.dsf.fhir.authorization;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.ActivityDefinition;

public class ActivityDefinitionProcessAuthorizationExtensions
{
	public static final String PROCESS_AUTHORIZATION_EXTENSION_URL = "http://highmed.org/fhir/StructureDefinition/process-authorization";

	private final ActivityDefinition activityDefinition;

	public ActivityDefinitionProcessAuthorizationExtensions(ActivityDefinition activityDefinition)
	{
		this.activityDefinition = activityDefinition;
	}

	public boolean isValid()
	{
		return doGetExtensions().map(ActivityDefinitionProcessAuthorizationExtension::isValid).count() >= 1;
	}

	public List<ActivityDefinitionProcessAuthorizationExtension> getExtensions()
	{
		return doGetExtensions().filter(ActivityDefinitionProcessAuthorizationExtension::isValid)
				.collect(Collectors.toList());
	}

	private Stream<ActivityDefinitionProcessAuthorizationExtension> doGetExtensions()
	{
		return activityDefinition.getExtensionsByUrl(PROCESS_AUTHORIZATION_EXTENSION_URL).stream()
				.map(ActivityDefinitionProcessAuthorizationExtension::new);
	}
}
