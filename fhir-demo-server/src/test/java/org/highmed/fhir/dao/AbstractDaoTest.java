package org.highmed.fhir.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.test.FhirEmbeddedPostgresWithLiquibase;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import de.rwh.utils.test.Database;

public abstract class AbstractDaoTest<D extends DomainResource, C extends BasicCrudDao<D>>
{
	@ClassRule
	public static final FhirEmbeddedPostgresWithLiquibase template = new FhirEmbeddedPostgresWithLiquibase();

	@Rule
	public final Database database = new Database(template);

	protected FhirContext fhirContext;
	protected C dao;

	@Before
	public void before() throws Exception
	{
		fhirContext = FhirContext.forR4();
		dao = createDao(database.getDataSource(), fhirContext);
	}

	protected abstract C createDao(BasicDataSource dataSource, FhirContext fhirContext);

	protected abstract IdType createIdWithoutVersion();

	@Test
	public void testEmpty() throws Exception
	{
		Optional<D> read = dao.read(createIdWithoutVersion());
		assertTrue(read.isEmpty());
	}

	protected abstract IdType createIdWithVersion();

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

		newResource.setIdElement(createdResource.getIdElement().copy());
		newResource.setMeta(createdResource.getMeta().copy());

		assertTrue(newResource.equalsDeep(createdResource));
		
		dao.read(createdResource.getIdElement());
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
	
	protected abstract void checkUpdates(D resource);
}
