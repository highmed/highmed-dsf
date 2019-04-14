package org.highmed.fhir.dao;

import java.sql.SQLException;
import java.util.Optional;

import org.hl7.fhir.r4.model.DomainResource;

public interface ReadByUrlDao<R extends DomainResource>
{
	Optional<R> readByUrl(String urlAndVersion) throws SQLException;
}
