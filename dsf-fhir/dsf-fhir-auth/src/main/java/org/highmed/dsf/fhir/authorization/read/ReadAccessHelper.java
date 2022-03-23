package org.highmed.dsf.fhir.authorization.read;

import java.util.List;
import java.util.function.Predicate;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Resource;

/**
 * Helper with methods to configure read access to FHIR resources.
 */
public interface ReadAccessHelper
{
	String READ_ACCESS_TAG_SYSTEM = "http://highmed.org/fhir/CodeSystem/read-access-tag";
	String READ_ACCESS_TAG_VALUE_LOCAL = "LOCAL";
	String READ_ACCESS_TAG_VALUE_ORGANIZATION = "ORGANIZATION";
	String READ_ACCESS_TAG_VALUE_ROLE = "ROLE";
	String READ_ACCESS_TAG_VALUE_ALL = "ALL";

	String ORGANIZATION_IDENTIFIER_SYSTEM = "http://highmed.org/sid/organization-identifier";

	String EXTENSION_READ_ACCESS_ORGANIZATION = "http://highmed.org/fhir/StructureDefinition/extension-read-access-organization";

	String EXTENSION_READ_ACCESS_CONSORTIUM_ROLE = "http://highmed.org/fhir/StructureDefinition/extension-read-access-consortium-role";
	String EXTENSION_READ_ACCESS_CONSORTIUM_ROLE_CONSORTIUM = "consortium";
	String EXTENSION_READ_ACCESS_CONSORTIUM_ROLE_ROLE = "role";

	/**
	 * Adds LOCAL tag. Removes ALL tag if present.
	 *
	 * @param <R>
	 *            the resource type
	 * @param resource
	 *            may be <code>null</code>
	 * @return <code>null</code> if given <b>resource</b> is <code>null</code>
	 * @see #addAll(Resource)
	 */
	<R extends Resource> R addLocal(R resource);

	/**
	 * Adds ORGANIZATION tag for the given organization. Adds LOCAL tag if not present, removes ALL tag if present.
	 *
	 * @param <R>
	 *            the resource type
	 * @param resource
	 *            may be <code>null</code>
	 * @param organizationIdentifier
	 *            not <code>null</code>
	 * @return <code>null</code> if given <b>resource</b> is <code>null</code>
	 * @see #addLocal(Resource)
	 * @see #addOrganization(Resource, Organization)
	 */
	<R extends Resource> R addOrganization(R resource, String organizationIdentifier);

	/**
	 * Adds ORGANIZATION tag for the given organization. Adds LOCAL tag if not present, removes ALL tag if present.
	 *
	 * @param <R>
	 *            the resource type
	 * @param resource
	 *            may be <code>null</code>
	 * @param organization
	 *            not <code>null</code>
	 * @return <code>null</code> if given <b>resource</b> is <code>null</code>
	 * @throws NullPointerException
	 *             if given <b>organization</b> is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if given <b>organization</b> does not have valid identifier
	 * @see #addLocal(Resource)
	 * @see #addOrganization(Resource, String)
	 */
	<R extends Resource> R addOrganization(R resource, Organization organization);

	/**
	 * Adds ROLE tag for the given affiliation. Adds LOCAL tag if not present, removes ALL tag if present.
	 *
	 * @param <R>
	 *            the resource type
	 * @param resource
	 *            may be <code>null</code>
	 * @param consortiumIdentifier
	 *            not <code>null</code>
	 * @param roleSystem
	 *            not <code>null</code>
	 * @param roleCode
	 *            not <code>null</code>
	 * @return <code>null</code> if given <b>resource</b> is <code>null</code>
	 * @see #addLocal(Resource)
	 * @see #addRole(Resource, OrganizationAffiliation)
	 */
	<R extends Resource> R addRole(R resource, String consortiumIdentifier, String roleSystem, String roleCode);

	/**
	 * Adds ROLE tag for the given affiliation. Adds LOCAL tag if not present, removes ALL tag if present.
	 *
	 * @param <R>
	 *            the resource type
	 * @param resource
	 *            may be <code>null</code>
	 * @param affiliation
	 *            not <code>null</code>
	 * @return <code>null</code> if given <b>resource</b> is <code>null</code>
	 * @throws NullPointerException
	 *             if given <b>affiliation</b> is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if given <b>affiliation</b> does not have valid consortium identifier or organization role (only one
	 *             role supported)
	 * @see #addLocal(Resource)
	 * @see #addRole(Resource, String, String, String)
	 */
	<R extends Resource> R addRole(R resource, OrganizationAffiliation affiliation);

	/**
	 * Adds All tag. Removes LOCAL, ORGANIZATION and ROLE tags if present.
	 *
	 * @param <R>
	 *            the resource type
	 * @param resource
	 *            may be <code>null</code>
	 * @return <code>null</code> if given <b>resource</b> is <code>null</code>
	 * @see #addLocal(Resource)
	 * @see #addOrganization(Resource, String)
	 * @see #addRole(Resource, String, String, String)
	 */
	<R extends Resource> R addAll(R resource);

	boolean hasLocal(Resource resource);

	boolean hasOrganization(Resource resource, String organizationIdentifier);

	boolean hasOrganization(Resource resource, Organization organization);

	boolean hasAnyOrganization(Resource resource);

	boolean hasRole(Resource resource, String consortiumIdentifier, String roleSystem, String roleCode);

	boolean hasRole(Resource resource, OrganizationAffiliation affiliation);

	boolean hasRole(Resource resource, List<OrganizationAffiliation> affiliations);

	boolean hasAnyRole(Resource resource);

	boolean hasAll(Resource resource);

	/**
	 * <b>Resource with access tags valid if:</b><br>
	 *
	 * 1 LOCAL tag and n {ORGANIZATION, ROLE} tags {@code (n >= 0)}<br>
	 * or<br>
	 * 1 ALL tag<br>
	 * <br>
	 * All tags {LOCAL, ORGANIZATION, ROLE, ALL} valid<br>
	 * <br>
	 * Does not check if referenced organizations or roles exist
	 *
	 * @param resource
	 *            may be <code>null</code>
	 * @return <code>false</code> if given <b>resource</b> is <code>null</code> or resource not valid
	 */
	boolean isValid(Resource resource);

	/**
	 * <b>Resource with access tags valid if:</b><br>
	 *
	 * 1 LOCAL tag and n {ORGANIZATION, ROLE} tags {@code (n >= 0)}<br>
	 * or<br>
	 * 1 ALL tag<br>
	 * <br>
	 * All tags {LOCAL, ORGANIZATION, ROLE, ALL} valid
	 *
	 * @param resource
	 *            may be <code>null</code>
	 * @param organizationWithIdentifierExists
	 *            not <code>null</code>
	 * @param roleExists
	 *            not <code>null</code>
	 * @return <code>false</code> if given <b>resource</b> is <code>null</code> or resource not valid
	 */
	boolean isValid(Resource resource, Predicate<Identifier> organizationWithIdentifierExists,
			Predicate<Coding> roleExists);
}
