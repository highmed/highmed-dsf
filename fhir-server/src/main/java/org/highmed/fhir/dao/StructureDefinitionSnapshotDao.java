package org.highmed.fhir.dao;

import java.sql.SQLException;
import java.util.UUID;

import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.service.SnapshotInfo;
import org.hl7.fhir.r4.model.StructureDefinition;

public interface StructureDefinitionSnapshotDao extends StructureDefinitionDaoBase
{
	StructureDefinition create(UUID uuid, StructureDefinition resource, SnapshotInfo info) throws SQLException;

	StructureDefinition update(StructureDefinition resource, SnapshotInfo info)
			throws SQLException, ResourceNotFoundException;

	void deleteAllByDependency(String url) throws SQLException;
}
