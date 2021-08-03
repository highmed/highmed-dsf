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
	 * @param version
	 *            <code>&gt; 0</code>
	 * @param role
	 *            not <code>null</code>
	 * @param organizationId
	 *            not <code>null</code>
	 * @return Distinct list of access types found for the given parameters, empty list means no read access
	 * @throws SQLException
	 *             if database access errors occur
	 */
	List<String> getAccessTypes(Connection connection, UUID resourceId, long version, UserRole role,
			UUID organizationId) throws SQLException;
}
