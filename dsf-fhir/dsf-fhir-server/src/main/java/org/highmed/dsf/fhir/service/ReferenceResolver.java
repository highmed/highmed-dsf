package org.highmed.dsf.fhir.service;

import java.sql.Connection;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;

public interface ReferenceResolver
{
	/**
	 * @param reference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return <code>true</code> if the {@link ResourceReference} can be resolved
	 */
	boolean referenceCanBeResolved(ResourceReference reference, Connection connection);

	/**
	 * @param user
	 *            not <code>null</code>
	 * @param reference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return {@link Optional#empty()} if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_INTERNAL},
	 *             {@link ResourceReference.ReferenceType#LITERAL_EXTERNAL},
	 *             {@link ResourceReference.ReferenceType#CONDITIONAL} or
	 *             {@link ResourceReference.ReferenceType#LOGICAL}
	 */
	Optional<Resource> resolveReference(User user, ResourceReference reference, Connection connection);

	/**
	 * @param reference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return <code>true</code> if the {@link ResourceReference} can be checked
	 */
	boolean referenceCanBeChecked(ResourceReference reference, Connection connection);

	/**
	 * @param resource
	 *            not <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return {@link Optional#empty()} if the reference could be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_INTERNAL}
	 * @see ResourceReference#getType(String)
	 */
	Optional<OperationOutcome> checkLiteralInternalReference(Resource resource, ResourceReference resourceReference,
			Connection connection) throws IllegalArgumentException;

	/**
	 * @param resource
	 *            not <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @param bundleIndex
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if the reference could be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_INTERNAL}
	 * @see ResourceReference#getType(String)
	 */
	Optional<OperationOutcome> checkLiteralInternalReference(Resource resource, ResourceReference resourceReference,
			Connection connection, Integer bundleIndex) throws IllegalArgumentException;

	/**
	 * @param resource
	 *            not <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @return {@link Optional#empty()} if the reference check was @Override successful
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_EXTERNAL}
	 * @see ResourceReference#getType(String)
	 */
	Optional<OperationOutcome> checkLiteralExternalReference(Resource resource, ResourceReference resourceReference)
			throws IllegalArgumentException;

	/**
	 * @param resource
	 *            not <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param bundleIndex
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if the reference check was successful
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_EXTERNAL}
	 * @see ResourceReference#getType(String)
	 */
	Optional<OperationOutcome> checkLiteralExternalReference(Resource resource, ResourceReference resourceReference,
			Integer bundleIndex) throws IllegalArgumentException;

	/**
	 * @param user
	 *            not <code>null</code>
	 * @param resource
	 *            not <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @param bundleIndex
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if the reference check was successful
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#CONDITIONAL}
	 * @see ResourceReference#getType(String)
	 */
	Optional<OperationOutcome> checkConditionalReference(User user, Resource resource,
			ResourceReference resourceReference, Connection connection, Integer bundleIndex)
			throws IllegalArgumentException;

	/**
	 * @param user
	 *            not <code>null</code>
	 * @param resource
	 *            not <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return {@link Optional#empty()} if the reference check was successful
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LOGICAL}
	 * @see ResourceReference#getType(String)
	 */
	Optional<OperationOutcome> checkLogicalReference(User user, Resource resource, ResourceReference resourceReference,
			Connection connection) throws IllegalArgumentException;

	/**
	 * @param user
	 *            not <code>null</code>
	 * @param resource
	 *            not <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @param bundleIndex
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if the reference check was successful
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LOGICAL}
	 * @see ResourceReference#getType(String)
	 */
	Optional<OperationOutcome> checkLogicalReference(User user, Resource resource, ResourceReference resourceReference,
			Connection connection, Integer bundleIndex) throws IllegalArgumentException;
}
