package org.highmed.dsf.fhir.authentication;

import org.highmed.dsf.fhir.OrganizationType;
import org.hl7.fhir.r4.model.Organization;

public class User
{
	private final Organization organization;
	private final UserRole userRole;

	public User(Organization organization, UserRole userRole)
	{
		this.organization = organization;
		this.userRole = userRole;
	}

	public UserRole getRole()
	{
		return userRole;
	}

	public Organization getOrganization()
	{
		return organization;
	}

	public OrganizationType getOrganizationType()
	{
		return OrganizationType.fromString(organization.getTypeFirstRep().getCodingFirstRep().getCode()).orElseThrow();
	}

	public String getName()
	{
		return organization == null ? "null" : organization.getName();
	}
}
