package org.highmed.dsf.fhir.service;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public interface ReferenceResolver
{
	/**
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
	Optional<Resource> resolveReference(String referenceLocation, Reference reference,
			List<Class<? extends Resource>> referenceTypes);

	/**
	 * @param reference
	 *            not <code>null</code>
	 * @return {@link Optional#empty()} if the reference could not be resolved
	 * @throws IllegalArgumentException
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_INTERNAL},
	 *             {@link ResourceReference.ReferenceType#LITERAL_EXTERNAL},
	 *             {@link ResourceReference.ReferenceType#CONDITIONAL} or
	 *             {@link ResourceReference.ReferenceType#LOGICAL}
	 */
	Optional<Resource> resolveReference(ResourceReference reference);

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
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_INTERNAL},
	 *             {@link ResourceReference.ReferenceType#LITERAL_EXTERNAL},
	 *             {@link ResourceReference.ReferenceType#CONDITIONAL} or
	 *             {@link ResourceReference.ReferenceType#LOGICAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveReference(Resource resource, ResourceReference resourceReference, Connection connection);

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
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LITERAL_INTERNAL},
	 *             {@link ResourceReference.ReferenceType#LITERAL_EXTERNAL},
	 *             {@link ResourceReference.ReferenceType#CONDITIONAL} or
	 *             {@link ResourceReference.ReferenceType#LOGICAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveReference(Resource resource, Integer bundleIndex, ResourceReference resourceReference,
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
	boolean resolveConditionalReference(Resource resource, ResourceReference resourceReference, Connection connection)
			throws WebApplicationException, IllegalArgumentException;

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
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#CONDITIONAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveConditionalReference(Resource resource, Integer bundleIndex, ResourceReference resourceReference,
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
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LOGICAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveLogicalReference(Resource resource, ResourceReference resourceReference, Connection connection)
			throws WebApplicationException, IllegalArgumentException;

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
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LOGICAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveLogicalReference(Resource resource, Integer bundleIndex, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException, IllegalArgumentException;
}
