package org.highmed.fhir.dao;

import java.sql.SQLException;
import java.util.List;

import org.hl7.fhir.r4.model.StructureDefinition;

public interface StructureDefinitionDaoBase
		extends DomainResourceDao<StructureDefinition>, ReadByUrlDao<StructureDefinition>
{
	List<StructureDefinition> readAll() throws SQLException;
}
