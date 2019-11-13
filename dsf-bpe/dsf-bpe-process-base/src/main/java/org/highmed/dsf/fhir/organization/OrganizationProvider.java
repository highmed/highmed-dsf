package org.highmed.dsf.fhir.organization;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;

public interface OrganizationProvider
{
	String getDefaultSystem();

	String getLocalIdentifierValue();

	Optional<Organization> getOrganization(String identifier);

	Optional<Organization> getOrganization(String system, String identifier);

	/**
	 * @return the local organization
	 * @throws NoSuchElementException
	 *             if no {@link Organization} with {@link #getDefaultSystem()} and {@link #getLocalIdentifierValue()}
	 *             could be found
	 */
	Organization getLocalOrganization();

	/**
	 * @return {@link Organization}s with {@link #getDefaultSystem()} and identifier other than
	 *         {@link #getLocalIdentifierValue()}
	 */
	List<Organization> getRemoteOrganizations();

	Identifier getLocalIdentifier();

	/**
	 * @return {@link Organization}s {@link Identifier} with {@link #getDefaultSystem()} and identifier other than
	 *         {@link #getLocalIdentifierValue()}
	 */
	List<Identifier> getRemoteIdentifiers();

	/**
	 * @param organizationId
	 * @return {@link Organization}s {@link Identifier} with idPart equal to the given organizationId, or
	 *         {@link Optional#empty()} if not found
	 */
	Optional<Identifier> getIdentifier(IdType organizationId);

	Stream<Organization> searchRemoteOrganizations(String searchParameterIdentifierValue);

	Stream<Identifier> searchRemoteOrganizationsIdentifiers(String searchParameterIdentifierValue);
}
