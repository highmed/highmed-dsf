package org.highmed.fhir.service;

import java.sql.Connection;

import javax.ws.rs.WebApplicationException;

import org.highmed.fhir.dao.command.ResourceReference;
import org.hl7.fhir.r4.model.DomainResource;

public interface ReferenceResolver
{
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
	boolean resolveReference(DomainResource resource, ResourceReference resourceReference, Connection connection);

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
	boolean resolveReference(DomainResource resource, Integer bundleIndex, ResourceReference resourceReference,
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
	boolean resolveLiteralInternalReference(DomainResource resource, ResourceReference resourceReference,
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
	boolean resolveLiteralInternalReference(DomainResource resource, Integer bundleIndex,
			ResourceReference resourceReference, Connection connection) throws WebApplicationException;

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
	boolean resolveLiteralExternalReference(DomainResource resource, ResourceReference resourceReference);

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
	boolean resolveLiteralExternalReference(DomainResource resource, Integer bundleIndex,
			ResourceReference resourceReference) throws WebApplicationException, IllegalArgumentException;

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
	boolean resolveConditionalReference(DomainResource resource, ResourceReference resourceReference,
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
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#CONDITIONAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveConditionalReference(DomainResource resource, Integer bundleIndex,
			ResourceReference resourceReference, Connection connection)
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
	 *             if the reference is not of type {@link ResourceReference.ReferenceType#LOGICAL}
	 * @see ResourceReference#getType(String)
	 */
	boolean resolveLogicalReference(DomainResource resource, ResourceReference resourceReference, Connection connection)
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
	boolean resolveLogicalReference(DomainResource resource, Integer bundleIndex, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException, IllegalArgumentException;
}
