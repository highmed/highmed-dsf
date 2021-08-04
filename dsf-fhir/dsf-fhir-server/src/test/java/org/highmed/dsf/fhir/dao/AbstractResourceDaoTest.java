package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;
import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.dsf.fhir.dao.exception.ResourceNotMarkedDeletedException;
import org.highmed.dsf.fhir.dao.exception.ResourceVersionNoMatchException;
import org.hl7.fhir.r4.model.Resource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import de.rwh.utils.test.LiquibaseTemplateTestClassRule;
import de.rwh.utils.test.LiquibaseTemplateTestRule;

public abstract class AbstractResourceDaoTest<D extends Resource, C extends ResourceDao<D>> extends AbstractDbTest
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractResourceDaoTest.class);

	@FunctionalInterface
	public interface TriFunction<A, B, C, R>
	{
		R apply(A a, B b, C c);
	}

	public static final String DAO_DB_TEMPLATE_NAME = "dao_template";

	protected static final BasicDataSource adminDataSource = createAdminBasicDataSource();
	protected static final BasicDataSource liquibaseDataSource = createLiquibaseDataSource();
	protected static final BasicDataSource defaultDataSource = createDefaultDataSource();
	protected static final BasicDataSource permanentDeleteDataSource = createPermanentDeleteDataSource();

	@ClassRule
	public static final LiquibaseTemplateTestClassRule liquibaseRule = new LiquibaseTemplateTestClassRule(
			adminDataSource, LiquibaseTemplateTestClassRule.DEFAULT_TEST_DB_NAME, DAO_DB_TEMPLATE_NAME,
			liquibaseDataSource, CHANGE_LOG_FILE, CHANGE_LOG_PARAMETERS, true);

	@BeforeClass
	public static void beforeClass() throws Exception
	{
		defaultDataSource.start();
		liquibaseDataSource.start();
		adminDataSource.start();
		permanentDeleteDataSource.start();
	}

	@AfterClass
	public static void afterClass() throws Exception
	{
		defaultDataSource.close();
		liquibaseDataSource.close();
		adminDataSource.close();
		permanentDeleteDataSource.close();
	}

	@Rule
	public final LiquibaseTemplateTestRule templateRule = new LiquibaseTemplateTestRule(adminDataSource,
			LiquibaseTemplateTestClassRule.DEFAULT_TEST_DB_NAME, DAO_DB_TEMPLATE_NAME);

	protected final Class<D> resouceClass;
	protected final TriFunction<DataSource, DataSource, FhirContext, C> daoCreator;

	protected final FhirContext fhirContext = FhirContext.forR4();
	protected C dao;

	protected AbstractResourceDaoTest(Class<D> resouceClass,
			TriFunction<DataSource, DataSource, FhirContext, C> daoCreator)
	{
		this.resouceClass = resouceClass;
		this.daoCreator = daoCreator;
	}

	@Before
	public void before() throws Exception
	{
		dao = daoCreator.apply(defaultDataSource, permanentDeleteDataSource, fhirContext);
	}

	public C getDao()
	{
		return dao;
	}

	public FhirContext getFhirContext()
	{
		return fhirContext;
	}

	public BasicDataSource getDefaultDataSource()
	{
		return defaultDataSource;
	}

	public BasicDataSource getPermanentDeleteDataSource()
	{
		return permanentDeleteDataSource;
	}

	public Logger getLogger()
	{
		return logger;
	}

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

	public abstract D createResource();

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
		assertNotNull(createdResource.getIdElement());
		assertNotNull(createdResource.getIdElement().getIdPart());
		assertNotNull(createdResource.getIdElement().getVersionIdPart());
		assertEquals(ResourceDao.FIRST_VERSION_STRING, createdResource.getIdElement().getVersionIdPart());
		assertNotNull(createdResource.getMeta());
		assertNotNull(createdResource.getMeta().getVersionId());
		assertEquals(ResourceDao.FIRST_VERSION_STRING, createdResource.getMeta().getVersionId());

		newResource.setIdElement(createdResource.getIdElement().copy());
		newResource.setMeta(createdResource.getMeta().copy());
		assertTrue(newResource.equalsDeep(createdResource));

		D updatedResource = dao.update(updateResource(createdResource), (long) ResourceDao.FIRST_VERSION);
		assertNotNull(updatedResource);
		assertNotNull(updatedResource.getIdElement());
		assertNotNull(updatedResource.getIdElement().getIdPart());
		assertNotNull(updatedResource.getIdElement().getVersionIdPart());
		assertEquals(String.valueOf(ResourceDao.FIRST_VERSION + 1), updatedResource.getIdElement().getVersionIdPart());
		assertNotNull(updatedResource.getMeta());
		assertNotNull(updatedResource.getMeta().getVersionId());
		assertEquals(String.valueOf(ResourceDao.FIRST_VERSION + 1), updatedResource.getMeta().getVersionId());
		assertTrue(updatedResource.getMeta().getLastUpdated().after(createdResource.getMeta().getLastUpdated()));

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

	@Test
	public void testUpdateDeleted() throws Exception
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getIdElement());
		assertNotNull(createdResource.getIdElement().getIdPart());
		assertNotNull(createdResource.getIdElement().getVersionIdPart());
		assertEquals(ResourceDao.FIRST_VERSION_STRING, createdResource.getIdElement().getVersionIdPart());
		assertNotNull(createdResource.getMeta());
		assertNotNull(createdResource.getMeta().getVersionId());
		assertEquals(ResourceDao.FIRST_VERSION_STRING, createdResource.getMeta().getVersionId());

		newResource.setIdElement(createdResource.getIdElement().copy());
		newResource.setMeta(createdResource.getMeta().copy());
		assertTrue(newResource.equalsDeep(createdResource));

		boolean deleted = dao.delete(UUID.fromString(createdResource.getIdElement().getIdPart()));
		assertTrue(deleted);

		D updatedResource = dao.update(updateResource(createdResource), (long) ResourceDao.FIRST_VERSION + 1L);
		assertNotNull(updatedResource);
		assertNotNull(updatedResource.getIdElement());
		assertNotNull(updatedResource.getIdElement().getIdPart());
		assertNotNull(updatedResource.getIdElement().getVersionIdPart());
		assertEquals(String.valueOf(ResourceDao.FIRST_VERSION + 2), updatedResource.getIdElement().getVersionIdPart());
		assertNotNull(updatedResource.getMeta());
		assertNotNull(updatedResource.getMeta().getVersionId());
		assertEquals(String.valueOf(ResourceDao.FIRST_VERSION + 2), updatedResource.getMeta().getVersionId());

		checkUpdates(updatedResource);
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

	@Test(expected = ResourceNotMarkedDeletedException.class)
	public void testDeletePermanentlyNotMarkedAsDeleted() throws Exception
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());

		dao.deletePermanently(UUID.fromString(createdResource.getIdElement().getIdPart()));
	}

	@Test
	public void testDeletePermanently() throws Exception
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());

		dao.delete(UUID.fromString(createdResource.getIdElement().getIdPart()));

		dao.deletePermanently(UUID.fromString(createdResource.getIdElement().getIdPart()));

		assertFalse(dao.read(UUID.fromString(createdResource.getIdElement().getIdPart())).isPresent());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testDeletePermanentlyNotFound() throws Exception
	{
		dao.deletePermanently(UUID.randomUUID());
	}

	@Test
	public void testReadIncludingDeleted() throws Exception
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

		boolean d = dao.delete(UUID.fromString(createdResource.getIdElement().getIdPart()));
		assertTrue(d);

		Optional<D> deleted = dao.readIncludingDeleted(UUID.fromString(createdResource.getIdElement().getIdPart()));
		assertTrue(deleted.isPresent());
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
	public void testReadAll() throws Exception
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		D createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getIdElement());
		assertNotNull(createdResource.getIdElement().getIdPart());
		assertNotNull(createdResource.getIdElement().getVersionIdPart());
		assertEquals(ResourceDao.FIRST_VERSION_STRING, createdResource.getIdElement().getVersionIdPart());
		assertNotNull(createdResource.getMeta());
		assertNotNull(createdResource.getMeta().getVersionId());
		assertEquals(ResourceDao.FIRST_VERSION_STRING, createdResource.getMeta().getVersionId());

		newResource.setIdElement(createdResource.getIdElement().copy());
		newResource.setMeta(createdResource.getMeta().copy());
		assertTrue(newResource.equalsDeep(createdResource));

		D updatedResource = dao.update(updateResource(createdResource), (long) ResourceDao.FIRST_VERSION);
		assertNotNull(updatedResource);
		assertNotNull(updatedResource.getIdElement());
		assertNotNull(updatedResource.getIdElement().getIdPart());
		assertNotNull(updatedResource.getIdElement().getVersionIdPart());
		assertEquals(String.valueOf(ResourceDao.FIRST_VERSION + 1), updatedResource.getIdElement().getVersionIdPart());
		assertNotNull(updatedResource.getMeta());
		assertNotNull(updatedResource.getMeta().getVersionId());
		assertEquals(String.valueOf(ResourceDao.FIRST_VERSION + 1), updatedResource.getMeta().getVersionId());

		checkUpdates(updatedResource);

		List<D> all = dao.readAll();
		assertNotNull(all);
		assertEquals(1, all.size());
		assertNotNull(all.get(0));
		assertNotNull(all.get(0).getIdElement());
		assertNotNull(all.get(0).getIdElement().getIdPart());
		assertNotNull(all.get(0).getIdElement().getVersionIdPart());
		assertEquals(String.valueOf(ResourceDao.FIRST_VERSION + 1), all.get(0).getIdElement().getVersionIdPart());
		assertNotNull(all.get(0).getMeta());
		assertNotNull(all.get(0).getMeta().getVersionId());
		assertEquals(String.valueOf(ResourceDao.FIRST_VERSION + 1), all.get(0).getMeta().getVersionId());
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
	public void testExistsNotDeletedNotExisting() throws Exception
	{
		boolean existsNotDeleted = dao.existsNotDeleted(UUID.randomUUID().toString(), "1");
		assertFalse(existsNotDeleted);
	}

	@Test
	public void testExistsNotDeletedExisting() throws Exception
	{
		D newResource = createResource();
		D createdResource = dao.create(newResource);

		boolean existsNotDeleted1 = dao.existsNotDeleted(createdResource.getIdElement().getIdPart(), null);
		assertTrue(existsNotDeleted1);

		boolean existsNotDeleted2 = dao.existsNotDeleted(createdResource.getIdElement().getIdPart(),
				createdResource.getIdElement().getVersionIdPart());
		assertTrue(existsNotDeleted2);
	}

	@Test
	public void testExistsNotDeletedDeleted() throws Exception
	{
		D newResource = createResource();
		D createdResource = dao.create(newResource);
		dao.delete(UUID.fromString(createdResource.getIdElement().getIdPart()));

		boolean existsNotDeleted1 = dao.existsNotDeleted(createdResource.getIdElement().getIdPart(), null);
		assertFalse(existsNotDeleted1);

		boolean existsNotDeleted2 = dao.existsNotDeleted(createdResource.getIdElement().getIdPart(),
				createdResource.getIdElement().getVersionIdPart());
		assertFalse(existsNotDeleted2);
	}
}
