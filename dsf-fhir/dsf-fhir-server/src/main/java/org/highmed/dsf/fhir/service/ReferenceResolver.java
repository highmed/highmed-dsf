package org.highmed.dsf.fhir.service;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;

import org.highmed.dsf.fhir.authentication.User;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public interface ReferenceResolver
{
	/**
	 * @param user
	 *            not <code>null</code>
	 * @param referenceLocation
	 * @param reference
	 *            not <code>null</code>
	 * @param referenceTypes
	 * @return {@link Optional#empty()} if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_INTERNAL},
	 *             {@link ResourceReference.ReferenceType#LITERAL_EXTERNAL},
	 *             {@link ResourceReference.ReferenceType#CONDITIONAL} or
	 *             {@link ResourceReference.ReferenceType#LOGICAL}
	 */
	Optional<Resource> resolveReference(User user, String referenceLocation, Reference reference,
			List<Class<? extends Resource>> referenceTypes);

	/**
	 * @param user
	 *            not <code>null</code>
	 * @param reference
	 *            not <code>null</code>
	 * @return {@link Optional#empty()} if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_INTERNAL},
	 *             {@link ResourceReference.ReferenceType#LITERAL_EXTERNAL},
	 *             {@link ResourceReference.ReferenceType#CONDITIONAL} or
	 *             {@link ResourceReference.ReferenceType#LOGICAL}
	 */
	Optional<Resource> resolveReference(User user, ResourceReference reference);

	/**
	 * @param user
	 *            not <code>null</code>
	 * @param resource
	 *            not <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return <code>true</code> if the resource was changed by resolving the reference (always returns
	 *         <code>false</code> for literal references)
	 * @throws WebApplicationException
	 *             if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_INTERNAL},
	 *             {@link ResourceReference.ReferenceType#LITERAL_EXTERNAL},
	 *             {@link ResourceReference.ReferenceType#CONDITIONAL} or
	 *             {@link ResourceReference.ReferenceType#LOGICAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveReference(User user, Resource resource, ResourceReference resourceReference, Connection connection);

	/**
	 * @param user
	 *            not <code>null</code>
	 * @param resource
	 *            not <code>null</code>
	 * @param bundleIndex
	 *            may be <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return <code>true</code> if the resource was changed by resolving the reference (always returns
	 *         <code>false</code> for literal references)
	 * @throws WebApplicationException
	 *             if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_INTERNAL},
	 *             {@link ResourceReference.ReferenceType#LITERAL_EXTERNAL},
	 *             {@link ResourceReference.ReferenceType#CONDITIONAL} or
	 *             {@link ResourceReference.ReferenceType#LOGICAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveReference(User user, Resource resource, Integer bundleIndex, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException, IllegalArgumentException;

	/**
	 * @param resource
	 *            not <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return <code>true</code> if the resource was changed by resolving the reference (always returns
	 *         <code>false</code> for literal references)
	 * @throws WebApplicationException
	 *             if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_INTERNAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveLiteralInternalReference(Resource resource, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException, IllegalArgumentException;

	/**
	 * @param resource
	 *            not <code>null</code>
	 * @param bundleIndex
	 *            may be <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return <code>true</code> if the resource was changed by resolving the reference (always returns
	 *         <code>false</code> for literal references)
	 * @throws WebApplicationException
	 *             if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_INTERNAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveLiteralInternalReference(Resource resource, Integer bundleIndex, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException;

	/**
	 * @param resource
	 *            not <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return <code>true</code> if the resource was changed by resolving the reference (always returns
	 *         <code>false</code> for literal references)
	 * @throws WebApplicationException
	 *             if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_EXTERNAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveLiteralExternalReference(Resource resource, ResourceReference resourceReference);

	/**
	 * @param resource
	 *            not <code>null</code>
	 * @param bundleIndex
	 *            may be <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return <code>true</code> if the resource was changed by resolving the reference (always returns
	 *         <code>false</code> for literal references)
	 * @throws WebApplicationException
	 *             if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_EXTERNAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveLiteralExternalReference(Resource resource, Integer bundleIndex, ResourceReference resourceReference)
			throws WebApplicationException, IllegalArgumentException;

	/**
	 * @param user
	 *            not <code>null</code>
	 * @param resource
	 *            not <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return <code>true</code> if the resource was changed by resolving the reference (always returns
	 *         <code>false</code> for literal references)
	 * @throws WebApplicationException
	 *             if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#CONDITIONAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveConditionalReference(User user, Resource resource, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException, IllegalArgumentException;

	/**
	 * @param user
	 *            not <code>null</code>
	 * @param resource
	 *            not <code>null</code>
	 * @param bundleIndex
	 *            may be <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return <code>true</code> if the resource was changed by resolving the reference (always returns
	 *         <code>false</code> for literal references)
	 * @throws WebApplicationException
	 *             if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#CONDITIONAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveConditionalReference(User user, Resource resource, Integer bundleIndex,
			ResourceReference resourceReference, Connection connection)
			throws WebApplicationException, IllegalArgumentException;

	/**
	 * @param user
	 *            not <code>null</code>
	 * @param resource
	 *            not <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return <code>true</code> if the resource was changed by resolving the reference (always returns
	 *         <code>false</code> for literal references)
	 * @throws WebApplicationException
	 *             if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LOGICAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveLogicalReference(User user, Resource resource, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException, IllegalArgumentException;

	/**
	 * @param user
	 *            not <code>null</code>
	 * @param resource
	 *            not <code>null</code>
	 * @param bundleIndex
	 *            may be <code>null</code>
	 * @param resourceReference
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @return <code>true</code> if the resource was changed by resolving the reference (always returns
	 *         <code>false</code> for literal references)
	 * @throws WebApplicationException
	 *             if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LOGICAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveLogicalReference(User user, Resource resource, Integer bundleIndex,
			ResourceReference resourceReference, Connection connection)
			throws WebApplicationException, IllegalArgumentException;
}
