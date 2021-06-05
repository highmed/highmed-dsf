package org.highmed.dsf.fhir.authorization.process;

import java.util.Collection;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.OrganizationAffiliation;

public interface Requester extends WithAuthorization
{
	static Requester localAll()
	{
		return new All(UserRole.LOCAL);
	}

	static Requester remoteAll()
	{
		return new All(UserRole.REMOTE);
	}

	static Requester all(UserRole role)
	{
		return new All(role);
	}

	static Requester localOrganization(String organizationIdentifier)
	{
		return new Organization(UserRole.LOCAL, organizationIdentifier);
	}

	static Requester remoteOrganization(String organizationIdentifier)
	{
		return new Organization(UserRole.REMOTE, organizationIdentifier);
	}

	static Requester organization(UserRole role, String organizationIdentifier)
	{
		return new Organization(role, organizationIdentifier);
	}

	static Requester localRole(String consortiumIdentifier, String roleSystem, String roleCode)
	{
		return role(UserRole.LOCAL, consortiumIdentifier, roleSystem, roleCode);
	}

	static Requester remoteRole(String consortiumIdentifier, String roleSystem, String roleCode)
	{
		return role(UserRole.REMOTE, consortiumIdentifier, roleSystem, roleCode);
	}

	static Requester role(UserRole role, String consortiumIdentifier, String roleSystem, String roleCode)
	{
		return new Role(role, consortiumIdentifier, roleSystem, roleCode);
	}

	boolean requesterMatches(Extension requesterExtension);

	boolean isRequesterAuthorized(User requesterUser, Stream<OrganizationAffiliation> requesterAffiliations);

	default boolean isRequesterAuthorized(User requesterUser, Collection<OrganizationAffiliation> requesterAffiliations)
	{
		return isRequesterAuthorized(requesterUser,
				requesterAffiliations == null ? null : requesterAffiliations.stream());
	}

	default Extension toRequesterExtension()
	{
		return new Extension().setUrl(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_REQUESTER)
				.setValue(getProcessAuthorizationCode());
	}
}
