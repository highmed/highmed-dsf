package org.highmed.fhir.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.StructureDefinition;

public interface StructureDefinitionDaoBase extends DomainResourceDao<StructureDefinition>
{
	List<StructureDefinition> readAll() throws SQLException;

	Optional<StructureDefinition> readByUrl(String urlAndVersion) throws SQLException;
}
