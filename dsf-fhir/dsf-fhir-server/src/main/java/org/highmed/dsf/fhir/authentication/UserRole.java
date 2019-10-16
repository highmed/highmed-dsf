package org.highmed.dsf.fhir.authentication;

public enum UserRole
{
	LOCAL, REMOTE;

	public static boolean userHasOneOfRoles(User u, UserRole... expectedRoles)
	{
		UserRole usersRole = u.getRole();
		for (UserRole expectedRole : expectedRoles)
			if (usersRole != null && expectedRole.equals(usersRole))
				return true;

		return false;
	}
}
