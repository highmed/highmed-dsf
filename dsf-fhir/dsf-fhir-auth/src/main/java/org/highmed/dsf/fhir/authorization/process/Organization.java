package org.highmed.dsf.fhir.authorization.process;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Reference;

public class Organization implements Recipient, Requester
{
	private final String organizationIdentifier;
	private final UserRole role;

	public Organization(UserRole role, String organizationIdentifier)
	{
		Objects.requireNonNull(role, "role");
		Objects.requireNonNull(organizationIdentifier, "organizationIdentifier");
		if (organizationIdentifier.isBlank())
			throw new IllegalArgumentException("organizationIdentifier blank");

		this.role = role;
		this.organizationIdentifier = organizationIdentifier;
	}

	@Override
	public boolean isRequesterAuthorized(User requesterUser, Stream<OrganizationAffiliation> requesterAffiliations)
	{
		return isAuthorized(requesterUser);
	}

	@Override
	public boolean isRecipientAuthorized(User recipientUser, Stream<OrganizationAffiliation> recipientAffiliations)
	{
		return isAuthorized(recipientUser);
	}

	private boolean isAuthorized(User user)
	{
		return user != null && role.equals(user.getRole()) && user.getOrganization() != null
				&& user.getOrganization().getActive() && hasOrganizationIdentifier(user.getOrganization());
	}

	private boolean hasOrganizationIdentifier(org.hl7.fhir.r4.model.Organization organization)
	{
		return organization.getIdentifier().stream().filter(Identifier::hasSystem).filter(Identifier::hasValue)
				.filter(i -> ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM.equals(i.getSystem()))
				.anyMatch(i -> organizationIdentifier.equals(i.getValue()));
	}

	@Override
	public Extension toRecipientExtension()
	{
		return new Extension().setUrl(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_RECIPIENT)
				.setValue(toCoding());
	}

	@Override
	public Extension toRequesterExtension()
	{
		return new Extension().setUrl(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_REQUESTER)
				.setValue(toCoding());
	}

	private Coding toCoding()
	{
		Identifier organization = new Reference().getIdentifier()
				.setSystem(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM).setValue(organizationIdentifier);

		Coding coding = getProcessAuthorizationCode();
		coding.addExtension().setUrl(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_ORGANIZATION)
				.setValue(organization);
		return coding;
	}

	@Override
	public boolean requesterMatches(Extension requesterExtension)
	{
		return matches(requesterExtension, ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_REQUESTER);
	}

	@Override
	public boolean recipientMatches(Extension recipientExtension)
	{
		return matches(recipientExtension, ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_RECIPIENT);
	}

	private boolean matches(Extension recipientExtension, String url)
	{
		return recipientExtension != null && url.equals(recipientExtension.getUrl()) && recipientExtension.hasValue()
				&& recipientExtension.getValue() instanceof Coding && matches((Coding) recipientExtension.getValue())
				&& recipientExtension.getValue().hasExtension()
				&& hasMatchingOrganizationExtension(recipientExtension.getValue().getExtension());
	}

	private boolean hasMatchingOrganizationExtension(List<Extension> extensions)
	{
		return extensions.stream().anyMatch(this::organizationExtensionMatches);
	}

	private boolean organizationExtensionMatches(Extension extension)
	{
		return ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_ORGANIZATION.equals(extension.getUrl())
				&& extension.hasValue() && extension.getValue() instanceof Identifier
				&& organizationIdentifierMatches((Identifier) extension.getValue());
	}

	private boolean organizationIdentifierMatches(Identifier identifier)
	{
		return identifier != null && identifier.hasSystem() && identifier.hasValue()
				&& ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM.equals(identifier.getSystem())
				&& organizationIdentifier.equals(identifier.getValue());
	}

	@Override
	public Coding getProcessAuthorizationCode()
	{
		switch (role)
		{
			case LOCAL:
				return new Coding(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM,
						ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ORGANIZATION, null);
			case REMOTE:
				return new Coding(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM,
						ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ORGANIZATION, null);
			default:
				throw new IllegalStateException(UserRole.class.getName() + " " + role + " not supported");
		}
	}

	@Override
	public boolean matches(Coding processAuthorizationCode)
	{
		switch (role)
		{
			case LOCAL:
				return processAuthorizationCode != null
						&& ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM
								.equals(processAuthorizationCode.getSystem())
						&& ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ORGANIZATION
								.equals(processAuthorizationCode.getCode());
			case REMOTE:
				return processAuthorizationCode != null
						&& ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM
								.equals(processAuthorizationCode.getSystem())
						&& ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ORGANIZATION
								.equals(processAuthorizationCode.getCode());
			default:
				throw new IllegalStateException(UserRole.class.getName() + " " + role + " not supported");
		}
	}

	@SuppressWarnings("unchecked")
	public static Optional<Requester> fromRequester(Coding coding,
			Predicate<Identifier> organizationWithIdentifierExists)
	{
		return (Optional<Requester>) from(coding, organizationWithIdentifierExists);
	}

	@SuppressWarnings("unchecked")
	public static Optional<Recipient> fromRecipient(Coding coding,
			Predicate<Identifier> organizationWithIdentifierExists)
	{
		return (Optional<Recipient>) from(coding, organizationWithIdentifierExists);
	}

	private static Optional<? super Organization> from(Coding coding,
			Predicate<Identifier> organizationWithIdentifierExists)
	{
		if (coding != null && coding.hasSystem()
				&& ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM.equals(coding.getSystem())
				&& coding.hasCode())
		{
			if (ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ORGANIZATION.equals(coding.getCode()))
				return from(UserRole.LOCAL, coding, organizationWithIdentifierExists);
			else if (ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ORGANIZATION
					.equals(coding.getCode()))
				return from(UserRole.REMOTE, coding, organizationWithIdentifierExists);
		}

		return Optional.empty();
	}

	private static Optional<? super Organization> from(UserRole userRole, Coding coding,
			Predicate<Identifier> organizationWithIdentifierExists)
	{
		if (coding != null && coding.hasExtension())
		{
			List<Extension> organizations = coding.getExtension().stream().filter(Extension::hasUrl).filter(
					e -> ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_ORGANIZATION.equals(e.getUrl()))
					.collect(Collectors.toList());
			if (organizations.size() == 1)
			{
				Extension organization = organizations.get(0);
				if (organization.hasValue() && organization.getValue() instanceof Identifier)
				{
					Identifier identifier = (Identifier) organization.getValue();
					if (ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM.equals(identifier.getSystem())
							&& organizationWithIdentifierExists.test(identifier))
						return Optional.of(new Organization(userRole, identifier.getValue()));
				}
			}
		}

		return Optional.empty();
	}
}