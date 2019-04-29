package org.highmed.fhir.organization;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.hl7.fhir.r4.model.Organization;

public interface OrganizationProvider
{
	String getDefaultSystem();

	String getLocalIdentifier();

	Optional<Organization> getOrganization(String identifier);

	Optional<Organization> getOrganization(String system, String identifier);

	/**
	 * @return the local organization
	 * @throws NoSuchElementException
	 *             if no {@link Organization} with {@link #getDefaultSystem()} and {@link #getLocalIdentifier()} could
	 *             be found
	 */
	Organization getLocalOrganization();

	/**
	 * @return {@link Organization}s with {@link #getDefaultSystem()} and identifier other than
	 *         {@link #getLocalIdentifier()}
	 */
	List<Organization> getRemoteOrganizations();
}
