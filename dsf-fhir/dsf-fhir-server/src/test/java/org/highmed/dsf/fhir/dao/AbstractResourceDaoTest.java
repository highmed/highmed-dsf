package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;
import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.dsf.fhir.dao.exception.ResourceVersionNoMatchException;
import org.highmed.dsf.fhir.test.FhirEmbeddedPostgresWithLiquibase;
import org.highmed.dsf.fhir.test.TestSuiteDbTests;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import de.rwh.utils.test.Database;

public abstract class AbstractResourceDaoTest<D extends Resource, C extends ResourceDao<D>>
{
	@ClassRule
	public static final FhirEmbeddedPostgresWithLiquibase template = new FhirEmbeddedPostgresWithLiquibase(
			TestSuiteDbTests.template);

	@Rule
	public final Database database = new Database(template);

	protected final Class<D> resouceClass;

	protected final FhirContext fhirContext = FhirContext.forR4();
	protected C ttpDao, medicDao;

	protected AbstractResourceDaoTest(Class<D> resouceClass)
	{
		this.resouceClass = resouceClass;
	}

	@Before
	public void before() throws Exception
	{
		ttpDao = createDao(database.getDataSource(), fhirContext, OrganizationType.TTP);
		medicDao = createDao(database.getDataSource(), fhirContext, OrganizationType.MeDIC);
	}

	protected abstract C createDao(BasicDataSource dataSource, FhirContext fhirContext,
			OrganizationType organizationType);

	@Test
	public void testEmptyForTtp() throws Exception
	{
		Optional<D> read = ttpDao.read(UUID.randomUUID());
		assertTrue(read.isEmpty());
	}

	@Test
	public void testEmptyForMedic() throws Exception
	{
		testEmpty(medicDao);
	}

	@Test
	public void testEmptyWithVersion() throws Exception
	{
		testEmpty(ttpDao);
	}

	private void testEmpty(C dao) throws SQLException
	{
		Optional<D> read = dao.readVersion(UUID.randomUUID(), 1L);
		assertTrue(read.isEmpty());
	}

	protected abstract D createResource();

	@Test
	public void testCreateForTtp() throws Exception
	{
		testCreate(ttpDao);
	}

	@Test
	public void testCreateForMedic() throws Exception
	{
		testCreate(medicDao);
	}

	private void testCreate(C dao) throws SQLException, ResourceDeletedException
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());
		assertEquals("1", createdResource.getIdElement().getVersionIdPart());
		assertEquals("1", createdResource.getMeta().getVersionId());

		newResource.setIdElement(createdResource.getIdElement().copy());
		newResource.setMeta(createdResource.getMeta().copy());

		assertTrue(newResource.equalsDeep(createdResource));

		Optional<D> read = dao.read(UUID.fromString(createdResource.getIdElement().getIdPart()));
		assertTrue(read.isPresent());
		assertNotNull(read.get().getId());
		assertNotNull(read.get().getMeta().getVersionId());
		assertEquals("1", read.get().getIdElement().getVersionIdPart());
		assertEquals("1", read.get().getMeta().getVersionId());
	}

	protected abstract void checkCreated(D resource);

	protected abstract D updateResource(D resource);

	@Test
	public void testUpdateForTtp() throws Exception
	{
		testUpdate(ttpDao);
	}

	@Test
	public void testUpdateForMedic() throws Exception
	{
		testUpdate(medicDao);
	}

	private void testUpdate(C dao) throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());

		newResource.setIdElement(createdResource.getIdElement().copy());
		newResource.setMeta(createdResource.getMeta().copy());

		assertTrue(newResource.equalsDeep(createdResource));

		D updatedResource = dao.update(updateResource(createdResource), null);
		assertNotNull(updatedResource);

		checkUpdates(updatedResource);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testUpdateNonExistingForTtp() throws Exception
	{
		testUpdateNonExisting(ttpDao);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testUpdateNonExistingForMedic() throws Exception
	{
		testUpdateNonExisting(medicDao);
	}

	private void testUpdateNonExisting(C dao)
			throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		dao.update(newResource, null);
	}

	@Test(expected = ResourceVersionNoMatchException.class)
	public void testUpdateNotLatestForTtp() throws Exception
	{
		testUpdateNotLatest(ttpDao);
	}

	@Test(expected = ResourceVersionNoMatchException.class)
	public void testUpdateNotLatestForMedic() throws Exception
	{
		testUpdateNotLatest(medicDao);
	}

	private void testUpdateNotLatest(C dao)
			throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());

		newResource.setIdElement(createdResource.getIdElement().copy());
		newResource.setMeta(createdResource.getMeta().copy());

		assertTrue(newResource.equalsDeep(createdResource));

		dao.update(updateResource(createdResource), 0L);
	}

	@Test
	public void testUpdateLatestForTtp() throws Exception
	{
		testUpdateLatest(ttpDao);
	}

	@Test
	public void testUpdateLatestForMedic() throws Exception
	{
		testUpdateLatest(medicDao);
	}

	private void testUpdateLatest(C dao) throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());

		newResource.setIdElement(createdResource.getIdElement().copy());
		newResource.setMeta(createdResource.getMeta().copy());

		assertTrue(newResource.equalsDeep(createdResource));

		D updatedResource = dao.update(updateResource(createdResource), 1L);
		assertNotNull(updatedResource);
	}

	protected abstract void checkUpdates(D resource);

	@Test(expected = ResourceDeletedException.class)
	public void testDeleteForTtp() throws Exception
	{
		testDelete(ttpDao);
	}

	@Test(expected = ResourceDeletedException.class)
	public void testDeleteForMedic() throws Exception
	{
		testDelete(medicDao);
	}

	private void testDelete(C dao) throws SQLException, ResourceDeletedException, ResourceNotFoundException
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());

		newResource.setIdElement(createdResource.getIdElement().copy());
		newResource.setMeta(createdResource.getMeta().copy());

		assertTrue(newResource.equalsDeep(createdResource));

		Optional<D> read = dao.read(UUID.fromString(createdResource.getIdElement().getIdPart()));
		assertTrue(read.isPresent());

		dao.delete(UUID.fromString(createdResource.getIdElement().getIdPart()));

		dao.read(UUID.fromString(createdResource.getIdElement().getIdPart()));
	}

	@Test
	public void testReadWithVersionForTtp() throws Exception
	{
		testReadWithVersion(ttpDao);
	}

	@Test
	public void testReadWithVersionFotMedic() throws Exception
	{
		testReadWithVersion(medicDao);
	}

	private void testReadWithVersion(C dao) throws SQLException
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());

		newResource.setIdElement(createdResource.getIdElement().copy());
		newResource.setMeta(createdResource.getMeta().copy());

		assertTrue(newResource.equalsDeep(createdResource));

		Optional<D> read = dao.readVersion(UUID.fromString(createdResource.getIdElement().getIdPart()),
				createdResource.getIdElement().getVersionIdPartAsLong());
		assertTrue(read.isPresent());

		assertTrue(newResource.equalsDeep(read.get()));
	}

	@Test
	public void testReadForTtp() throws Exception
	{
		testRead(ttpDao);
	}

	@Test
	public void testReadForMedic() throws Exception
	{
		testRead(medicDao);
	}

	private void testRead(C dao) throws SQLException, ResourceDeletedException
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());

		newResource.setIdElement(createdResource.getIdElement().copy());
		newResource.setMeta(createdResource.getMeta().copy());

		assertTrue(newResource.equalsDeep(createdResource));

		Optional<D> read = dao.read(UUID.fromString(createdResource.getIdElement().getIdPart()));
		assertTrue(read.isPresent());

		String s1 = fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(newResource);
		String s2 = fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(read.get());
		assertTrue(s1 + "\nvs\n" + s2, newResource.equalsDeep(read.get()));
	}

	@Test
	public void testReadLatestForTtp() throws Exception
	{
		testReadLatest(ttpDao);
	}

	@Test
	public void testReadLatestForMedic() throws Exception
	{
		testReadLatest(medicDao);
	}

	private void testReadLatest(C dao)
			throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException, ResourceDeletedException
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());
		assertEquals("1", createdResource.getIdElement().getVersionIdPart());

		D updatedResource = dao.update(createdResource, null);
		assertNotNull(updatedResource);
		assertNotNull(updatedResource.getId());
		assertNotNull(updatedResource.getMeta().getVersionId());
		assertEquals("2", updatedResource.getIdElement().getVersionIdPart());

		D updatedResource2 = dao.update(updatedResource, null);
		assertNotNull(updatedResource2);
		assertNotNull(updatedResource2.getId());
		assertNotNull(updatedResource2.getMeta().getVersionId());
		assertEquals("3", updatedResource2.getIdElement().getVersionIdPart());

		newResource.setIdElement(updatedResource2.getIdElement().copy());
		newResource.setMeta(updatedResource2.getMeta().copy());

		assertTrue(newResource.equalsDeep(updatedResource2));

		Optional<D> read = dao.read(UUID.fromString(updatedResource2.getIdElement().getIdPart()));
		assertTrue(read.isPresent());

		String s1 = fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(updatedResource2);
		String s2 = fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(read.get());
		assertTrue(s1 + "\nvs\n" + s2, updatedResource2.equalsDeep(read.get()));
		assertEquals("3", read.get().getIdElement().getVersionIdPart());
	}

	@Test
	public void testUpdateSameRowForTtp() throws Exception
	{
		testUpdateSameRow(ttpDao);
	}

	@Test
	public void testUpdateSameRowForMedic() throws Exception
	{
		testUpdateSameRow(medicDao);
	}

	private void testUpdateSameRow(C dao) throws SQLException, ResourceNotFoundException, ResourceDeletedException
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());

		newResource.setIdElement(createdResource.getIdElement().copy());
		newResource.setMeta(createdResource.getMeta().copy());

		assertTrue(newResource.equalsDeep(createdResource));

		D updateResource = updateResource(createdResource);
		try (Connection connection = dao.getNewTransaction())
		{
			D updatedResource = dao.updateSameRowWithTransaction(connection, updateResource);

			connection.commit();

			assertNotNull(updatedResource);
			assertNotNull(updatedResource.getId());
			assertNotNull(updatedResource.getMeta().getVersionId());

			assertEquals(createdResource.getIdElement().getIdPart(), updatedResource.getIdElement().getIdPart());
			assertEquals(createdResource.getMeta().getVersionId(), updatedResource.getMeta().getVersionId());
		}

		Optional<D> read = dao.read(UUID.fromString(createdResource.getIdElement().getIdPart()));
		assertTrue(read.isPresent());

		assertTrue(
				fhirContext.newXmlParser().encodeResourceToString(read.get()) + "\nvs.\n"
						+ fhirContext.newXmlParser().encodeResourceToString(updateResource),
				read.get().equalsDeep(updateResource));
		assertEquals(createdResource.getIdElement().getIdPart(), read.get().getIdElement().getIdPart());
		assertEquals(createdResource.getMeta().getVersionId(), read.get().getMeta().getVersionId());
	}
}
