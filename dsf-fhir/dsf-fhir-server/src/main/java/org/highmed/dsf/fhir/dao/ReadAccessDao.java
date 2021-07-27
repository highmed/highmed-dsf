package org.highmed.dsf.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.highmed.dsf.fhir.authentication.UserRole;

public interface ReadAccessDao
{
	/**
	 * @param connection
	 *            not <code>null</code>
	 * @param resourceId
	 *            not <code>null</code>
	 * @param role
	 *            not <code>null</code>
	 * @param organizationId
	 *            not <code>null</code>
	 * @return
	 * @throws SQLException
	 */
	List<String> getAccessTypes(Connection connection, UUID resourceId, UserRole role, UUID organizationId)
			throws SQLException;
}
