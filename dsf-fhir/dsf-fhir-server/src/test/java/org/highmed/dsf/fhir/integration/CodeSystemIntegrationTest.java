package org.highmed.dsf.fhir.integration;

import org.highmed.dsf.fhir.dao.CodeSystemDao;
import org.hl7.fhir.r4.model.CodeSystem;
import org.junit.Test;

public class CodeSystemIntegrationTest extends AbstractIntegrationTest
{
	@Test
	public void testReadAllowedForLocalUserByLocalTag() throws Exception
	{
		CodeSystem cs = new CodeSystem();
		readAccessHelper.addLocal(cs);

		CodeSystemDao codeSystemDao = getSpringWebApplicationContext().getBean(CodeSystemDao.class);
		CodeSystem createdCs = codeSystemDao.create(cs);

		getWebserviceClient().read(CodeSystem.class, createdCs.getIdElement().getIdPart());
	}

	@Test
	public void testReadNotAllowedForRemoteUserByLocalTag() throws Exception
	{
		CodeSystem cs = new CodeSystem();
		readAccessHelper.addLocal(cs);

		CodeSystemDao codeSystemDao = getSpringWebApplicationContext().getBean(CodeSystemDao.class);
		CodeSystem createdCs = codeSystemDao.create(cs);

		expectForbidden(
				() -> getExternalWebserviceClient().read(CodeSystem.class, createdCs.getIdElement().getIdPart()));
	}

	@Test
	public void testReadAllowedForLocalUserByAllTag() throws Exception
	{
		CodeSystem cs = new CodeSystem();
		readAccessHelper.addAll(cs);

		CodeSystemDao codeSystemDao = getSpringWebApplicationContext().getBean(CodeSystemDao.class);
		CodeSystem createdCs = codeSystemDao.create(cs);

		getWebserviceClient().read(CodeSystem.class, createdCs.getIdElement().getIdPart());
	}

	@Test
	public void testReadAllowedForRemoteUserByAllTag() throws Exception
	{
		CodeSystem cs = new CodeSystem();
		readAccessHelper.addAll(cs);

		CodeSystemDao codeSystemDao = getSpringWebApplicationContext().getBean(CodeSystemDao.class);
		CodeSystem createdCs = codeSystemDao.create(cs);

		getExternalWebserviceClient().read(CodeSystem.class, createdCs.getIdElement().getIdPart());
	}

	@Test
	public void testReadAllowedForLocalUserByOrganizationTag() throws Exception
	{
		CodeSystem cs = new CodeSystem();
		readAccessHelper.addOrganization(cs, "Test_Organization");

		CodeSystemDao codeSystemDao = getSpringWebApplicationContext().getBean(CodeSystemDao.class);
		CodeSystem createdCs = codeSystemDao.create(cs);

		getWebserviceClient().read(CodeSystem.class, createdCs.getIdElement().getIdPart());
	}

	@Test
	public void testReadAllowedForRemoteUserByOrganizationTag() throws Exception
	{
		CodeSystem cs = new CodeSystem();
		readAccessHelper.addOrganization(cs, "External_Test_Organization");

		CodeSystemDao codeSystemDao = getSpringWebApplicationContext().getBean(CodeSystemDao.class);
		CodeSystem createdCs = codeSystemDao.create(cs);

		getExternalWebserviceClient().read(CodeSystem.class, createdCs.getIdElement().getIdPart());
	}

	@Test
	public void testReadNotAllowedForRemoteUserByOrganizationTag() throws Exception
	{
		CodeSystem cs = new CodeSystem();
		readAccessHelper.addOrganization(cs, "Test_Organization");

		CodeSystemDao codeSystemDao = getSpringWebApplicationContext().getBean(CodeSystemDao.class);
		CodeSystem createdCs = codeSystemDao.create(cs);

		expectForbidden(
				() -> getExternalWebserviceClient().read(CodeSystem.class, createdCs.getIdElement().getIdPart()));
	}

	@Test
	public void testReadAllowedForLocalUserWithRoleTag() throws Exception
	{
		CodeSystem cs = new CodeSystem();
		readAccessHelper.addRole(cs, "Parent_Organization", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		CodeSystemDao codeSystemDao = getSpringWebApplicationContext().getBean(CodeSystemDao.class);
		CodeSystem createdCs = codeSystemDao.create(cs);

		getWebserviceClient().read(CodeSystem.class, createdCs.getIdElement().getIdPart());
	}

	@Test
	public void testReadAllowedForRemoteUserWithRoleTag() throws Exception
	{
		CodeSystem cs = new CodeSystem();
		readAccessHelper.addRole(cs, "Parent_Organization", "http://highmed.org/fhir/CodeSystem/organization-role",
				"TTP");

		CodeSystemDao codeSystemDao = getSpringWebApplicationContext().getBean(CodeSystemDao.class);
		CodeSystem createdCs = codeSystemDao.create(cs);

		getExternalWebserviceClient().read(CodeSystem.class, createdCs.getIdElement().getIdPart());
	}

	@Test
	public void testReadAllowedForRemoteUserWithRoleTag2() throws Exception
	{
		CodeSystem cs = new CodeSystem();
		readAccessHelper.addRole(cs, "Parent_Organization", "http://highmed.org/fhir/CodeSystem/organization-role",
				"DTS");

		CodeSystemDao codeSystemDao = getSpringWebApplicationContext().getBean(CodeSystemDao.class);
		CodeSystem createdCs = codeSystemDao.create(cs);

		getExternalWebserviceClient().read(CodeSystem.class, createdCs.getIdElement().getIdPart());
	}

	@Test
	public void testReadAllowedForRemoteUserWithRoleTag3() throws Exception
	{
		CodeSystem cs = new CodeSystem();
		readAccessHelper.addRole(cs, "Parent_Organization", "http://highmed.org/fhir/CodeSystem/organization-role",
				"TTP");
		readAccessHelper.addRole(cs, "Parent_Organization", "http://highmed.org/fhir/CodeSystem/organization-role",
				"DTS");

		CodeSystemDao codeSystemDao = getSpringWebApplicationContext().getBean(CodeSystemDao.class);
		CodeSystem createdCs = codeSystemDao.create(cs);

		getExternalWebserviceClient().read(CodeSystem.class, createdCs.getIdElement().getIdPart());
	}

	@Test
	public void testReadNotAllowedForRemoteUserWithRoleTag() throws Exception
	{
		CodeSystem cs = new CodeSystem();
		readAccessHelper.addRole(cs, "Parent_Organization", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		CodeSystemDao codeSystemDao = getSpringWebApplicationContext().getBean(CodeSystemDao.class);
		CodeSystem createdCs = codeSystemDao.create(cs);

		expectForbidden(
				() -> getExternalWebserviceClient().read(CodeSystem.class, createdCs.getIdElement().getIdPart()));
	}
}
