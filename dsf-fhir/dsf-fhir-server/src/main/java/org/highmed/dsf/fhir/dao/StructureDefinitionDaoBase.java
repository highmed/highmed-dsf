package org.highmed.dsf.fhir.dao;

import java.sql.SQLException;
import java.util.List;

import org.hl7.fhir.r4.model.StructureDefinition;

public interface StructureDefinitionDaoBase
		extends ResourceDao<StructureDefinition>, ReadByUrlDao<StructureDefinition>
{
	List<StructureDefinition> readAll() throws SQLException;
}
