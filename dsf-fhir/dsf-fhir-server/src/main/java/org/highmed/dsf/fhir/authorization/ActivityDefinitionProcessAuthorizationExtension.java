package org.highmed.dsf.fhir.authorization;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.OrganizationType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;

public class ActivityDefinitionProcessAuthorizationExtension
{
	public static final String MESSAGE_NAME_EXTENSION_URL = "message-name";
	public static final String AUTHORIZATION_ROLE_EXTENSION_URL = "authorization-role";
	public static final String ORGANIZATION_TYPES_EXTENSION_URL = "organization-types";
	public static final String ORGANIZATION_TYPE_EXTENSION_URL = "organization-type";
	public static final String TASK_PROFILE_EXTENSION_URL = "task-profile";

	private final Extension processAuthorizationExtension;

	public ActivityDefinitionProcessAuthorizationExtension(Extension processAuthorizationExtension)
	{
		this.processAuthorizationExtension = processAuthorizationExtension;
	}

	public boolean isValid()
	{
		return doGetMessageName().isPresent() && doGetAuthorizationRole().isPresent()
				&& (getOrganizationTypes().size() == 1 || (getOrganizationTypes().size() == 2
						&& !getOrganizationTypes().get(0).equals(getOrganizationTypes().get(1))))
				&& doGetTaskProfile().isPresent();
	}

	public String getMessageName()
	{
		return doGetMessageName().orElseThrow();
	}

	private Optional<String> doGetMessageName()
	{
		return Optional.ofNullable(processAuthorizationExtension.getExtensionByUrl(MESSAGE_NAME_EXTENSION_URL))
				.map(e -> e.getValue()).filter(t -> t instanceof StringType).map(t -> ((StringType) t).getValue());
	}

	public AuthorizationRole getAuthorizationRole()
	{
		return doGetAuthorizationRole().orElseThrow();
	}

	private Optional<AuthorizationRole> doGetAuthorizationRole()
	{
		return Optional.ofNullable(processAuthorizationExtension.getExtensionByUrl(AUTHORIZATION_ROLE_EXTENSION_URL))
				.map(e -> e.getValue()).filter(t -> t instanceof Coding).map(t -> ((Coding) t).getCode())
				.flatMap(AuthorizationRole::fromString);
	}

	public List<OrganizationType> getOrganizationTypes()
	{
		return Optional.ofNullable(processAuthorizationExtension.getExtensionByUrl(ORGANIZATION_TYPES_EXTENSION_URL))
				.map(e -> e.getExtensionsByUrl(ORGANIZATION_TYPE_EXTENSION_URL).stream()
						.flatMap(this::getOrganizationType).collect(Collectors.toList()))
				.orElseGet(Collections::emptyList);
	}

	private Stream<OrganizationType> getOrganizationType(Extension organizationType)
	{
		return Optional.ofNullable(organizationType).map(ex -> ex.getValue()).filter(t -> t instanceof Coding)
				.map(t -> ((Coding) t).getCode()).flatMap(OrganizationType::fromString).stream();
	}

	public String getTaskProfile()
	{
		return doGetTaskProfile().orElseThrow();
	}

	private Optional<String> doGetTaskProfile()
	{
		Optional<String> taskProfile = Optional.ofNullable(processAuthorizationExtension.getExtensionByUrl(TASK_PROFILE_EXTENSION_URL))
				.map(e -> e.getValue()).filter(t -> t instanceof CanonicalType)
				.map(t -> ((CanonicalType) t).getValue());
		return taskProfile;
	}
}
