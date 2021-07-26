package org.highmed.dsf.fhir.authentication;

import org.hl7.fhir.r4.model.Organization;

public class User
{
	private final Organization organization;
	private final UserRole userRole;
	private final boolean deleteAllowed;
	private final String subjectDn;

	public User(Organization organization, UserRole userRole, String subjectDn)
	{
		this(organization, userRole, false, subjectDn);
	}

	public User(Organization organization, UserRole userRole, boolean deleteAllowed, String subjectDn)
	{
		if (!UserRole.LOCAL.equals(userRole) && deleteAllowed)
			throw new IllegalArgumentException("Users with UserRole != LOCAL cannot have delete privileges");

		this.organization = organization;
		this.userRole = userRole;
		this.deleteAllowed = deleteAllowed;
		this.subjectDn = subjectDn;
	}

	public UserRole getRole()
	{
		return userRole;
	}

	public boolean hasDeletePrivileges()
	{
		return deleteAllowed;
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
