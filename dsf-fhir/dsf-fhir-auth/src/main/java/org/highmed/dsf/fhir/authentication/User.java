package org.highmed.dsf.fhir.authentication;

import org.hl7.fhir.r4.model.Organization;

public class User
{
	private final Organization organization;
	private final UserRole userRole;
	private final String subjectDn;

	public User(Organization organization, UserRole userRole, String subjectDn)
	{
		this.organization = organization;
		this.userRole = userRole;
		this.subjectDn = subjectDn;
	}

	public UserRole getRole()
	{
		return userRole;
	}

	public Organization getOrganization()
	{
		return organization;
	}

	public String getSubjectDn()
	{
		return subjectDn;
	}

	public String getName()
	{
		return organization == null ? "null" : organization.getName();
	}
}
