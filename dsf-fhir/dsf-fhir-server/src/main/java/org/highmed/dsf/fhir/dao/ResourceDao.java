package org.highmed.dsf.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;
import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.dsf.fhir.dao.exception.ResourceNotMarkedDeletedException;
import org.highmed.dsf.fhir.dao.exception.ResourceVersionNoMatchException;
import org.highmed.dsf.fhir.search.DbSearchQuery;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.hl7.fhir.r4.model.Resource;

public interface ResourceDao<R extends Resource>
{
	int FIRST_VERSION = 1;
	String FIRST_VERSION_STRING = String.valueOf(FIRST_VERSION);

	String getResourceTypeName();

	Class<R> getResourceType();

	Connection newReadWriteTransaction() throws SQLException;

	/**
	 * @param resource
	 *            not <code>null</code>
	 * @return the stored resource, not the same object as the given resource (defensive copy)
	 * @throws SQLException
	 *             if database access errors occur
	 */
	R create(R resource) throws SQLException;

	/**
	 * @param resource
	 *            not <code>null</code>
	 * @param uuid
	 *            not <code>null</code>
	 * @return the stored resource, not the same object as the given resource (defensive copy)
	 * @throws SQLException
	 *             if database access errors occur
	 */
	R createWithId(R resource, UUID uuid) throws SQLException;

	/**
	 * @param connection
	 *            not <code>null</code>, not {@link Connection#isReadOnly()}
	 * @param resource
	 *            not <code>null</code>
	 * @param uuid
	 *            not <code>null</code>
	 * @return the stored resource, not the same object as the given resource
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws IllegalArgumentException
	 *             if the given connection is {@link Connection#isReadOnly()}
	 */
	R createWithTransactionAndId(Connection connection, R resource, UUID uuid) throws SQLException;

	/**
	 * @param uuid
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if the given uuid is <code>null</code> or no resource could be found for the
	 *         given uuid
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws ResourceDeletedException
	 *             if a resource with the given uuid could be found, but is marked as delete
	 */
	Optional<R> read(UUID uuid) throws SQLException, ResourceDeletedException;

	/**
	 * @param connection
	 *            not <code>null</code>
	 * @param uuid
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if the given uuid is <code>null</code> or no resource could be found for the
	 *         given uuid
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws ResourceDeletedException
	 *             if a resource with the given uuid could be found, but is marked as delete
	 */
	Optional<R> readWithTransaction(Connection connection, UUID uuid) throws SQLException, ResourceDeletedException;

	/**
	 * @param uuid
	 *            may be <code>null</code>
	 * @param version
	 *            may be less then {@value #FIRST_VERSION}
	 * @return {@link Optional#empty()} if the given uuid is <code>null</code>, the given version is less then
	 *         {@value #FIRST_VERSION} or no resource could be found for the given uuid and version
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws ResourceDeletedException
	 *             if a resource with the given uuid and version could be found, but is the delete history entry
	 */
	Optional<R> readVersion(UUID uuid, long version) throws SQLException, ResourceDeletedException;

	/**
	 * @param connection
	 *            not <code>null</code>
	 * @param uuid
	 *            may be <code>null</code>
	 * @param version
	 *            may be less then {@value #FIRST_VERSION}
	 * @return {@link Optional#empty()} if the given uuid is <code>null</code>, the given version is less then
	 *         {@value #FIRST_VERSION} or no resource could be found for the given uuid and version
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws ResourceDeletedException
	 *             if a resource with the given uuid and version could be found, but is the delete history entry
	 */
	Optional<R> readVersionWithTransaction(Connection connection, UUID uuid, long version)
			throws SQLException, ResourceDeletedException;

	/**
	 * @param uuid
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if the given uuid is <code>null</code> or no resource could be found for the
	 *         given uuid
	 * @throws SQLException
	 *             if database access errors occur
	 */
	Optional<R> readIncludingDeleted(UUID uuid) throws SQLException;

	/**
	 * @param connection
	 *            not <code>null</code>
	 * @param uuid
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if the given uuid is <code>null</code> or no resource could be found for the
	 *         given uuid
	 * @throws SQLException
	 *             if database access errors occur
	 */
	Optional<R> readIncludingDeletedWithTransaction(Connection connection, UUID uuid) throws SQLException;

	List<R> readAll() throws SQLException;

	/**
	 * @param connection
	 *            not <code>null</code>
	 * @return {@link List} containing matching resources
	 * @throws SQLException
	 *             if database access errors occur
	 */
	List<R> readAllWithTransaction(Connection connection) throws SQLException;

	/**
	 * @param id
	 *            not <code>null</code>
	 * @param version
	 *            may be <code>null</code>
	 * @return if the given <b>id</b> is null <code>false</code>; if the given <b>version</b> is <code>null</code> or
	 *         blank and a resource with the given <b>id</b> exists and is not marked as deleted <code>true</code>; if
	 *         the given <b>version</b> is not <code>null</code> and not blank and a resource with the given <b>id</b>
	 *         and <b>version</b> exists and is not marked as deleted <code>true</code>
	 * @throws SQLException
	 *             if database access errors occur
	 * @see String#isBlank()
	 */
	boolean existsNotDeleted(String id, String version) throws SQLException;

	/**
	 * @param connection
	 *            not <code>null</code>
	 * @param id
	 *            not <code>null</code>
	 * @param version
	 *            may be <code>null</code>
	 * @return if the given <b>id</b> is null <code>false</code>; if the given <b>version</b> is <code>null</code> or
	 *         blank and a resource with the given <b>id</b> exists and is not marked as deleted <code>true</code>; if
	 *         the given <b>version</b> is not <code>null</code> and not blank and a resource with the given <b>id</b>
	 *         and <b>version</b> exists and is not marked as deleted <code>true</code>
	 * @throws SQLException
	 *             if database access errors occur
	 * @see String#isBlank()
	 */
	boolean existsNotDeletedWithTransaction(Connection connection, String id, String version) throws SQLException;

	/**
	 * Sets the version of the stored resource to latest version from DB plus 1.
	 *
	 * Does not check the latest version in DB before storing the update.
	 *
	 * Resurrects all old versions (removes deleted flag) if the latest version in DB is marked as deleted.
	 *
	 * @param resource
	 *            not <code>null</code>
	 * @return the stored resource, not the same object as the given resource (defensive copy)
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws ResourceNotFoundException
	 *             if the given resource could not be found
	 * @see ResourceDao#update(Resource, Long)
	 */
	default R update(R resource) throws SQLException, ResourceNotFoundException
	{
		try
		{
			return update(resource, null);
		}
		catch (ResourceVersionNoMatchException e)
		{
			// should never be thrown if update is called with a null expectedVersion
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sets the version of the stored resource to latest version from DB plus 1.
	 *
	 * If the given expectedVersion is not <code>null</code>, checks if the given expectedVersion is the latest version
	 * in DB before storing the update.
	 *
	 * Resurrects all old versions (removes deleted flag) if the latest version in DB is marked as deleted.
	 *
	 * @param resource
	 *            not <code>null</code>
	 * @param expectedVersion
	 *            may be <code>null</code>
	 * @return the stored resource, not the same object as the given resource (defensive copy)
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws ResourceNotFoundException
	 *             if the given resource could not be found
	 * @throws ResourceVersionNoMatchException
	 *             if the given expectedVersion is not <code>null</code> and the latest version does not match the given
	 *             expectedVersion
	 */
	R update(R resource, Long expectedVersion)
			throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException;

	/**
	 * Sets the version of the stored resource to latest version from DB plus 1.
	 *
	 * Does not check the latest version in DB before storing the update.
	 *
	 * Resurrects all old versions (removes deleted flag) if the latest version in DB is marked as deleted.
	 *
	 * @param connection
	 *            not <code>null</code>, not {@link Connection#isReadOnly()} and not {@link Connection#getAutoCommit()}
	 *            and {@link Connection#getTransactionIsolation()} one of {@link Connection#TRANSACTION_REPEATABLE_READ}
	 *            or {@link Connection#TRANSACTION_SERIALIZABLE}
	 * @param resource
	 *            not <code>null</code>
	 * @return the stored resource, not the same object as the given resource (defensive copy)
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws ResourceNotFoundException
	 *             if the given resource could not be found
	 * @throws IllegalArgumentException
	 *             if the given connection is {@link Connection#isReadOnly()} or is {@link Connection#getAutoCommit()}
	 *             or {@link Connection#getTransactionIsolation()} is not one of
	 *             {@link Connection#TRANSACTION_REPEATABLE_READ} or {@link Connection#TRANSACTION_SERIALIZABLE}
	 */
	default R updateWithTransaction(Connection connection, R resource) throws SQLException, ResourceNotFoundException
	{
		try
		{
			return updateWithTransaction(connection, resource, null);
		}
		catch (ResourceVersionNoMatchException e)
		{
			// should never be thrown if update is called with a null expectedVersion
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sets the version of the stored resource to latest version from DB plus 1.
	 *
	 * If the given expectedVersion is not <code>null</code>, checks if the given expectedVersion is the latest version
	 * in DB before storing the update.
	 *
	 * Resurrects all old versions (removes deleted flag) if the latest version in DB is marked as deleted.
	 *
	 * @param connection
	 *            not <code>null</code>, not {@link Connection#isReadOnly()} and not {@link Connection#getAutoCommit()}
	 *            and {@link Connection#getTransactionIsolation()} one of {@link Connection#TRANSACTION_REPEATABLE_READ}
	 *            or {@link Connection#TRANSACTION_SERIALIZABLE}
	 * @param resource
	 *            not <code>null</code>
	 * @param expectedVersion
	 *            may be <code>null</code>
	 * @return the stored resource, not the same object as the given resource (defensive copy)
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws ResourceNotFoundException
	 *             if the given resource could not be found
	 * @throws ResourceVersionNoMatchException
	 *             if the given expectedVersion is not <code>null</code> and is not the latest version
	 * @throws IllegalArgumentException
	 *             if the given connection is {@link Connection#isReadOnly()} or is {@link Connection#getAutoCommit()}
	 *             or {@link Connection#getTransactionIsolation()} is not one of
	 *             {@link Connection#TRANSACTION_REPEATABLE_READ} or {@link Connection#TRANSACTION_SERIALIZABLE}
	 */
	R updateWithTransaction(Connection connection, R resource, Long expectedVersion)
			throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException;

	/**
	 * Returns <code>false</code> if a matching resource was already marked as deleted
	 *
	 * @param uuid
	 *            may be <code>null</code>
	 * @return <code>true</code> if a resource with the given <b>uuid</b> could be found and marked as deleted,
	 *         <code>false</code> if a resource with the given <b>uuid</b> was already marked as deleted
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws ResourceNotFoundException
	 *             if the given <b>uuid</b> is <code>null</code> or no resource could be found with the given uuid
	 */
	boolean delete(UUID uuid) throws SQLException, ResourceNotFoundException;

	/**
	 * Returns <code>false</code> if a matching resource was already marked as deleted
	 *
	 * @param connection
	 *            not <code>null</code>, not {@link Connection#isReadOnly()}
	 * @param uuid
	 *            may be <code>null</code>
	 * @return <code>true</code> if a resource with the given <b>uuid</b> could be found and marked as deleted,
	 *         <code>false</code> if a resource with the given <b>uuid</b> was already marked as deleted
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws ResourceNotFoundException
	 *             if the given <b>uuid</b> is <code>null</code> or no resource could be found with the given uuid
	 */
	boolean deleteWithTransaction(Connection connection, UUID uuid) throws SQLException, ResourceNotFoundException;

	/**
	 * @param query
	 *            not <code>null</code>
	 * @return {@link PartialResult} that matched the search query
	 * @throws SQLException
	 *             if database access errors occur
	 */
	PartialResult<R> search(DbSearchQuery query) throws SQLException;

	/**
	 * @param connection
	 *            not <code>null</code>
	 * @param query
	 *            not <code>null</code>
	 * @return {@link PartialResult} that matched the search query
	 * @throws SQLException
	 *             if database access errors occur
	 */
	PartialResult<R> searchWithTransaction(Connection connection, DbSearchQuery query) throws SQLException;

	SearchQuery<R> createSearchQuery(User user, int page, int count);

	SearchQuery<R> createSearchQueryWithoutUserFilter(int page, int count);

	/**
	 * Permanently delete a resource that was previously marked as deleted.
	 *
	 * @param uuid
	 *            may be <code>null</code>
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws ResourceNotFoundException
	 *             if the given <b>uuid</b> is <code>null</code> or no resource could be found with the given uuid
	 * @throws ResourceNotMarkedDeletedException
	 *             if the resource was not marked as deleted
	 */
	void deletePermanently(UUID uuid) throws SQLException, ResourceNotFoundException, ResourceNotMarkedDeletedException;

	/**
	 * Permanently delete a resource that was previously marked as deleted.
	 *
	 * @param connection
	 *            not <code>null</code>, not {@link Connection#isReadOnly()}
	 * @param uuid
	 *            may be <code>null</code>
	 * @throws SQLException
	 *             if database access errors occur
	 * @throws ResourceNotFoundException
	 *             if the given <b>uuid</b> is <code>null</code> or no resource could be found with the given uuid
	 * @throws ResourceNotMarkedDeletedException
	 *             if the resource was not marked as deleted
	 */
	void deletePermanentlyWithTransaction(Connection connection, UUID uuid)
			throws SQLException, ResourceNotFoundException, ResourceNotMarkedDeletedException;
}
