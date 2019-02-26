package org.highmed.fhir.authentication;

import org.hl7.fhir.r4.model.Organization;

public class User
{
	private final Organization organization;

	public User(Organization organization)
	{
		this.organization = organization;
	}

	public UserRole getRole()
	{
		return UserRole.UNKNOWN; // TODO
	}

	public String getName()
	{
		return organization.getName();
	}
}
