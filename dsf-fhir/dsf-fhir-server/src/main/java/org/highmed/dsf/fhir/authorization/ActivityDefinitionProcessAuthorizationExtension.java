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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityDefinitionProcessAuthorizationExtension
{
	private static final Logger logger = LoggerFactory.getLogger(ActivityDefinitionProcessAuthorizationExtension.class);

	public static final String MESSAGE_NAME_EXTENSION_URL = "message-name";
	public static final String AUTHORIZATION_ROLES_EXTENSION_URL = "authorization-roles";
	public static final String AUTHORIZATION_ROLE_EXTENSION_URL = "authorization-role";
	public static final String REQUESTER_ORGANIZATION_TYPES_EXTENSION_URL = "requester-organization-types";
	public static final String REQUESTER_ORGANIZATION_TYPE_EXTENSION_URL = "requester-organization-type";
	public static final String RECIPIENT_ORGANIZATION_TYPES_EXTENSION_URL = "recipient-organization-types";
	public static final String RECIPIENT_ORGANIZATION_TYPE_EXTENSION_URL = "recipient-organization-type";
	public static final String TASK_PROFILE_EXTENSION_URL = "task-profile";

	private final Extension processAuthorizationExtension;

	public ActivityDefinitionProcessAuthorizationExtension(Extension processAuthorizationExtension)
	{
		this.processAuthorizationExtension = processAuthorizationExtension;
	}

	public boolean isValid()
	{
		boolean valid = doGetMessageName().isPresent()
				&& (getAuthorizationRoles().size() == 1 || (getAuthorizationRoles().size() == 2
						&& !getAuthorizationRoles().get(0).equals(getAuthorizationRoles().get(1))))
				&& (getRequesterOrganizationTypes().size() == 1 || (getRequesterOrganizationTypes().size() == 2
						&& !getRequesterOrganizationTypes().get(0).equals(getRequesterOrganizationTypes().get(1))))
				&& (getRecipientOrganizationTypes().size() == 1 || (getRecipientOrganizationTypes().size() == 2
						&& !getRecipientOrganizationTypes().get(0).equals(getRecipientOrganizationTypes().get(1))))
				&& doGetTaskProfile().isPresent();

		if (!valid)
			logger.warn("Not valid: {}", this);

		return valid;
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

	public List<AuthorizationRole> getAuthorizationRoles()
	{
		return Optional.ofNullable(processAuthorizationExtension.getExtensionByUrl(AUTHORIZATION_ROLES_EXTENSION_URL))
				.map(e -> e.getExtensionsByUrl(AUTHORIZATION_ROLE_EXTENSION_URL).stream()
						.flatMap(this::getAuthorizationRole).collect(Collectors.toList()))
				.orElseGet(Collections::emptyList);
	}

	private Stream<AuthorizationRole> getAuthorizationRole(Extension authorizationRole)
	{
		return Optional.ofNullable(authorizationRole).map(ex -> ex.getValue()).filter(t -> t instanceof Coding)
				.map(t -> ((Coding) t).getCode()).flatMap(AuthorizationRole::fromString).stream();
	}

	public List<OrganizationType> getRequesterOrganizationTypes()
	{
		return Optional
				.ofNullable(processAuthorizationExtension.getExtensionByUrl(REQUESTER_ORGANIZATION_TYPES_EXTENSION_URL))
				.map(e -> e.getExtensionsByUrl(REQUESTER_ORGANIZATION_TYPE_EXTENSION_URL).stream()
						.flatMap(this::getOrganizationType).collect(Collectors.toList()))
				.orElseGet(Collections::emptyList);
	}

	public List<OrganizationType> getRecipientOrganizationTypes()
	{
		return Optional
				.ofNullable(processAuthorizationExtension.getExtensionByUrl(RECIPIENT_ORGANIZATION_TYPES_EXTENSION_URL))
				.map(e -> e.getExtensionsByUrl(RECIPIENT_ORGANIZATION_TYPE_EXTENSION_URL).stream()
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
		Optional<String> taskProfile = Optional
				.ofNullable(processAuthorizationExtension.getExtensionByUrl(TASK_PROFILE_EXTENSION_URL))
				.map(e -> e.getValue()).filter(t -> t instanceof CanonicalType)
				.map(t -> ((CanonicalType) t).getValue());
		return taskProfile;
	}

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();
		b.append(MESSAGE_NAME_EXTENSION_URL).append(": ").append(doGetMessageName().orElse(""));
		b.append(", ").append(AUTHORIZATION_ROLES_EXTENSION_URL).append(": ").append(getAuthorizationRoles());
		b.append(", ").append(REQUESTER_ORGANIZATION_TYPES_EXTENSION_URL).append(": ")
				.append(getRequesterOrganizationTypes());
		b.append(", ").append(RECIPIENT_ORGANIZATION_TYPES_EXTENSION_URL).append(": ")
				.append(getRecipientOrganizationTypes());
		b.append(", ").append(TASK_PROFILE_EXTENSION_URL).append(": ").append(doGetTaskProfile().orElse(""));
		return b.toString();
	}
}
