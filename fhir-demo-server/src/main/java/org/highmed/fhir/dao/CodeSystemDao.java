package org.highmed.fhir.dao;

import java.sql.SQLException;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeSystem;

public interface CodeSystemDao extends DomainResourceDao<CodeSystem>
{
	Optional<CodeSystem> readByUrl(String urlAndVersion) throws SQLException;
}
