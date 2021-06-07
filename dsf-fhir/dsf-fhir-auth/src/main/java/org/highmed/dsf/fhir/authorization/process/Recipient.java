package org.highmed.dsf.fhir.authorization.process;

import java.util.Collection;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.OrganizationAffiliation;

public interface Recipient extends WithAuthorization
{
	static Recipient localAll()
	{
		return new All(UserRole.LOCAL);
	}

	static Recipient localOrganization(String organizationIdentifier)
	{
		return new Organization(UserRole.LOCAL, organizationIdentifier);
	}

	static Recipient localRole(String consortiumIdentifier, String roleSystem, String roleCode)
	{
		return new Role(UserRole.LOCAL, consortiumIdentifier, roleSystem, roleCode);
	}

	boolean recipientMatches(Extension recipientExtension);

	boolean isRecipientAuthorized(User recipientUser, Stream<OrganizationAffiliation> recipientAffiliations);

	default boolean isRecipientAuthorized(User recipientUser, Collection<OrganizationAffiliation> recipientAffiliations)
	{
		return isRecipientAuthorized(recipientUser,
				recipientAffiliations == null ? null : recipientAffiliations.stream());
	}

	default Extension toRecipientExtension()
	{
		return new Extension().setUrl(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_RECIPIENT)
				.setValue(getProcessAuthorizationCode());
	}
}
