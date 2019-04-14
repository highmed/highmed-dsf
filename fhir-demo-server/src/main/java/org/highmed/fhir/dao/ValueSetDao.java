package org.highmed.fhir.dao;

import java.sql.SQLException;
import java.util.Optional;

import org.hl7.fhir.r4.model.ValueSet;

public interface ValueSetDao extends DomainResourceDao<ValueSet>
{
	Optional<ValueSet> readByUrl(String urlAndVersion) throws SQLException;
}
