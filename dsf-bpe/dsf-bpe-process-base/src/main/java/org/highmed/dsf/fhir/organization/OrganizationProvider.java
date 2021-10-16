package org.highmed.dsf.fhir.organization;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;

public interface OrganizationProvider
{
	String getDefaultIdentifierSystem();

	/**
	 * @deprecated as of release 0.6.0, use {@link #getDefaultRoleSystem()} instead
	 */
	@Deprecated
	String getDefaultTypeSystem();

	String getDefaultRoleSystem();

	String getLocalIdentifierValue();

	Optional<Organization> getOrganization(String identifier);

	Optional<Organization> getOrganization(String system, String identifier);

	/**
	 * @return the local organization
	 * @throws NoSuchElementException
	 *             if no {@link Organization} with {@link #getDefaultIdentifierSystem()} and
	 *             {@link #getLocalIdentifierValue()} could be found
	 */
	Organization getLocalOrganization();

	/**
	 * @return {@link Organization}s with {@link #getDefaultIdentifierSystem()} and identifier other than
	 *         {@link #getLocalIdentifierValue()}
	 */
	List<Organization> getRemoteOrganizations();

	/**
	 * @param type
	 *            not <code>null</code>
	 * @return {@link Organization}s with {@link #getDefaultTypeSystem()} and given type
	 * @deprecated as of release 0.6.0, the organization type has moved into the OrganizationAffiliation resource, use
	 *             {@link #getOrganizationsByRole(String)} instead
	 */
	@Deprecated
	Stream<Organization> getOrganizationsByType(String type);

	/**
	 * @param role
	 *            not <code>null</code>
	 * @return {@link Organization}s having an {@link OrganizationAffiliation} matching the given type and
	 *         {@link #getDefaultRoleSystem()}
	 */
	Stream<Organization> getOrganizationsByRole(String role);

	/**
	 * @param consortiumIdentifier
	 *            not <code>null</code>
	 * @return {@link Organization}s having an {@link OrganizationAffiliation} to the given consortiumIdentifier
	 */
	Stream<Organization> getOrganizationsByConsortium(String consortiumIdentifier);

	/**
	 * @param consortiumIdentifier
	 *            not <code>null</code>
	 * @param role
	 *            not <code>null</code>
	 * @return {@link Organization}s having an {@link OrganizationAffiliation} to the given consortiumIdentifier and
	 *         matching the given role and {@link #getDefaultRoleSystem()}
	 */
	Stream<Organization> getOrganizationsByConsortiumAndRole(String consortiumIdentifier, String role);

	Identifier getLocalIdentifier();

	/**
	 * @return {@link Organization}s {@link Identifier} with {@link #getDefaultIdentifierSystem()} and identifier other
	 *         than {@link #getLocalIdentifierValue()}
	 */
	List<Identifier> getRemoteIdentifiers();

	/**
	 * @param organizationId
	 *            not <code>null</code>
	 * @return {@link Organization}s {@link Identifier} with idPart equal to the given organizationId, or
	 *         {@link Optional#empty()} if not found
	 */
	Optional<Identifier> getIdentifier(IdType organizationId);

	Stream<Organization> searchRemoteOrganizations(String searchParameterIdentifierValue);

	Stream<Identifier> searchRemoteOrganizationsIdentifiers(String searchParameterIdentifierValue);
}
