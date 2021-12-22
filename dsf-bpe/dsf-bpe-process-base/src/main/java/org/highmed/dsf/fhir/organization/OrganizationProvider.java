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
	 * @return url of the default organization type CodeSystem
	 */
	@Deprecated
	String getDefaultTypeSystem();

	/**
	 * @return url of the default organization role CodeSystem
	 */
	String getDefaultRoleSystem();

	String getLocalIdentifierValue();

	Optional<Organization> getOrganization(String identifier);

	Optional<Organization> getOrganization(String system, String identifier);

	/**
	 * @return the active local organization
	 * @throws NoSuchElementException
	 *             if no {@link Organization} with {@link #getDefaultIdentifierSystem()} and
	 *             {@link #getLocalIdentifierValue()} could be found
	 */
	Organization getLocalOrganization();

	/**
	 * @return active {@link Organization}s with {@link #getDefaultIdentifierSystem()} and identifier other than
	 *         {@link #getLocalIdentifierValue()}
	 */
	List<Organization> getRemoteOrganizations();

	/**
	 * @param type
	 *            not <code>null</code>
	 * @return active {@link Organization}s with {@link #getDefaultTypeSystem()} and given type
	 * @deprecated as of release 0.6.0, the organization type has moved into the OrganizationAffiliation resource, use
	 *             {@link #getOrganizationsByRole(String)} instead
	 */
	@Deprecated
	Stream<Organization> getOrganizationsByType(String type);

	/**
	 * @param roleSystem
	 *            not <code>null</code>
	 * @param roleValue
	 *            not <code>null</code>
	 * @return active {@link Organization}s having an {@link OrganizationAffiliation} matching the given role
	 */
	Stream<Organization> getOrganizationsByRole(String roleSystem, String roleValue);

	/**
	 * @param role
	 *            specifies the roleValue, uses {@link #getDefaultRoleSystem()} as roleSystem, not <code>null</code>
	 * @return active {@link Organization}s having an {@link OrganizationAffiliation} matching
	 *         {@link #getDefaultRoleSystem()} and the given role value
	 */
	default Stream<Organization> getOrganizationsByRole(String role)
	{
		return getOrganizationsByRole(getDefaultRoleSystem(), role);
	}

	/**
	 * @param consortiumIdentifierSystem
	 *            not <code>null</code>
	 * @param consortiumIdentifierValue
	 *            not <code>null</code>
	 * @return active {@link Organization}s having an {@link OrganizationAffiliation} using the given
	 *         consortiumIdentifier
	 */
	Stream<Organization> getOrganizationsByConsortium(String consortiumIdentifierSystem,
			String consortiumIdentifierValue);

	/**
	 * @param consortiumIdentifier
	 *            specifies the consortiumIdentifierValue, uses {@link #getDefaultIdentifierSystem()} as
	 *            consortiumIdentifierSystem, not <code>null</code>
	 * @return active {@link Organization}s having an {@link OrganizationAffiliation} using
	 *         {@link #getDefaultIdentifierSystem()} and the given consortiumIdentifier value
	 */
	default Stream<Organization> getOrganizationsByConsortium(String consortiumIdentifier)
	{
		return getOrganizationsByConsortium(getDefaultIdentifierSystem(), consortiumIdentifier);
	}

	/**
	 * @param consortiumIdentifierSystem
	 *            not <code>null</code>
	 * @param consortiumIdentifierValue
	 *            not <code>null</code>
	 * @param roleSystem
	 *            not <code>null</code>
	 * @param roleValue
	 *            not <code>null</code>
	 * @return active {@link Organization}s having an {@link OrganizationAffiliation} using the given
	 *         consortiumIdentifier and role
	 */
	Stream<Organization> getOrganizationsByConsortiumAndRole(String consortiumIdentifierSystem,
			String consortiumIdentifierValue, String roleSystem, String roleValue);

	/**
	 * @param consortiumIdentifier
	 *            specifies the consortiumIdentifierValue, uses {@link #getDefaultIdentifierSystem()} as
	 *            consortiumIdentifierSystem, not <code>null</code>
	 * @param role
	 *            specifies the roleValue, uses {@link #getDefaultRoleSystem()} as roleSystem, not <code>null</code>
	 * @return active {@link Organization}s having an {@link OrganizationAffiliation} using
	 *         {@link #getDefaultIdentifierSystem()} and the given consortiumIdentifier value as well as matching
	 *         {@link #getDefaultRoleSystem()} and the given role value
	 */
	default Stream<Organization> getOrganizationsByConsortiumAndRole(String consortiumIdentifier, String role)
	{
		return getOrganizationsByConsortiumAndRole(getDefaultIdentifierSystem(), consortiumIdentifier,
				getDefaultRoleSystem(), role);
	}

	Identifier getLocalIdentifier();

	/**
	 * @return active {@link Organization}s {@link Identifier} with {@link #getDefaultIdentifierSystem()} and identifier
	 *         other than {@link #getLocalIdentifierValue()}
	 */
	List<Identifier> getRemoteIdentifiers();

	/**
	 * @param organizationId
	 *            not <code>null</code>
	 * @return active {@link Organization}s {@link Identifier} with idPart equal to the given organizationId, or
	 *         {@link Optional#empty()} if not found
	 */
	Optional<Identifier> getIdentifier(IdType organizationId);

	Stream<Organization> searchRemoteOrganizations(String searchParameterIdentifierValue);

	Stream<Identifier> searchRemoteOrganizationsIdentifiers(String searchParameterIdentifierValue);
}
