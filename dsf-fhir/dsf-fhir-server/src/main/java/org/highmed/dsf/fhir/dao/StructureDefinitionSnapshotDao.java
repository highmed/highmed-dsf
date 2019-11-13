package org.highmed.dsf.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.dsf.fhir.service.SnapshotInfo;
import org.hl7.fhir.r4.model.StructureDefinition;

public interface StructureDefinitionSnapshotDao extends StructureDefinitionDaoBase
{
	StructureDefinition create(UUID uuid, StructureDefinition resource, SnapshotInfo info) throws SQLException;

	StructureDefinition createWithTransaction(Connection connection, UUID uuid, StructureDefinition resource,
			SnapshotInfo info) throws SQLException;

	StructureDefinition update(StructureDefinition resource, SnapshotInfo info)
			throws SQLException, ResourceNotFoundException;

	StructureDefinition updateWithTransaction(Connection connection, StructureDefinition resource, SnapshotInfo info)
			throws SQLException, ResourceNotFoundException;

	void deleteAllByDependency(String url) throws SQLException;

	void deleteAllByDependencyWithTransaction(Connection connection, String url) throws SQLException;
}
