package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelperImpl;
import org.highmed.dsf.fhir.dao.jdbc.BinaryDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationAffiliationDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationDaoJdbc;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.junit.Test;

public class OrganizationAffiliationDaoTest
		extends AbstractResourceDaoTest<OrganizationAffiliation, OrganizationAffiliationDao>
		implements ReadAccessDaoTest<OrganizationAffiliation>
{
	private static final String identifierSystem = "http://highmed.org/sid/organization-identifier";
	private static final String identifierValue = "identifier.test";
	private static final boolean active = true;

	private final OrganizationDao organizationDao = new OrganizationDaoJdbc(defaultDataSource,
			permanentDeleteDataSource, fhirContext);

	public OrganizationAffiliationDaoTest()
	{
		super(OrganizationAffiliation.class, OrganizationAffiliationDaoJdbc::new);
	}

	@Override
	public OrganizationAffiliation createResource()
	{
		OrganizationAffiliation organizationAffiliation = new OrganizationAffiliation();
		organizationAffiliation.addIdentifier().setSystem(identifierSystem).setValue(identifierValue);
		return organizationAffiliation;
	}

	@Override
	protected void checkCreated(OrganizationAffiliation resource)
	{
		assertTrue(resource.hasIdentifier());
		assertEquals(identifierSystem, resource.getIdentifierFirstRep().getSystem());
		assertEquals(identifierValue, resource.getIdentifierFirstRep().getValue());
	}

	@Override
	protected OrganizationAffiliation updateResource(OrganizationAffiliation resource)
	{
		resource.setActive(active);
		return resource;
	}

	@Override
	protected void checkUpdates(OrganizationAffiliation resource)
	{
		assertEquals(active, resource.getActive());
	}

	@Test
	public void testReadActiveNotDeletedByMemberOrganizationIdentifier() throws Exception
	{
		final String parentIdentifier = "parent.org";

		try (Connection connection = getDao().newReadWriteTransaction())
		{
			Organization memberOrg = createAndStoreOrganizationInDb(identifierValue, connection);
			Organization parentOrg = createAndStoreOrganizationInDb(parentIdentifier, connection);

			OrganizationAffiliation affiliation = createAndStoreOrganizationAffiliationInDb(parentOrg, memberOrg,
					connection);

			List<OrganizationAffiliation> affiliations = getDao()
					.readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
							connection, identifierValue);
			assertNotNull(affiliations);
			assertEquals(1, affiliations.size());
			assertEquals(affiliation.getIdElement().getIdPart(), affiliations.get(0).getIdElement().getIdPart());
			assertTrue(affiliations.get(0).hasParticipatingOrganization());
			assertTrue(affiliations.get(0).getParticipatingOrganization().hasReference());
			assertEquals("Organization/" + memberOrg.getIdElement().getIdPart(),
					affiliations.get(0).getParticipatingOrganization().getReference());
			assertTrue(affiliations.get(0).getParticipatingOrganization().hasIdentifier());
			assertTrue(affiliations.get(0).getParticipatingOrganization().getIdentifier().hasSystem());
			assertEquals(identifierSystem,
					affiliations.get(0).getParticipatingOrganization().getIdentifier().getSystem());
			assertTrue(affiliations.get(0).getParticipatingOrganization().getIdentifier().hasValue());
			assertEquals(identifierValue,
					affiliations.get(0).getParticipatingOrganization().getIdentifier().getValue());
			assertTrue(affiliations.get(0).hasOrganization());
			assertTrue(affiliations.get(0).getOrganization().hasReference());
			assertEquals("Organization/" + parentOrg.getIdElement().getIdPart(),
					affiliations.get(0).getOrganization().getReference());
			assertTrue(affiliations.get(0).getOrganization().hasIdentifier());
			assertTrue(affiliations.get(0).getOrganization().getIdentifier().hasSystem());
			assertEquals(identifierSystem, affiliations.get(0).getOrganization().getIdentifier().getSystem());
			assertTrue(affiliations.get(0).getOrganization().getIdentifier().hasValue());
			assertEquals(parentIdentifier, affiliations.get(0).getOrganization().getIdentifier().getValue());
		}
	}

	@Test
	public void testSizeOfReadActiveNotDeletedByMemberOrganizationIdentifier() throws Exception
	{
		final String parentFooIdentifier = "parentFoo.org";
		final String parentBarIdentifier = "parentBar.org";

		try (Connection connection = getDao().newReadWriteTransaction())
		{
			Organization memberOrg = createAndStoreOrganizationInDb(identifierValue, connection);
			Organization parentFooOrg = createAndStoreOrganizationInDb(parentFooIdentifier, connection);
			Organization parentBarOrg = createAndStoreOrganizationInDb(parentBarIdentifier, connection);

			createAndStoreOrganizationAffiliationInDb(parentFooOrg, memberOrg, connection);
			createAndStoreOrganizationAffiliationInDb(parentBarOrg, memberOrg, connection);

			List<OrganizationAffiliation> affiliations = getDao()
					.readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
							connection, identifierValue);
			assertNotNull(affiliations);

			assertEquals(2, affiliations.size());
			assertEquals(identifierValue,
					affiliations.get(0).getParticipatingOrganization().getIdentifier().getValue());
			assertEquals(identifierValue,
					affiliations.get(1).getParticipatingOrganization().getIdentifier().getValue());
			assertNotEquals(affiliations.get(0).getOrganization().getIdentifier().getValue(),
					affiliations.get(1).getOrganization().getIdentifier().getValue());
			assertTrue(List.of(parentFooIdentifier, parentBarIdentifier)
					.contains(affiliations.get(0).getOrganization().getIdentifier().getValue()));
			assertTrue(List.of(parentFooIdentifier, parentBarIdentifier)
					.contains(affiliations.get(1).getOrganization().getIdentifier().getValue()));
		}
	}

	private Organization createAndStoreOrganizationInDb(String identifierValue, Connection connection)
			throws SQLException
	{
		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(identifierSystem).setValue(identifierValue);

		return organizationDao.createWithTransactionAndId(connection, memberOrg, UUID.randomUUID());
	}

	private OrganizationAffiliation createAndStoreOrganizationAffiliationInDb(Organization parent, Organization member,
			Connection connection) throws SQLException
	{
		OrganizationAffiliation organizationAffiliation = new OrganizationAffiliation();
		organizationAffiliation.setActive(true);
		organizationAffiliation.getParticipatingOrganization()
				.setReference("Organization/" + member.getIdElement().getIdPart());
		organizationAffiliation.getOrganization().setReference("Organization/" + parent.getIdElement().getIdPart());

		return getDao().createWithTransactionAndId(connection, organizationAffiliation, UUID.randomUUID());
	}

	@Override
	@Test
	public void testReadAccessTriggerAll() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerAll();
	}

	@Override
	@Test
	public void testReadAccessTriggerLocal() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerLocal();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganization() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganization();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganizationResourceFirst() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganizationResourceFirst();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganization2Organizations1Matching() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganization2Organizations1Matching();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganization2Organizations2Matching() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganization2Organizations2Matching();
	}

	@Override
	@Test
	public void testReadAccessTriggerRole() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRole();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleResourceFirst() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleResourceFirst();
	}

	@Override
	@Test
	public void testReadAccessTriggerRole2Organizations1Matching() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRole2Organizations1Matching();
	}

	@Override
	@Test
	public void testReadAccessTriggerRole2Organizations2Matching() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRole2Organizations2Matching();
	}

	@Override
	@Test
	public void testReadAccessTriggerAllUpdate() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerAllUpdate();
	}

	@Override
	@Test
	public void testReadAccessTriggerLocalUpdate() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerLocalUpdate();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganizationUpdate() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganizationUpdate();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleUpdate() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleUpdate();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleUpdateMemberOrganizationNonActive() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleUpdateMemberOrganizationNonActive();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleUpdateParentOrganizationNonActive() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleUpdateParentOrganizationNonActive();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleUpdateMemberAndParentOrganizationNonActive() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleUpdateMemberAndParentOrganizationNonActive();
	}

	@Override
	@Test
	public void testReadAccessTriggerAllDelete() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerAllDelete();
	}

	@Override
	@Test
	public void testReadAccessTriggerLocalDelete() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerLocalDelete();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganizationDelete() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganizationDelete();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleDelete() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleDelete();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleDeleteMember() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleDeleteMember();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleDeleteParent() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleDeleteParent();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleDeleteMemberAndParent() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleDeleteMemberAndParent();
	}

	@Override
	@Test
	public void testSearchWithUserFilterAfterReadAccessTriggerAllWithLocalUser() throws Exception
	{
		ReadAccessDaoTest.super.testSearchWithUserFilterAfterReadAccessTriggerAllWithLocalUser();
	}

	@Override
	@Test
	public void testSearchWithUserFilterAfterReadAccessTriggerLocalwithLocalUser() throws Exception
	{
		ReadAccessDaoTest.super.testSearchWithUserFilterAfterReadAccessTriggerLocalwithLocalUser();
	}

	@Override
	@Test
	public void testSearchWithUserFilterAfterReadAccessTriggerAllWithRemoteUser() throws Exception
	{
		ReadAccessDaoTest.super.testSearchWithUserFilterAfterReadAccessTriggerAllWithRemoteUser();
	}

	@Override
	@Test
	public void testSearchWithUserFilterAfterReadAccessTriggerLocalWithRemoteUser() throws Exception
	{
		ReadAccessDaoTest.super.testSearchWithUserFilterAfterReadAccessTriggerLocalWithRemoteUser();
	}

	@Test
	public void testUpdateWithExistingBinary() throws Exception
	{
		BinaryDaoJdbc binaryDao = new BinaryDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
		OrganizationDaoJdbc orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.getIdentifierFirstRep().setSystem(ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue("member.com");

		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.getIdentifierFirstRep().setSystem(ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue("parent.com");

		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation affiliation = new OrganizationAffiliation();
		affiliation.setActive(true);
		affiliation.getParticipatingOrganization()
				.setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());
		affiliation.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		affiliation.addCode().getCodingFirstRep().setSystem("role-system").setCode("role-code");

		OrganizationAffiliation createdAffiliation = dao.create(affiliation);

		Binary binary = new Binary();
		binary.setContentType("text/plain");
		binary.setData("1234567890".getBytes());
		new ReadAccessHelperImpl().addRole(binary, "parent.com", "role-system", "role-code");

		Binary createdBinary = binaryDao.create(binary);
		assertNotNull(createdBinary);

		dao.update(createdAffiliation);
	}

	@Test
	public void testUpdateWithExistingBinaryUpdateMemberOrg() throws Exception
	{
		BinaryDaoJdbc binaryDao = new BinaryDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
		OrganizationDaoJdbc orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.getIdentifierFirstRep().setSystem(ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue("member.com");

		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.getIdentifierFirstRep().setSystem(ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue("parent.com");

		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation affiliation = new OrganizationAffiliation();
		affiliation.setActive(true);
		affiliation.getParticipatingOrganization()
				.setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());
		affiliation.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		affiliation.addCode().getCodingFirstRep().setSystem("role-system").setCode("role-code");

		dao.create(affiliation);

		Binary binary = new Binary();
		binary.setContentType("text/plain");
		binary.setData("1234567890".getBytes());
		new ReadAccessHelperImpl().addRole(binary, "parent.com", "role-system", "role-code");

		Binary createdBinary = binaryDao.create(binary);
		assertNotNull(createdBinary);

		orgDao.update(createdMemberOrg);
	}

	@Test
	public void testUpdateWithExistingBinaryUpdateParentOrg() throws Exception
	{
		BinaryDaoJdbc binaryDao = new BinaryDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
		OrganizationDaoJdbc orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.getIdentifierFirstRep().setSystem(ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue("member.com");

		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.getIdentifierFirstRep().setSystem(ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue("parent.com");

		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation affiliation = new OrganizationAffiliation();
		affiliation.setActive(true);
		affiliation.getParticipatingOrganization()
				.setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());
		affiliation.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		affiliation.addCode().getCodingFirstRep().setSystem("role-system").setCode("role-code");

		dao.create(affiliation);

		Binary binary = new Binary();
		binary.setContentType("text/plain");
		binary.setData("1234567890".getBytes());
		new ReadAccessHelperImpl().addRole(binary, "parent.com", "role-system", "role-code");

		Binary createdBinary = binaryDao.create(binary);
		assertNotNull(createdBinary);

		orgDao.update(createdParentOrg);
	}
}
