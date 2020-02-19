package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationDaoJdbc;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;

public class OrganizationDaoTest extends AbstractResourceDaoTest<Organization, OrganizationDao>
{
	private static final String name = "Demo Organization";
	private static final boolean active = true;

	public OrganizationDaoTest()
	{
		super(Organization.class);
	}

	@Override
	protected OrganizationDao createDao(BasicDataSource dataSource, FhirContext fhirContext,
			OrganizationType organizationType)
	{
		return new OrganizationDaoJdbc(dataSource, fhirContext, organizationType);
	}

	@Override
	protected Organization createResource()
	{
		Organization organization = new Organization();
		organization.setName(name);
		return organization;
	}

	@Override
	protected void checkCreated(Organization resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected Organization updateResource(Organization resource)
	{
		resource.setActive(active);
		return resource;
	}

	@Override
	protected void checkUpdates(Organization resource)
	{
		assertEquals(active, resource.getActive());
	}

	@Test
	public void testReadActiveNotDeletedByThumbprint() throws Exception
	{
		final String certHex = Hex.encodeHexString("FooBarBaz".getBytes(StandardCharsets.UTF_8));

		Organization org = new Organization();
		org.setActive(true);
		org.setName("Test");
		org.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint")
				.setValue(new StringType(certHex));

		Organization created = ttpDao.create(org);
		assertNotNull(created);

		Optional<Organization> read = ttpDao.readActiveNotDeletedByThumbprint(certHex);
		assertNotNull(read);
		assertTrue(read.isPresent());
		assertNotNull(
				read.get().getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint"));
		assertEquals(StringType.class,
				read.get().getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint")
						.getValue().getClass());
		assertEquals(certHex,
				((StringType) read.get()
						.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint")
						.getValue()).asStringValue());
	}

	@Test
	public void testReadActiveNotDeletedByThumbprintNotActive() throws Exception
	{
		final String certHex = Hex.encodeHexString("FooBarBaz".getBytes(StandardCharsets.UTF_8));

		Organization org = new Organization();
		org.setActive(false);
		org.setName("Test");
		org.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint")
				.setValue(new StringType(certHex));

		Organization created = ttpDao.create(org);
		assertNotNull(created);

		Optional<Organization> read = ttpDao.readActiveNotDeletedByThumbprint(certHex);
		assertNotNull(read);
		assertTrue(read.isEmpty());

		Optional<Organization> read2 = ttpDao.read(UUID.fromString(created.getIdElement().getIdPart()));
		assertNotNull(read2);
		assertTrue(read2.isPresent());
	}

	@Test
	public void testReadActiveNotDeletedByThumbprintDeleted() throws Exception
	{
		final String certHex = Hex.encodeHexString("FooBarBaz".getBytes(StandardCharsets.UTF_8));

		Organization org = new Organization();
		org.setActive(false);
		org.setName("Test");
		org.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint")
				.setValue(new StringType(certHex));

		Organization created = ttpDao.create(org);
		assertNotNull(created);
		ttpDao.delete(UUID.fromString(created.getIdElement().getIdPart()));

		Optional<Organization> read = ttpDao.readActiveNotDeletedByThumbprint(certHex);
		assertNotNull(read);
		assertTrue(read.isEmpty());
	}

	@Test
	public void testReadActiveNotDeletedByThumbprintNotExisting() throws Exception
	{
		final String certHex = Hex.encodeHexString("FooBarBaz".getBytes(StandardCharsets.UTF_8));

		Optional<Organization> read = ttpDao.readActiveNotDeletedByThumbprint(certHex);
		assertNotNull(read);
		assertTrue(read.isEmpty());
	}

	@Test
	public void testReadActiveNotDeletedByThumbprintNull() throws Exception
	{
		Optional<Organization> read = ttpDao.readActiveNotDeletedByThumbprint(null);
		assertNotNull(read);
		assertTrue(read.isEmpty());
	}

	@Test
	public void testReadActiveNotDeletedByThumbprintBlank() throws Exception
	{
		Optional<Organization> read = ttpDao.readActiveNotDeletedByThumbprint("  ");
		assertNotNull(read);
		assertTrue(read.isEmpty());
	}
}
