package org.highmed.dsf.fhir.authorization.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;

public class ProcessAuthorizationHelperImpl implements ProcessAuthorizationHelper
{
	@Override
	public ActivityDefinition add(ActivityDefinition activityDefinition, String messageName, String taskProfile,
			Requester requester, Recipient recipient)
	{
		Objects.requireNonNull(activityDefinition, "activityDefinition");
		Objects.requireNonNull(messageName, "messageName");
		if (messageName.isBlank())
			throw new IllegalArgumentException("messageName blank");
		Objects.requireNonNull(taskProfile, "taskProfile");
		if (taskProfile.isBlank())
			throw new IllegalArgumentException("taskProfile blank");
		Objects.requireNonNull(requester, "requester");
		Objects.requireNonNull(recipient, "recipient");

		Extension extension = getExtensionByMessageNameAndTaskProfile(activityDefinition, messageName, taskProfile);
		if (!hasAuthorization(extension, requester))
			extension.addExtension(requester.toRequesterExtension());
		if (!hasAuthorization(extension, recipient))
			extension.addExtension(recipient.toRecipientExtension());

		return activityDefinition;
	}

	@Override
	public ActivityDefinition add(ActivityDefinition activityDefinition, String messageName, String taskProfile,
			Collection<? extends Requester> requesters, Collection<? extends Recipient> recipients)
	{
		Objects.requireNonNull(activityDefinition, "activityDefinition");
		Objects.requireNonNull(messageName, "messageName");
		if (messageName.isBlank())
			throw new IllegalArgumentException("messageName blank");
		Objects.requireNonNull(taskProfile, "taskProfile");
		if (taskProfile.isBlank())
			throw new IllegalArgumentException("taskProfile blank");
		Objects.requireNonNull(requesters, "requesters");
		if (requesters.isEmpty())
			throw new IllegalArgumentException("requesters empty");
		Objects.requireNonNull(recipients, "recipients");
		if (recipients.isEmpty())
			throw new IllegalArgumentException("recipients empty");

		Extension extension = getExtensionByMessageNameAndTaskProfile(activityDefinition, messageName, taskProfile);
		requesters.stream().filter(r -> !hasAuthorization(extension, r))
				.forEach(r -> extension.addExtension(r.toRequesterExtension()));
		recipients.stream().filter(r -> !hasAuthorization(extension, r))
				.forEach(r -> extension.addExtension(r.toRecipientExtension()));

		return activityDefinition;
	}

	private Extension getExtensionByMessageNameAndTaskProfile(ActivityDefinition a, String messageName,
			String taskProfile)
	{
		return a.getExtension().stream().filter(Extension::hasUrl)
				.filter(e -> ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION.equals(e.getUrl()))
				.filter(Extension::hasExtension)
				.filter(e -> hasMessageName(e, messageName) && hasTaskProfileExact(e, taskProfile)).findFirst()
				.orElseGet(() ->
				{
					Extension e = newExtension(messageName, taskProfile);
					a.addExtension(e);
					return e;
				});
	}

	private boolean hasMessageName(Extension processAuthorization, String messageName)
	{
		return processAuthorization.getExtension().stream().filter(Extension::hasUrl)
				.filter(e -> ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_MESSAGE_NAME.equals(e.getUrl()))
				.filter(Extension::hasValue).filter(e -> e.getValue() instanceof StringType)
				.map(e -> (StringType) e.getValue()).anyMatch(s -> messageName.equals(s.getValueAsString()));
	}

	private boolean hasTaskProfileExact(Extension processAuthorization, String taskProfile)
	{
		return processAuthorization.getExtension().stream().filter(Extension::hasUrl)
				.filter(e -> ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_TASK_PROFILE.equals(e.getUrl()))
				.filter(Extension::hasValue).filter(e -> e.getValue() instanceof CanonicalType)
				.map(e -> (CanonicalType) e.getValue()).anyMatch(c -> taskProfile.equals(c.getValueAsString()));
	}

	private Extension newExtension(String messageName, String taskProfile)
	{
		Extension e = new Extension(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION);
		e.addExtension(newMessageName(messageName));
		e.addExtension(newTaskProfile(taskProfile));

		return e;
	}

	private Extension newMessageName(String messageName)
	{
		return new Extension(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_MESSAGE_NAME)
				.setValue(new StringType(messageName));
	}

	private Extension newTaskProfile(String taskProfile)
	{
		return new Extension(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_TASK_PROFILE)
				.setValue(new CanonicalType(taskProfile));
	}

	private boolean hasAuthorization(Extension processAuthorization, Requester authorization)
	{
		return processAuthorization.getExtension().stream().anyMatch(authorization::requesterMatches);
	}

	private boolean hasAuthorization(Extension processAuthorization, Recipient authorization)
	{
		return processAuthorization.getExtension().stream().anyMatch(authorization::recipientMatches);
	}

	@Override
	public boolean isValid(ActivityDefinition activityDefinition, Predicate<CanonicalType> profileExists,
			Predicate<Identifier> organizationWithIdentifierExists, Predicate<Coding> roleExists)
	{
		if (activityDefinition == null)
			return false;

		List<Extension> processAuthorizations = activityDefinition.getExtension().stream().filter(Extension::hasUrl)
				.filter(e -> ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION.equals(e.getUrl()))
				.collect(Collectors.toList());

		if (processAuthorizations.isEmpty())
			return false;

		return processAuthorizations.stream()
				.map(e -> isProcessAuthorizationValid(e, profileExists, organizationWithIdentifierExists, roleExists))
				.allMatch(v -> v) && messageNamesUnique(processAuthorizations);
	}

	private boolean messageNamesUnique(List<Extension> processAuthorizations)
	{
		return processAuthorizations.size() == processAuthorizations.stream().flatMap(e -> e.getExtension().stream()
				.filter(mn -> EXTENSION_PROCESS_AUTHORIZATION_MESSAGE_NAME.equals(mn.getUrl())).map(Extension::getValue)
				.map(v -> (StringType) v).map(StringType::getValueAsString).findFirst().stream()).distinct().count();
	}

	private boolean isProcessAuthorizationValid(Extension processAuthorization, Predicate<CanonicalType> profileExists,
			Predicate<Identifier> organizationWithIdentifierExists, Predicate<Coding> roleExists)
	{
		if (processAuthorization == null
				|| !ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION.equals(processAuthorization.getUrl())
				|| !processAuthorization.hasExtension())
			return false;

		List<Extension> messageNames = new ArrayList<>(), taskProfiles = new ArrayList<>(),
				requesters = new ArrayList<>(), recipients = new ArrayList<>();
		for (Extension extension : processAuthorization.getExtension())
		{
			if (extension.hasUrl())
			{
				switch (extension.getUrl())
				{
					case EXTENSION_PROCESS_AUTHORIZATION_MESSAGE_NAME:
						messageNames.add(extension);
						break;
					case EXTENSION_PROCESS_AUTHORIZATION_TASK_PROFILE:
						taskProfiles.add(extension);
						break;
					case EXTENSION_PROCESS_AUTHORIZATION_REQUESTER:
						requesters.add(extension);
						break;
					case EXTENSION_PROCESS_AUTHORIZATION_RECIPIENT:
						recipients.add(extension);
						break;
				}
			}
		}

		if (messageNames.size() != 1 || taskProfiles.size() != 1 || requesters.isEmpty() || recipients.isEmpty())
			return false;

		return isMessageNameValid(messageNames.get(0)) && isTaskProfileValid(taskProfiles.get(0), profileExists)
				&& isRequestersValid(requesters, organizationWithIdentifierExists, roleExists)
				&& isRecipientsValid(recipients, organizationWithIdentifierExists, roleExists);
	}

	private boolean isMessageNameValid(Extension messageName)
	{
		if (messageName == null || !ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_MESSAGE_NAME
				.equals(messageName.getUrl()))
			return false;

		return messageName.hasValue() && messageName.getValue() instanceof StringType
				&& !((StringType) messageName.getValue()).getValueAsString().isBlank();
	}

	private boolean isTaskProfileValid(Extension taskProfile, Predicate<CanonicalType> profileExists)
	{
		if (taskProfile == null || !ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_TASK_PROFILE
				.equals(taskProfile.getUrl()))
			return false;

		return taskProfile.hasValue() && taskProfile.getValue() instanceof CanonicalType
				&& profileExists.test((CanonicalType) taskProfile.getValue());
	}

	private boolean isRequestersValid(List<Extension> requesters,
			Predicate<Identifier> organizationWithIdentifierExists, Predicate<Coding> roleExists)
	{
		return requesters.stream().allMatch(r -> isRequesterValid(r, organizationWithIdentifierExists, roleExists));
	}

	private boolean isRequesterValid(Extension requester, Predicate<Identifier> organizationWithIdentifierExists,
			Predicate<Coding> roleExists)
	{
		if (requester == null
				|| !ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_REQUESTER.equals(requester.getUrl()))
			return false;

		if (requester.hasValue() && requester.getValue() instanceof Coding)
		{
			return requesterFrom((Coding) requester.getValue(), organizationWithIdentifierExists, roleExists)
					.isPresent();
		}

		return false;
	}

	private Optional<Requester> requesterFrom(Coding coding, Predicate<Identifier> organizationWithIdentifierExists,
			Predicate<Coding> roleExists)
	{
		switch (coding.getCode())
		{
			case ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ALL:
			case ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ALL:
				return All.fromRequester(coding);

			case ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ORGANIZATION:
			case ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ORGANIZATION:
				return Organization.fromRequester(coding, organizationWithIdentifierExists);

			case ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ROLE:
			case ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ROLE:
				return Role.fromRequester(coding, organizationWithIdentifierExists, roleExists);
		}

		return Optional.empty();
	}

	private boolean isRecipientsValid(List<Extension> recipients,
			Predicate<Identifier> organizationWithIdentifierExists, Predicate<Coding> roleExists)
	{
		return recipients.stream().allMatch(r -> isRecipientValid(r, organizationWithIdentifierExists, roleExists));
	}

	private boolean isRecipientValid(Extension recipient, Predicate<Identifier> organizationWithIdentifierExists,
			Predicate<Coding> roleExists)
	{
		if (recipient == null
				|| !ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_RECIPIENT.equals(recipient.getUrl()))
			return false;

		if (recipient.hasValue() && recipient.getValue() instanceof Coding)
		{
			return recipientFrom((Coding) recipient.getValue(), organizationWithIdentifierExists, roleExists)
					.isPresent();
		}

		return false;
	}

	private Optional<Recipient> recipientFrom(Coding coding, Predicate<Identifier> organizationWithIdentifierExists,
			Predicate<Coding> roleExists)
	{
		switch (coding.getCode())
		{
			case ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ALL:
				return All.fromRecipient(coding);

			case ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ORGANIZATION:
				return Organization.fromRecipient(coding, organizationWithIdentifierExists);

			case ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ROLE:
				return Role.fromRecipient(coding, organizationWithIdentifierExists, roleExists);
		}

		return Optional.empty();
	}

	@Override
	public Stream<Requester> getRequesters(ActivityDefinition activityDefinition, String processUrl,
			String processVersion, String messageName, Collection<String> taskProfiles)
	{
		Optional<Extension> authorizationExtension = getAuthorizationExtension(activityDefinition, processUrl,
				processVersion, messageName, taskProfiles);

		if (authorizationExtension.isEmpty())
			return Stream.empty();
		else
			return authorizationExtension.get().getExtension().stream().filter(Extension::hasUrl)
					.filter(e -> ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_REQUESTER
							.equals(e.getUrl()))
					.filter(Extension::hasValue).filter(e -> e.getValue() instanceof Coding)
					.map(e -> (Coding) e.getValue())
					.flatMap(coding -> requesterFrom(coding, i -> true, c -> true).stream());
	}

	@Override
	public Stream<Recipient> getRecipients(ActivityDefinition activityDefinition, String processUrl,
			String processVersion, String messageName, Collection<String> taskProfiles)
	{
		Optional<Extension> authorizationExtension = getAuthorizationExtension(activityDefinition, processUrl,
				processVersion, messageName, taskProfiles);

		if (authorizationExtension.isEmpty())
			return Stream.empty();
		else
			return authorizationExtension.get().getExtension().stream().filter(Extension::hasUrl)
					.filter(e -> ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_RECIPIENT
							.equals(e.getUrl()))
					.filter(Extension::hasValue).filter(e -> e.getValue() instanceof Coding)
					.map(e -> (Coding) e.getValue())
					.flatMap(coding -> recipientFrom(coding, i -> true, c -> true).stream());
	}

	private Optional<Extension> getAuthorizationExtension(ActivityDefinition activityDefinition, String processUrl,
			String processVersion, String messageName, Collection<String> taskProfiles)
	{
		if (activityDefinition == null || processUrl == null || processUrl.isBlank() || processVersion == null
				|| processVersion.isBlank() || messageName == null || messageName.isBlank() || taskProfiles == null)
			return Optional.empty();

		if (!processUrl.equals(activityDefinition.getUrl()) || !processVersion.equals(activityDefinition.getVersion()))
			return Optional.empty();

		Optional<Extension> authorizationExtension = activityDefinition.getExtension().stream()
				.filter(Extension::hasUrl)
				.filter(e -> ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION.equals(e.getUrl()))
				.filter(Extension::hasExtension)
				.filter(e -> hasMessageName(e, messageName) && hasTaskProfile(e, taskProfiles)).findFirst();
		return authorizationExtension;
	}

	private boolean hasTaskProfile(Extension processAuthorization, Collection<String> taskProfiles)
	{
		return taskProfiles.stream()
				.anyMatch(taskProfile -> hasTaskProfileNotVersionSpecific(processAuthorization, taskProfile));
	}

	private boolean hasTaskProfileNotVersionSpecific(Extension processAuthorization, String taskProfile)
	{
		return processAuthorization.getExtension().stream().filter(Extension::hasUrl)
				.filter(e -> ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_TASK_PROFILE.equals(e.getUrl()))
				.filter(Extension::hasValue).filter(e -> e.getValue() instanceof CanonicalType)
				.map(e -> (CanonicalType) e.getValue())

				// match if task profile is equal to value in activity definition
				// or match if task profile is not version specific but value in activity definition is and non version
				// specific profiles are same -> client does not care about version of task resource, may result in
				// validation errors
				.anyMatch(c -> taskProfile.equals(c.getValueAsString())
						|| taskProfile.equals(getBase(c.getValueAsString())));
	}

	private static String getBase(String canonicalUrl)
	{
		if (canonicalUrl.contains("|"))
		{
			String[] split = canonicalUrl.split("\\|");
			return split[0];
		}
		else
			return canonicalUrl;
	}
}
