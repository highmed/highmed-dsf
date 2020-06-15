package org.highmed.dsf.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.hl7.fhir.r4.model.DomainResource;

public interface ReadByUrlDao<R extends DomainResource>
{
	Optional<R> readByUrlAndVersion(String urlAndVersion) throws SQLException;

	Optional<R> readByUrlAndVersion(String url, String version) throws SQLException;

	Optional<R> readByUrlAndVersionWithTransaction(Connection connection, String urlAndVersion) throws SQLException;

	Optional<R> readByUrlAndVersionWithTransaction(Connection connection, String url, String version)
			throws SQLException;
}
