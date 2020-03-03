package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.UserRole;

public enum AuthorizationRole
{
	/**
	 * For users with {@link UserRole#LOCAL} only
	 */
	LOCAL,
	/**
	 * For users with {@link UserRole#LOCAL} or {@link UserRole#REMOTE}
	 */
	REMOTE;

	public static Optional<AuthorizationRole> fromString(String role)
	{
		switch (role)
		{
			case "LOCAL":
			case "local":
				return Optional.of(LOCAL);

			case "REMOTE":
			case "remote":
				return Optional.of(REMOTE);

			default:
				return Optional.empty();
		}
	}
}
