package org.highmed.dsf.fhir.authorization.process;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.OrganizationAffiliation;

public class All implements Recipient, Requester
{
	private final UserRole role;

	public All(UserRole role)
	{
		Objects.requireNonNull(role, "role");

		this.role = role;
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
		return user != null && user.getOrganization() != null && user.getOrganization().getActive()
				&& role.equals(user.getRole());
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
				&& recipientExtension.getValue() instanceof Coding && matches((Coding) recipientExtension.getValue());
	}

	@Override
	public Coding getProcessAuthorizationCode()
	{
		switch (role)
		{
			case LOCAL:
				return new Coding(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM,
						ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ALL, null);
			case REMOTE:
				return new Coding(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM,
						ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ALL, null);
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
						&& ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ALL
								.equals(processAuthorizationCode.getCode());
			case REMOTE:
				return processAuthorizationCode != null
						&& ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM
								.equals(processAuthorizationCode.getSystem())
						&& ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ALL
								.equals(processAuthorizationCode.getCode());
			default:
				throw new IllegalStateException(UserRole.class.getName() + " " + role + " not supported");
		}
	}

	public static Optional<Requester> fromRequester(Coding coding)
	{
		if (coding != null && coding.hasSystem()
				&& ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM.equals(coding.getSystem())
				&& coding.hasCode())
		{
			if (ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ALL.equals(coding.getCode()))
				return Optional.of(new All(UserRole.LOCAL));
			else if (ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ALL.equals(coding.getCode()))
				return Optional.of(new All(UserRole.REMOTE));
		}

		return Optional.empty();
	}

	public static Optional<Recipient> fromRecipient(Coding coding)
	{
		if (coding != null && coding.hasSystem()
				&& ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM.equals(coding.getSystem())
				&& coding.hasCode())
		{
			if (ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ALL.equals(coding.getCode()))
				return Optional.of(new All(UserRole.LOCAL));
			// remote not allowed for recipient
		}

		return Optional.empty();
	}
}