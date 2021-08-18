package org.highmed.dsf.fhir.integration;

import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.ACTIVE;
import static org.junit.Assert.*;

import java.util.Date;

import org.highmed.dsf.fhir.dao.NamingSystemDao;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.NamingSystem.NamingSystemIdentifierType;
import org.hl7.fhir.r4.model.NamingSystem.NamingSystemType;
import org.junit.Test;

public class NamingSystemIntegrationTest extends AbstractIntegrationTest
{
	private static final String uniqueId = "http://foo.bar/sid/baz";
	private static final String name = "NamingSystemTestName";

	private NamingSystem createResource(String name, String uniqueId)
	{
		var s = new NamingSystem();
		s.setName(name);
		s.setKind(NamingSystemType.IDENTIFIER);
		s.setDate(new Date());
		s.setStatus(ACTIVE);
		s.addUniqueId().setValue(uniqueId).setType(NamingSystemIdentifierType.URI).addModifierExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-check-logical-reference")
				.setValue(new BooleanType(true));
		getReadAccessHelper().addAll(s);

		return s;
	}

	@Test
	public void testCreateNamingSystemNameNotExists() throws Exception
	{
		getWebserviceClient().create(createResource(name, uniqueId));
	}

	@Test
	public void testCreateNamingSystemNameExists() throws Exception
	{
		NamingSystemDao dao = getSpringWebApplicationContext().getBean(NamingSystemDao.class);
		NamingSystem created = dao.create(createResource(name, uniqueId));
		assertNotNull(created);

		expectForbidden(() -> getWebserviceClient().create(createResource(name, uniqueId + "foo")));
	}

	@Test
	public void testCreateNamingSystemUniqueIdExists() throws Exception
	{
		NamingSystemDao dao = getSpringWebApplicationContext().getBean(NamingSystemDao.class);
		NamingSystem created = dao.create(createResource(name, uniqueId));
		assertNotNull(created);

		expectForbidden(() -> getWebserviceClient().create(createResource(name + "foo", uniqueId)));
	}

	@Test
	public void testCreateNamingSystemUniqueIdExistsMultipleUniqueIds1() throws Exception
	{
		NamingSystemDao dao = getSpringWebApplicationContext().getBean(NamingSystemDao.class);
		NamingSystem created = dao.create(createResource(name, uniqueId));
		assertNotNull(created);

		NamingSystem toCreate = createResource(name + "foo", uniqueId);
		toCreate.addUniqueId().setValue(uniqueId + "bar").setType(NamingSystemIdentifierType.URI);

		expectForbidden(() -> getWebserviceClient().create(toCreate));
	}

	@Test
	public void testCreateNamingSystemUniqueIdExistsMultipleUniqueIds2() throws Exception
	{
		NamingSystemDao dao = getSpringWebApplicationContext().getBean(NamingSystemDao.class);
		NamingSystem created = dao.create(createResource(name, uniqueId));
		assertNotNull(created);

		NamingSystem toCreate = createResource(name + "foo", uniqueId + "bar");
		toCreate.addUniqueId().setValue(uniqueId).setType(NamingSystemIdentifierType.URI).addModifierExtension();

		expectForbidden(() -> getWebserviceClient().create(toCreate));
	}

	@Test
	public void testCreateNamingSystemWithNonUniqueIds() throws Exception
	{

		NamingSystem toCreate = createResource(name, uniqueId);
		toCreate.addUniqueId().setValue(uniqueId).setType(NamingSystemIdentifierType.URI).addModifierExtension();

		expectForbidden(() -> getWebserviceClient().create(toCreate));
	}
}
