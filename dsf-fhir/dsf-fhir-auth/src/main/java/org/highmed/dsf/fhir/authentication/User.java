package org.highmed.dsf.fhir.authentication;

import java.util.function.Function;

import org.hl7.fhir.r4.model.Organization;

public class User
{
	/**
	 * @param organization
	 *            the users {@link Organization}
	 * @return {@link User} based on the given <b>organization</b> with role {@link UserRole#LOCAL},
	 *         <code>permanentDeleteAllowed = false</code> and <code>subjectDn = "local"</code>
	 */
	public static User local(Organization organization)
	{
		return new User(organization, UserRole.LOCAL, false, "local");
	}

	/**
	 * @return Function to create {@link User} based on the applied {@link Organization} with role
	 *         {@link UserRole#LOCAL}, <code>permanentDeleteAllowed = false</code> and <code>subjectDn = "local"</code>
	 */
	public static Function<Organization, User> local()
	{
		return User::local;
	}

	/**
	 * @param permanentDeleteAllowed
	 *            <code>true</code> if permanent delete allowed
	 * @param subjectDn
	 *            not <code>null</code>
	 * @return Function to create {@link User} based on the applied {@link Organization} with role
	 *         {@link UserRole#LOCAL} and the given <b>permanentDeleteAllowed</b> and <b>subjectDn</b>
	 */
	public static Function<Organization, User> local(boolean permanentDeleteAllowed, String subjectDn)
	{
		return organization -> new User(organization, UserRole.LOCAL, permanentDeleteAllowed, subjectDn);
	}

	/**
	 * @param organization
	 *            the users {@link Organization}
	 * @return {@link User} based on the given <b>organization</b> with role {@link UserRole#REMOTE},
	 *         <code>permanentDeleteAllowed = false</code> and <code>subjectDn = "remote"</code>
	 */
	public static User remote(Organization organization)
	{
		return new User(organization, UserRole.REMOTE, false, "remote");
	}

	/**
	 * @param subjectDn
	 *            not <code>null</code>
	 * @return Function to create {@link User} based on the applied {@link Organization} with role
	 *         {@link UserRole#REMOTE}, <code>permanentDeleteAllowed = false</code> and the given <b>subjectDn</b>
	 */
	public static Function<Organization, User> remote(String subjectDn)
	{
		return organization -> new User(organization, UserRole.REMOTE, false, subjectDn);
	}

	private final Organization organization;
	private final UserRole userRole;
	private final boolean permanentDeleteAllowed;
	private final String subjectDn;

	private User(Organization organization, UserRole userRole, boolean permanentDeleteAllowed, String subjectDn)
	{
		if (!UserRole.LOCAL.equals(userRole) && permanentDeleteAllowed)
			throw new IllegalArgumentException("Users with UserRole != LOCAL cannot have delete privileges");

		this.organization = organization;
		this.userRole = userRole;
		this.permanentDeleteAllowed = permanentDeleteAllowed;
		this.subjectDn = subjectDn;
	}

	public UserRole getRole()
	{
		return userRole;
	}

	public boolean isPermanentDeleteAllowed()
	{
		return permanentDeleteAllowed;
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
