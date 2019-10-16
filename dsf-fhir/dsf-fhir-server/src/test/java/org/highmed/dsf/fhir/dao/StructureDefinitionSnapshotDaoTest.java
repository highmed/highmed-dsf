package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.converter.SnapshotInfoConverter;
import org.highmed.dsf.fhir.dao.jdbc.StructureDefinitionSnapshotDaoJdbc;
import org.highmed.dsf.fhir.spring.config.JsonConfig;
import org.hl7.fhir.r4.model.StructureDefinition;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

public class StructureDefinitionSnapshotDaoTest
		extends AbstractResourceDaoTest<StructureDefinition, StructureDefinitionSnapshotDao>
{
	private static final String name = "StructureDefinitionSnapshot";
	private static final String title = "Demo Structure Definition Snapshot";

	private final ObjectMapper objectMapper = new JsonConfig().objectMapper();

	public StructureDefinitionSnapshotDaoTest()
	{
		super(StructureDefinition.class);
	}

	@Override
	protected StructureDefinitionSnapshotDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new StructureDefinitionSnapshotDaoJdbc(dataSource, fhirContext, new SnapshotInfoConverter(objectMapper));
	}

	@Override
	protected StructureDefinition createResource()
	{
		StructureDefinition structureDefinition = new StructureDefinition();
		structureDefinition.setName(name);
		return structureDefinition;
	}

	@Override
	protected void checkCreated(StructureDefinition resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected StructureDefinition updateResource(StructureDefinition resource)
	{
		resource.setTitle(title);
		return resource;
	}

	@Override
	protected void checkUpdates(StructureDefinition resource)
	{
		assertEquals(title, resource.getTitle());
	}

	// TODO

	// @Test
	// public void testUpdateSnapshotInfo() throws Exception
	// {
	// StructureDefinition snapshot = dao.create(createResource());
	//
	// try (Connection connection = database.getDataSource().getConnection();
	// PreparedStatement statement = connection.prepareStatement(
	// "SELECT structure_definition_snapshot_info FROM structure_definition_snapshots WHERE
	// structure_definition_snapshot_id = ?"))
	// {
	// statement.setObject(1, dao.uuidToPgObject(UUID.fromString(snapshot.getIdElement().getIdPart())));
	// try (ResultSet result = statement.executeQuery())
	// {
	// assertTrue(result.next());
	// assertNull(result.getString(1));
	// }
	// }
	//
	// final String profile0 = "profile0";
	// final String profile1 = "profile1";
	// final String targetProfile0 = "targetProfile0";
	// final String targetProfile1 = "targetProfile1";
	//
	// SnapshotDependencies dependencies = new SnapshotDependencies(Arrays.asList(profile0, profile1),
	// Arrays.asList(targetProfile0, targetProfile1));
	// SnapshotInfo info = new SnapshotInfo(dependencies);
	//
	// dao.updateSnapshotInfo(UUID.fromString(snapshot.getIdElement().getIdPart()), info);
	//
	// try (Connection connection = database.getDataSource().getConnection();
	// PreparedStatement statement = connection.prepareStatement(
	// "SELECT structure_definition_snapshot_info FROM structure_definition_snapshots WHERE
	// structure_definition_snapshot_id = ?"))
	// {
	// statement.setObject(1, dao.uuidToPgObject(UUID.fromString(snapshot.getIdElement().getIdPart())));
	// try (ResultSet result = statement.executeQuery())
	// {
	// assertTrue(result.next());
	// String readInfoString = result.getString(1);
	// assertNotNull(readInfoString);
	//
	// SnapshotInfo readInfo = objectMapper.reader().forType(SnapshotInfo.class).readValue(readInfoString);
	// assertNotNull(readInfo);
	//
	// assertNotNull(readInfo);
	// assertNotNull(readInfo.getDependencies());
	// assertNotNull(readInfo.getDependencies().getProfiles());
	// assertEquals(2, readInfo.getDependencies().getProfiles().size());
	// assertEquals(profile0, readInfo.getDependencies().getProfiles().get(0));
	// assertEquals(profile1, readInfo.getDependencies().getProfiles().get(1));
	// assertNotNull(readInfo.getDependencies().getTargetProfiles());
	// assertEquals(2, readInfo.getDependencies().getTargetProfiles().size());
	// assertEquals(targetProfile0, readInfo.getDependencies().getTargetProfiles().get(0));
	// assertEquals(targetProfile1, readInfo.getDependencies().getTargetProfiles().get(1));
	// }
	// }
	// }
	//
	// @Test
	// public void testDeleteAllByDependency() throws Exception
	// {
	// StructureDefinition snapshot1a = dao.create(createResource());
	// StructureDefinition snapshot1b = dao.create(createResource());
	// StructureDefinition snapshot2 = dao.create(createResource());
	//
	// try (Connection connection = database.getDataSource().getConnection();
	// PreparedStatement statement = connection.prepareStatement(
	// "SELECT count(*) FROM structure_definition_snapshots WHERE structure_definition_snapshot_info IS NULL"))
	// {
	// try (ResultSet result = statement.executeQuery())
	// {
	// assertTrue(result.next());
	// assertEquals(3, result.getInt(1));
	// }
	// }
	//
	// final String profile10 = "profile10";
	// final String profile11 = "profile11";
	// final String targetProfile10 = "targetProfile10";
	// final String targetProfile11 = "targetProfile11";
	//
	// SnapshotDependencies dependencies1a = new SnapshotDependencies(Arrays.asList(profile10, profile11),
	// Arrays.asList(targetProfile10, targetProfile11));
	// SnapshotInfo info1a = new SnapshotInfo(dependencies1a);
	// dao.updateSnapshotInfo(UUID.fromString(snapshot1a.getIdElement().getIdPart()), info1a);
	//
	// SnapshotDependencies dependencies1b = new SnapshotDependencies(Arrays.asList(profile10),
	// Arrays.asList(targetProfile10));
	// SnapshotInfo info1b = new SnapshotInfo(dependencies1b);
	// dao.updateSnapshotInfo(UUID.fromString(snapshot1b.getIdElement().getIdPart()), info1b);
	//
	// final String profile20 = "profile20";
	// final String profile21 = "profile21";
	// final String targetProfile20 = "targetProfile20";
	// final String targetProfile21 = "targetProfile21";
	//
	// SnapshotDependencies dependencies2 = new SnapshotDependencies(Arrays.asList(profile20, profile21),
	// Arrays.asList(targetProfile20, targetProfile21));
	// SnapshotInfo info2 = new SnapshotInfo(dependencies2);
	// dao.updateSnapshotInfo(UUID.fromString(snapshot2.getIdElement().getIdPart()), info2);
	//
	// try (Connection connection = database.getDataSource().getConnection();
	// PreparedStatement statement = connection.prepareStatement(
	// "SELECT count(*) FROM structure_definition_snapshots WHERE structure_definition_snapshot_info IS NOT NULL"))
	// {
	// try (ResultSet result = statement.executeQuery())
	// {
	// assertTrue(result.next());
	// assertEquals(3, result.getInt(1));
	// }
	// }
	//
	// try (Connection connection = database.getDataSource().getConnection();
	// PreparedStatement statement = connection.prepareStatement(
	// "SELECT count(*) FROM structure_definition_snapshots WHERE
	// structure_definition_snapshot_info->'dependencies'->'profiles' ?? ?"))
	// {
	// statement.setString(1, profile10);
	//
	// try (ResultSet result = statement.executeQuery())
	// {
	// assertTrue(result.next());
	// assertEquals(2, result.getInt(1));
	// }
	// }
	//
	// dao.deleteAllByDependency(profile10);
	//
	// try (Connection connection = database.getDataSource().getConnection();
	// PreparedStatement statement = connection
	// .prepareStatement("SELECT count(*) FROM structure_definition_snapshots"))
	// {
	// try (ResultSet result = statement.executeQuery())
	// {
	// assertTrue(result.next());
	// assertEquals(1, result.getInt(1));
	// }
	// }
	// }
}
