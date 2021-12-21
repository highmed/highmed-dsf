package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.charset.StandardCharsets;

import org.highmed.dsf.fhir.dao.LibraryDao;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Library;
import org.junit.Test;

public class LibraryIntegrationTest extends AbstractIntegrationTest
{
	private Library createValidLibrary()
	{
		Library library = new Library();
		library.setUrl("https://foo.bar/fhir/Library/30561ba6-106f-4d52-bb8d-e49e20a40d40");
		library.setStatus(Enumerations.PublicationStatus.ACTIVE);
		library.getType().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/library-type")
				.setCode("logic-library");
		library.getContentFirstRep().setContentType("text/cql").setData("Zm9vCg==".getBytes(StandardCharsets.UTF_8));
		return library;
	}

	@Test
	public void testCreateValidByLocalUserReadAccessTagAll() throws Exception
	{
		Library library = createValidLibrary();
		readAccessHelper.addAll(library);

		Library created = getWebserviceClient().create(library);
		assertNotNull(created);
		assertNotNull(created.getIdElement().getIdPart());
		assertNotNull(created.getIdElement().getVersionIdPart());
	}

	@Test
	public void testCreateValidByLocalUserReadAccessTagLocal() throws Exception
	{
		Library library = createValidLibrary();
		readAccessHelper.addLocal(library);

		Library created = getWebserviceClient().create(library);
		assertNotNull(created);
		assertNotNull(created.getIdElement().getIdPart());
		assertNotNull(created.getIdElement().getVersionIdPart());
	}

	@Test
	public void testCreateValidByLocalUserReadAccessTagOrganization() throws Exception
	{
		Library library = createValidLibrary();
		readAccessHelper.addLocal(library);
		readAccessHelper.addOrganization(library, "External_Test_Organization");

		Library created = getWebserviceClient().create(library);
		assertNotNull(created);
		assertNotNull(created.getIdElement().getIdPart());
		assertNotNull(created.getIdElement().getVersionIdPart());
	}

	@Test
	public void testReadWithOrganizationTag() throws Exception
	{
		Library library = createValidLibrary();
		readAccessHelper.addLocal(library);
		readAccessHelper.addOrganization(library, "External_Test_Organization");

		LibraryDao dao = getSpringWebApplicationContext().getBean(LibraryDao.class);
		Library created = dao.create(library);
		assertNotNull(created);
		assertNotNull(created.getIdElement().getIdPart());
		assertNotNull(created.getIdElement().getVersionIdPart());

		Library read = getExternalWebserviceClient().read(Library.class, created.getIdElement().getIdPart());
		assertNotNull(read);
		assertEquals(created.getIdElement().getIdPart(), read.getIdElement().getIdPart());
		assertEquals(created.getIdElement().getVersionIdPart(), read.getIdElement().getVersionIdPart());
	}

	@Test
	public void testCreateValidByLocalUserReadAccessTagRole() throws Exception
	{
		Library library = createValidLibrary();
		readAccessHelper.addLocal(library);
		readAccessHelper.addRole(library, "Parent_Organization", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		Library created = getWebserviceClient().create(library);
		assertNotNull(created);
		assertNotNull(created.getIdElement().getIdPart());
		assertNotNull(created.getIdElement().getVersionIdPart());
	}

	@Test
	public void testReadWithRoleTag() throws Exception
	{
		Library library = createValidLibrary();
		readAccessHelper.addLocal(library);
		readAccessHelper.addRole(library, "Parent_Organization", "http://highmed.org/fhir/CodeSystem/organization-role",
				"TTP");

		LibraryDao dao = getSpringWebApplicationContext().getBean(LibraryDao.class);
		Library created = dao.create(library);
		assertNotNull(created);
		assertNotNull(created.getIdElement().getIdPart());
		assertNotNull(created.getIdElement().getVersionIdPart());

		Library read = getExternalWebserviceClient().read(Library.class, created.getIdElement().getIdPart());
		assertNotNull(read);
		assertEquals(created.getIdElement().getIdPart(), read.getIdElement().getIdPart());
		assertEquals(created.getIdElement().getVersionIdPart(), read.getIdElement().getVersionIdPart());
	}
}
