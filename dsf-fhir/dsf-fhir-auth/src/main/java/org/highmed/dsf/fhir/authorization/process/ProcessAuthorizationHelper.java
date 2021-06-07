package org.highmed.dsf.fhir.authorization.process;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;

public interface ProcessAuthorizationHelper
{
	String PROCESS_AUTHORIZATION_SYSTEM = "http://highmed.org/fhir/CodeSystem/process-authorization";
	String PROCESS_AUTHORIZATION_VALUE_LOCAL_ORGANIZATION = "LOCAL_ORGANIZATION";
	String PROCESS_AUTHORIZATION_VALUE_REMOTE_ORGANIZATION = "REMOTE_ORGANIZATION";
	String PROCESS_AUTHORIZATION_VALUE_LOCAL_ROLE = "LOCAL_ROLE";
	String PROCESS_AUTHORIZATION_VALUE_REMOTE_ROLE = "REMOTE_ROLE";
	String PROCESS_AUTHORIZATION_VALUE_LOCAL_ALL = "LOCAL_ALL";
	String PROCESS_AUTHORIZATION_VALUE_REMOTE_ALL = "REMOTE_ALL";

	String ORGANIZATION_IDENTIFIER_SYSTEM = "http://highmed.org/sid/organization-identifier";

	String EXTENSION_PROCESS_AUTHORIZATION = "http://highmed.org/fhir/StructureDefinition/extension-process-authorization";
	String EXTENSION_PROCESS_AUTHORIZATION_MESSAGE_NAME = "message-name";
	String EXTENSION_PROCESS_AUTHORIZATION_TASK_PROFILE = "task-profile";
	String EXTENSION_PROCESS_AUTHORIZATION_REQUESTER = "requester";
	String EXTENSION_PROCESS_AUTHORIZATION_RECIPIENT = "recipient";

	String EXTENSION_PROCESS_AUTHORIZATION_ORGANIZATION = "http://highmed.org/fhir/StructureDefinition/extension-process-authorization-organization";

	String EXTENSION_PROCESS_AUTHORIZATION_CONSORTIUM_ROLE = "http://highmed.org/fhir/StructureDefinition/extension-process-authorization-consortium-role";
	String EXTENSION_PROCESS_AUTHORIZATION_CONSORTIUM_ROLE_CONSORTIUM = "consortium";
	String EXTENSION_PROCESS_AUTHORIZATION_CONSORTIUM_ROLE_ROLE = "role";

	ActivityDefinition add(ActivityDefinition activityDefinition, String messageName, String taskProfile,
			Requester requester, Recipient recipient);

	ActivityDefinition add(ActivityDefinition activityDefinition, String messageName, String taskProfile,
			Collection<? extends Requester> requesters, Collection<? extends Recipient> recipients);

	boolean isValid(ActivityDefinition activityDefinition, Predicate<CanonicalType> profileExists,
			Predicate<Identifier> organizationWithIdentifierExists, Predicate<Coding> roleExists);

	default Stream<Requester> getRequesters(ActivityDefinition activityDefinition, String processUrl,
			String processVersion, String messageName, String taskProfile)
	{
		return getRequesters(activityDefinition, processUrl, processVersion, messageName,
				Collections.singleton(taskProfile));
	}

	Stream<Requester> getRequesters(ActivityDefinition activityDefinition, String processUrl, String processVersion,
			String messageName, Collection<String> taskProfiles);

	default Stream<Recipient> getRecipients(ActivityDefinition activityDefinition, String processUrl,
			String processVersion, String messageName, String taskProfiles)
	{
		return getRecipients(activityDefinition, processUrl, processVersion, messageName,
				Collections.singleton(taskProfiles));
	}

	Stream<Recipient> getRecipients(ActivityDefinition activityDefinition, String processUrl, String processVersion,
			String messageName, Collection<String> taskProfiles);
}
