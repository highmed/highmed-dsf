package org.highmed.dsf.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.hl7.fhir.r4.model.Organization;

public interface OrganizationDao extends ResourceDao<Organization>
{
	Optional<Organization> readActiveNotDeletedByThumbprint(String thumbprintHex) throws SQLException;

	/**
	 * Uses <i>http://highmed.org/sid/organization-identifier</i> as identifier system
	 *
	 * @param identifierValue
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if param <b>identifierValue</b> is null or {@link String#isBlank()}
	 * @throws SQLException
	 *             if database access errors occur
	 */
	Optional<Organization> readActiveNotDeletedByIdentifier(String identifierValue) throws SQLException;

	boolean existsNotDeletedByThumbprintWithTransaction(Connection connection, String thumbprintHex)
			throws SQLException;
}
