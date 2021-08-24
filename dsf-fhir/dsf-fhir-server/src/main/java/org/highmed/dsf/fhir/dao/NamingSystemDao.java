package org.highmed.dsf.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.hl7.fhir.r4.model.NamingSystem;

public interface NamingSystemDao extends ResourceDao<NamingSystem>
{
	Optional<NamingSystem> readByName(String name) throws SQLException;

	Optional<NamingSystem> readByNameWithTransaction(Connection connection, String name) throws SQLException;

	boolean existsWithUniqueIdUriEntry(Connection connection, String uniqueIdValue) throws SQLException;

	boolean existsWithUniqueIdUriEntryResolvable(Connection connection, String uniqueIdValue) throws SQLException;
}
