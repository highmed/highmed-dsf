package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.hl7.fhir.r4.model.Resource;

public interface AuthorizationRule<R extends Resource>
{
	Class<R> getResourceType();

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 *
	 * @param user
	 *            not <code>null</code>
	 * @param newResource
	 *            not <code>null</code>
	 * @return Reason as String in {@link Optional#of(Object)} if create allowed
	 */
	Optional<String> reasonCreateAllowed(User user, R newResource);

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 *
	 * @param connection
	 *            not <code>null</code>
	 * @param user
	 *            not <code>null</code>
	 * @param newResource
	 *            not <code>null</code>
	 * @return Reason as String in {@link Optional#of(Object)} if create allowed
	 */
	Optional<String> reasonCreateAllowed(Connection connection, User user, R newResource);

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 *
	 * @param user
	 *            not <code>null</code>
	 * @param existingResource
	 *            not <code>null</code>
	 * @return Reason as String in {@link Optional#of(Object)} if read allowed
	 */
	Optional<String> reasonReadAllowed(User user, R existingResource);

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 *
	 * @param connection
	 *            not <code>null</code>
	 * @param user
	 *            not <code>null</code>
	 * @param existingResource
	 *            not <code>null</code>
	 * @return Reason as String in {@link Optional#of(Object)} if read allowed
	 */
	Optional<String> reasonReadAllowed(Connection connection, User user, R existingResource);

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 *
	 * @param user
	 *            not <code>null</code>
	 * @param oldResource
	 *            not <code>null</code>
	 * @param newResource
	 *            not <code>null</code>
	 * @return Reason as String in {@link Optional#of(Object)} if update allowed
	 */
	Optional<String> reasonUpdateAllowed(User user, R oldResource, R newResource);

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 *
	 * @param connection
	 *            not <code>null</code>
	 * @param user
	 *            not <code>null</code>
	 * @param oldResource
	 *            not <code>null</code>
	 * @param newResource
	 *            not <code>null</code>
	 * @return Reason as String in {@link Optional#of(Object)} if update allowed
	 */
	Optional<String> reasonUpdateAllowed(Connection connection, User user, R oldResource, R newResource);

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 *
	 * @param user
	 *            not <code>null</code>
	 * @param oldResource
	 *            not <code>null</code>
	 * @return Reason as String in {@link Optional#of(Object)} if delete allowed
	 */
	Optional<String> reasonDeleteAllowed(User user, R oldResource);

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 *
	 * @param connection
	 *            not <code>null</code>
	 * @param user
	 *            not <code>null</code>
	 * @param oldResource
	 *            not <code>null</code>
	 * @return Reason as String in {@link Optional#of(Object)} if delete allowed
	 */
	Optional<String> reasonDeleteAllowed(Connection connection, User user, R oldResource);

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 *
	 * @param user
	 *            not <code>null</code>
	 * @return Reason as String in {@link Optional#of(Object)} if delete allowed
	 */
	Optional<String> reasonSearchAllowed(User user);

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 *
	 * @param user
	 *            not <code>null</code>
	 * @return Reason as String in {@link Optional#of(Object)} if delete allowed
	 */
	Optional<String> reasonHistoryAllowed(User user);

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 *
	 * @param user
	 *            not <code>null</code>
	 * @param oldResource
	 *            not <code>null</code>
	 * @return Reason as String in {@link Optional#of(Object)} if permanent delete allowed
	 */
	Optional<String> reasonPermanentDeleteAllowed(User user, R oldResource);

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 *
	 * @param connection
	 *            not <code>null</code>
	 * @param user
	 *            not <code>null</code>
	 * @param oldResource
	 *            not <code>null</code>
	 * @return Reason as String in {@link Optional#of(Object)} if permanent delete allowed
	 */
	Optional<String> reasonPermanentDeleteAllowed(Connection connection, User user, R oldResource);
}