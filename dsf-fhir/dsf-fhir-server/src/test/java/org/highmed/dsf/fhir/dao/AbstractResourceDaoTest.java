package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.dbcp2.BasicDataSource;
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
	protected C dao;

	protected AbstractResourceDaoTest(Class<D> resouceClass)
	{
		this.resouceClass = resouceClass;
	}

	@Before
	public void before() throws Exception
	{
		dao = createDao(database.getDataSource(), fhirContext);
	}

	protected abstract C createDao(BasicDataSource dataSource, FhirContext fhirContext);

	@Test
	public void testEmpty() throws Exception
	{
		Optional<D> read = dao.read(UUID.randomUUID());
		assertTrue(read.isEmpty());
	}

	@Test
	public void testEmptyWithVersion() throws Exception
	{
		Optional<D> read = dao.readVersion(UUID.randomUUID(), 1L);
		assertTrue(read.isEmpty());
	}

	protected abstract D createResource();

	@Test
	public void testCreate() throws Exception
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
	public void testUpdate() throws Exception
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
	public void testUpdateNonExisting() throws Exception
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		dao.update(newResource, null);
	}

	@Test(expected = ResourceVersionNoMatchException.class)
	public void testUpdateNotLatest() throws Exception
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
	public void testUpdateLatest() throws Exception
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
	public void testDelete() throws Exception
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
	public void testReadWithVersion() throws Exception
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
	public void testRead() throws Exception
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
	public void testReadLatest() throws Exception
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
	public void testUpdateSameRow() throws Exception
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
