package org.highmed.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.test.FhirEmbeddedPostgresWithLiquibase;
import org.highmed.fhir.test.TestSuiteDbTests;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.rwh.utils.test.Database;

public abstract class AbstractDomainResourceDaoTest<D extends DomainResource, C extends AbstractDomainResourceDao<D>>
{
	@ClassRule
	public static final FhirEmbeddedPostgresWithLiquibase template = new FhirEmbeddedPostgresWithLiquibase(
			TestSuiteDbTests.template);

	@Rule
	public final Database database = new Database(template);

	protected final Class<D> resouceClass;

	protected FhirContext fhirContext;
	protected C dao;

	protected AbstractDomainResourceDaoTest(Class<D> resouceClass)
	{
		this.resouceClass = resouceClass;
	}

	private IdType createIdWithoutVersion()
	{
		return new IdType(resouceClass.getAnnotation(ResourceDef.class).name(), UUID.randomUUID().toString(), null);
	}

	private IdType createIdWithVersion()
	{
		return new IdType(resouceClass.getAnnotation(ResourceDef.class).name(), UUID.randomUUID().toString(), "1");
	}

	@Before
	public void before() throws Exception
	{
		fhirContext = FhirContext.forR4();
		dao = createDao(database.getDataSource(), fhirContext);
	}

	protected abstract C createDao(BasicDataSource dataSource, FhirContext fhirContext);

	@Test
	public void testEmpty() throws Exception
	{
		Optional<D> read = dao.read(createIdWithoutVersion());
		assertTrue(read.isEmpty());
	}

	@Test
	public void testEmptyWithVersion() throws Exception
	{
		Optional<D> read = dao.readVersion(createIdWithVersion());
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

		Optional<D> read = dao.read(createdResource.getIdElement());
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

		D updatedResource = dao.update(updateResource(createdResource));
		assertNotNull(updatedResource);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testUpdateNonExisting() throws Exception
	{
		D newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		dao.update(newResource);
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

		Optional<D> read = dao.read(createdResource.getIdElement());
		assertTrue(read.isPresent());

		dao.delete(createdResource.getIdElement());

		dao.read(createdResource.getIdElement());
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

		Optional<D> read = dao.readVersion(createdResource.getIdElement());
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

		Optional<D> read = dao.read(createdResource.getIdElement());
		assertTrue(read.isPresent());

		assertTrue(newResource.equalsDeep(read.get()));
	}
}
