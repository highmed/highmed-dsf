package org.highmed.dsf.fhir.dao;

import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM;
import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.READ_ACCESS_TAG_VALUE_ALL;
import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.READ_ACCESS_TAG_VALUE_LOCAL;
import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.READ_ACCESS_TAG_VALUE_ORGANIZATION;
import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.READ_ACCESS_TAG_VALUE_ROLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelperImpl;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationAffiliationDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationDaoJdbc;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Test;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;

import ca.uhn.fhir.context.FhirContext;

public interface ReadAccessDaoTest<D extends Resource>
{
	Logger getLogger();

	DataSource getDefaultDataSource();

	DataSource getPermanentDeleteDataSource();

	D createResource();

	ResourceDao<D> getDao();

	FhirContext getFhirContext();

	default void assertReadAccessEntryCount(int totalExpectedCount, int expectedCount, Resource resource,
			String accessType) throws Exception
	{
		assertReadAccessEntryCount(totalExpectedCount, expectedCount, resource, accessType, null, null);
	}

	default void assertReadAccessEntryCount(int totalExpectedCount, int expectedCount, Resource resource,
			String accessType, Organization organization) throws Exception
	{
		assertReadAccessEntryCount(totalExpectedCount, expectedCount, resource, accessType, organization, null);
	}

	default void assertReadAccessEntryCount(int totalExpectedCount, int expectedCount, Resource resource,
			String accessType, Organization organization, OrganizationAffiliation organizationAffiliation)
			throws Exception
	{
		try (Connection connection = getDefaultDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM read_access"))
		{
			try (ResultSet result = statement.executeQuery())
			{
				assertTrue(result.next());
				assertEquals(totalExpectedCount, result.getInt(1));
			}
		}

		StringBuilder query = new StringBuilder(
				"SELECT count(*) FROM read_access WHERE resource_id = ? AND resource_version = ? AND access_type = ?");

		if (organization != null)
			query.append(" AND organization_id = ?");
		if (organizationAffiliation != null)
			query.append(" AND organization_affiliation_id = ?");

		try (Connection connection = getDefaultDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement(query.toString()))
		{
			statement.setObject(1, toUuidObject(resource.getIdElement().getIdPart()));
			statement.setLong(2, resource.getIdElement().getVersionIdPartAsLong());
			statement.setString(3, accessType);

			if (organization != null)
				statement.setObject(4, toUuidObject(organization.getIdElement().getIdPart()));
			if (organizationAffiliation != null)
				statement.setObject(5, toUuidObject(organizationAffiliation.getIdElement().getIdPart()));

			getLogger().trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				assertTrue(result.next());
				assertEquals(expectedCount, result.getInt(1));
			}
		}
	}

	default PGobject toUuidObject(String uuid) throws Exception
	{
		if (uuid == null)
			return null;

		PGobject uuidObject = new PGobject();
		uuidObject.setType("UUID");
		uuidObject.setValue(uuid);
		return uuidObject;
	}

	default void testReadAccessTrigger(String accessType, Consumer<D> readAccessModifier) throws Exception
	{
		D d = createResource();
		readAccessModifier.accept(d);

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(1, 1, createdD, accessType);
	}

	@Test
	default void testReadAccessTriggerAll() throws Exception
	{
		testReadAccessTrigger(READ_ACCESS_TAG_VALUE_ALL, new ReadAccessHelperImpl()::addAll);
	}

	@Test
	default void testReadAccessTriggerLocal() throws Exception
	{
		testReadAccessTrigger(READ_ACCESS_TAG_VALUE_LOCAL, new ReadAccessHelperImpl()::addLocal);
	}

	@Test
	default void testReadAccessTriggerOrganization() throws Exception
	{
		Organization org = new Organization();
		org.setActive(true);
		org.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org.com");
		Organization createdOrg = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext()).create(org);

		D d = createResource();
		new ReadAccessHelperImpl().addOrganization(d, createdOrg);

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
	}

	@Test
	default void testReadAccessTriggerOrganizationResourceFirst() throws Exception
	{
		final String orgIdentifier = "org.com";

		D d = createResource();
		new ReadAccessHelperImpl().addOrganization(d, orgIdentifier);

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(1, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, createdD, READ_ACCESS_TAG_VALUE_ORGANIZATION);

		Organization org = new Organization();
		org.setActive(true);
		org.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue(orgIdentifier);
		Organization createdOrg = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext()).create(org);

		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
	}

	@Test
	default void testReadAccessTriggerOrganization2Organizations1Matching() throws Exception
	{
		OrganizationDaoJdbc organizationDao = new OrganizationDaoJdbc(getDefaultDataSource(),
				getPermanentDeleteDataSource(), getFhirContext());

		Organization org1 = new Organization();
		org1.setActive(true);
		org1.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org1.com");
		Organization createdOrg1 = organizationDao.create(org1);

		Organization org2 = new Organization();
		org2.setActive(true);
		org2.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org2.com");
		Organization createdOrg2 = organizationDao.create(org2);

		D d = createResource();
		new ReadAccessHelperImpl().addOrganization(d, createdOrg1);

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg1);
		assertReadAccessEntryCount(2, 0, createdD, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg2);
	}

	@Test
	default void testReadAccessTriggerOrganization2Organizations2Matching() throws Exception
	{
		OrganizationDaoJdbc organizationDao = new OrganizationDaoJdbc(getDefaultDataSource(),
				getPermanentDeleteDataSource(), getFhirContext());
		ReadAccessHelperImpl readAccessHelper = new ReadAccessHelperImpl();

		Organization org1 = new Organization();
		org1.setActive(true);
		org1.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org1.com");
		Organization createdOrg1 = organizationDao.create(org1);

		Organization org2 = new Organization();
		org2.setActive(true);
		org2.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org2.com");
		Organization createdOrg2 = organizationDao.create(org2);

		D d = createResource();
		readAccessHelper.addOrganization(d, createdOrg1);
		readAccessHelper.addOrganization(d, createdOrg2);

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(3, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(3, 1, createdD, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg1);
		assertReadAccessEntryCount(3, 1, createdD, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg2);
	}

	@Test
	default void testReadAccessTriggerRole() throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext());
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = new OrganizationAffiliationDaoJdbc(getDefaultDataSource(),
				getPermanentDeleteDataSource(), getFhirContext()).create(aff);

		D d = createResource();
		new ReadAccessHelperImpl().addRole(d, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}

	@Test
	default void testReadAccessTriggerRoleResourceFirst() throws Exception
	{
		D d = createResource();
		new ReadAccessHelperImpl().addRole(d, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(1, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, createdD, READ_ACCESS_TAG_VALUE_ROLE);

		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext());
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = new OrganizationAffiliationDaoJdbc(getDefaultDataSource(),
				getPermanentDeleteDataSource(), getFhirContext()).create(aff);

		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}

	@Test
	default void testReadAccessTriggerRole2Organizations1Matching() throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg1 = new Organization();
		memberOrg1.setActive(true);
		memberOrg1.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member1.com");

		Organization memberOrg2 = new Organization();
		memberOrg2.setActive(true);
		memberOrg2.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member2.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext());
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg1 = orgDao.create(memberOrg1);
		Organization createdMemberOrg2 = orgDao.create(memberOrg2);

		OrganizationAffiliation aff1 = new OrganizationAffiliation();
		aff1.setActive(true);
		aff1.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff1.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff1.getParticipatingOrganization()
				.setReference("Organization/" + createdMemberOrg1.getIdElement().getIdPart());

		OrganizationAffiliation aff2 = new OrganizationAffiliation();
		aff2.setActive(true);
		aff2.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("TTP");
		aff2.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff2.getParticipatingOrganization()
				.setReference("Organization/" + createdMemberOrg2.getIdElement().getIdPart());

		OrganizationAffiliationDaoJdbc organizationAffiliationDao = new OrganizationAffiliationDaoJdbc(
				getDefaultDataSource(), getPermanentDeleteDataSource(), getFhirContext());
		OrganizationAffiliation createdAff1 = organizationAffiliationDao.create(aff1);
		OrganizationAffiliation createdAff2 = organizationAffiliationDao.create(aff2);

		D d = createResource();
		new ReadAccessHelperImpl().addRole(d, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg1, createdAff1);
		assertReadAccessEntryCount(2, 0, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg2, createdAff2);
	}

	@Test
	default void testReadAccessTriggerRole2Organizations2Matching() throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg1 = new Organization();
		memberOrg1.setActive(true);
		memberOrg1.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member1.com");

		Organization memberOrg2 = new Organization();
		memberOrg2.setActive(true);
		memberOrg2.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member2.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext());
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg1 = orgDao.create(memberOrg1);
		Organization createdMemberOrg2 = orgDao.create(memberOrg2);

		OrganizationAffiliation aff1 = new OrganizationAffiliation();
		aff1.setActive(true);
		aff1.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff1.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff1.getParticipatingOrganization()
				.setReference("Organization/" + createdMemberOrg1.getIdElement().getIdPart());

		OrganizationAffiliation aff2 = new OrganizationAffiliation();
		aff2.setActive(true);
		aff2.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff2.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff2.getParticipatingOrganization()
				.setReference("Organization/" + createdMemberOrg2.getIdElement().getIdPart());

		OrganizationAffiliationDaoJdbc organizationAffiliationDao = new OrganizationAffiliationDaoJdbc(
				getDefaultDataSource(), getPermanentDeleteDataSource(), getFhirContext());
		OrganizationAffiliation createdAff1 = organizationAffiliationDao.create(aff1);
		OrganizationAffiliation createdAff2 = organizationAffiliationDao.create(aff2);

		D d = createResource();
		new ReadAccessHelperImpl().addRole(d, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(3, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(3, 1, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg1, createdAff1);
		assertReadAccessEntryCount(3, 1, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg2, createdAff2);
	}

	private void testReadAccessTriggerUpdate(String accessType, Consumer<D> readAccessModifier) throws Exception
	{
		D d = createResource();
		readAccessModifier.accept(d);

		D v1 = getDao().create(d);
		assertEquals(1L, (long) v1.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(1, 1, v1, accessType);

		v1.getMeta().setTag(Collections.emptyList());
		D v2 = getDao().update(v1);
		assertEquals(2L, (long) v2.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(1, 1, v1, accessType);
		assertReadAccessEntryCount(1, 0, v2, accessType);

		readAccessModifier.accept(v2);
		D v3 = getDao().update(v2);
		assertEquals(3L, (long) v3.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(2, 1, v1, accessType);
		assertReadAccessEntryCount(2, 0, v2, accessType);
		assertReadAccessEntryCount(2, 1, v3, accessType);
	}

	@Test
	default void testReadAccessTriggerAllUpdate() throws Exception
	{
		testReadAccessTriggerUpdate(READ_ACCESS_TAG_VALUE_ALL, new ReadAccessHelperImpl()::addAll);
	}

	@Test
	default void testReadAccessTriggerLocalUpdate() throws Exception
	{
		testReadAccessTriggerUpdate(READ_ACCESS_TAG_VALUE_LOCAL, new ReadAccessHelperImpl()::addLocal);
	}

	@Test
	default void testReadAccessTriggerOrganizationUpdate() throws Exception
	{
		final OrganizationDaoJdbc organizationDao = new OrganizationDaoJdbc(getDefaultDataSource(),
				getPermanentDeleteDataSource(), getFhirContext());

		Organization org = new Organization();
		org.setActive(true);
		org.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org.com");
		Organization createdOrg = organizationDao.create(org);

		D d = createResource();
		new ReadAccessHelperImpl().addOrganization(d, createdOrg);

		D v1 = getDao().create(d);
		assertEquals(1L, (long) v1.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);

		createdOrg.setActive(false);
		Organization updatedOrg = organizationDao.update(createdOrg);

		assertReadAccessEntryCount(1, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, v1, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);

		updatedOrg.setActive(true);
		organizationDao.update(updatedOrg);

		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
	}

	@Test
	default void testReadAccessTriggerRoleUpdate() throws Exception
	{
		final OrganizationAffiliationDaoJdbc organizationAffiliationDao = new OrganizationAffiliationDaoJdbc(
				getDefaultDataSource(), getPermanentDeleteDataSource(), getFhirContext());

		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext());
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = organizationAffiliationDao.create(aff);

		D d = createResource();
		new ReadAccessHelperImpl().addRole(d, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		D v1 = getDao().create(d);
		assertEquals(1L, (long) v1.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		createdAff.setActive(false);
		OrganizationAffiliation updatedAff = organizationAffiliationDao.update(createdAff);

		assertReadAccessEntryCount(1, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, v1, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, updatedAff);

		updatedAff.setActive(true);
		organizationAffiliationDao.update(updatedAff);

		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, updatedAff);
	}

	@Test
	default void testReadAccessTriggerRoleUpdateMemberOrganizationNonActive() throws Exception
	{
		final OrganizationAffiliationDaoJdbc organizationAffiliationDao = new OrganizationAffiliationDaoJdbc(
				getDefaultDataSource(), getPermanentDeleteDataSource(), getFhirContext());

		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext());
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = organizationAffiliationDao.create(aff);

		D d = createResource();
		new ReadAccessHelperImpl().addRole(d, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		D v1 = getDao().create(d);
		assertEquals(1L, (long) v1.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		createdMemberOrg.setActive(false);
		Organization updatedMemberOrg = orgDao.update(createdMemberOrg);

		assertReadAccessEntryCount(1, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, v1, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg, createdAff);

		updatedMemberOrg.setActive(true);
		orgDao.update(updatedMemberOrg);

		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg, createdAff);
	}

	@Test
	default void testReadAccessTriggerRoleUpdateParentOrganizationNonActive() throws Exception
	{
		final OrganizationAffiliationDaoJdbc organizationAffiliationDao = new OrganizationAffiliationDaoJdbc(
				getDefaultDataSource(), getPermanentDeleteDataSource(), getFhirContext());

		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext());
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = organizationAffiliationDao.create(aff);

		D d = createResource();
		new ReadAccessHelperImpl().addRole(d, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		D v1 = getDao().create(d);
		assertEquals(1L, (long) v1.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		createdParentOrg.setActive(false);
		Organization updatedParentOrg = orgDao.update(createdParentOrg);

		assertReadAccessEntryCount(1, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, v1, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		updatedParentOrg.setActive(true);
		orgDao.update(updatedParentOrg);

		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}

	@Test
	default void testReadAccessTriggerRoleUpdateMemberAndParentOrganizationNonActive() throws Exception
	{
		final OrganizationAffiliationDaoJdbc organizationAffiliationDao = new OrganizationAffiliationDaoJdbc(
				getDefaultDataSource(), getPermanentDeleteDataSource(), getFhirContext());

		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext());
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = organizationAffiliationDao.create(aff);

		D d = createResource();
		new ReadAccessHelperImpl().addRole(d, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		D v1 = getDao().create(d);
		assertEquals(1L, (long) v1.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		createdParentOrg.setActive(false);
		createdMemberOrg.setActive(false);
		Organization updatedParentOrg = orgDao.update(createdParentOrg);
		Organization updatedMemberOrg = orgDao.update(createdMemberOrg);

		assertReadAccessEntryCount(1, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, v1, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg, createdAff);

		updatedParentOrg.setActive(true);
		orgDao.update(updatedParentOrg);

		assertReadAccessEntryCount(1, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, v1, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg, createdAff);

		updatedMemberOrg.setActive(true);
		Organization updatedMemberOrg2 = orgDao.update(updatedMemberOrg);

		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg2, createdAff);
	}

	private void testReadAccessTriggerDelete(String accessType, Consumer<D> readAccessModifier) throws Exception
	{
		D d = createResource();
		readAccessModifier.accept(d);

		D v1 = getDao().create(d);
		assertEquals(1L, (long) v1.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(1, 1, v1, accessType);

		assertTrue(getDao().existsNotDeleted(v1.getIdElement().getIdPart(), v1.getIdElement().getVersionIdPart()));
		getDao().delete(UUID.fromString(v1.getIdElement().getIdPart()));
		assertFalse(getDao().existsNotDeleted(v1.getIdElement().getIdPart(), v1.getIdElement().getVersionIdPart()));

		assertReadAccessEntryCount(1, 1, v1, accessType);
	}

	@Test
	default void testReadAccessTriggerAllDelete() throws Exception
	{
		testReadAccessTriggerDelete(READ_ACCESS_TAG_VALUE_ALL, new ReadAccessHelperImpl()::addAll);
	}

	@Test
	default void testReadAccessTriggerLocalDelete() throws Exception
	{
		testReadAccessTriggerDelete(READ_ACCESS_TAG_VALUE_LOCAL, new ReadAccessHelperImpl()::addLocal);
	}

	@Test
	default void testReadAccessTriggerOrganizationDelete() throws Exception
	{
		final OrganizationDaoJdbc organizationDao = new OrganizationDaoJdbc(getDefaultDataSource(),
				getPermanentDeleteDataSource(), getFhirContext());

		Organization org = new Organization();
		org.setActive(true);
		org.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org.com");
		Organization createdOrg = organizationDao.create(org);

		D d = createResource();
		new ReadAccessHelperImpl().addOrganization(d, createdOrg);

		D v1 = getDao().create(d);
		assertEquals(1L, (long) v1.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, v1, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);

		organizationDao.delete(UUID.fromString(createdOrg.getIdElement().getIdPart()));

		assertReadAccessEntryCount(1, 1, v1, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, v1, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
	}

	@Test
	default void testReadAccessTriggerRoleDelete() throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext());
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliationDaoJdbc orgAffDao = new OrganizationAffiliationDaoJdbc(getDefaultDataSource(),
				getPermanentDeleteDataSource(), getFhirContext());
		OrganizationAffiliation createdAff = orgAffDao.create(aff);

		D d = createResource();
		new ReadAccessHelperImpl().addRole(d, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		orgAffDao.delete(UUID.fromString(createdAff.getIdElement().getIdPart()));

		assertReadAccessEntryCount(1, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}

	@Test
	default void testReadAccessTriggerRoleDeleteMember() throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext());
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = new OrganizationAffiliationDaoJdbc(getDefaultDataSource(),
				getPermanentDeleteDataSource(), getFhirContext()).create(aff);

		D d = createResource();
		new ReadAccessHelperImpl().addRole(d, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		orgDao.delete(UUID.fromString(createdMemberOrg.getIdElement().getIdPart()));

		assertReadAccessEntryCount(1, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}

	@Test
	default void testReadAccessTriggerRoleDeleteParent() throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext());
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = new OrganizationAffiliationDaoJdbc(getDefaultDataSource(),
				getPermanentDeleteDataSource(), getFhirContext()).create(aff);

		D d = createResource();
		new ReadAccessHelperImpl().addRole(d, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		orgDao.delete(UUID.fromString(createdParentOrg.getIdElement().getIdPart()));

		assertReadAccessEntryCount(1, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}

	@Test
	default void testReadAccessTriggerRoleDeleteMemberAndParent() throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(getDefaultDataSource(), getPermanentDeleteDataSource(),
				getFhirContext());
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = new OrganizationAffiliationDaoJdbc(getDefaultDataSource(),
				getPermanentDeleteDataSource(), getFhirContext()).create(aff);

		D d = createResource();
		new ReadAccessHelperImpl().addRole(d, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");

		D createdD = getDao().create(d);

		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		orgDao.delete(UUID.fromString(createdMemberOrg.getIdElement().getIdPart()));
		orgDao.delete(UUID.fromString(createdParentOrg.getIdElement().getIdPart()));

		assertReadAccessEntryCount(1, 1, createdD, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(1, 0, createdD, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}

	@Test
	default void testSearchAfterReadAccessTriggerLocal() throws Exception
	{

	}

	default void testSearchWithUserFilterAfterReadAccessTrigger(String accessType, Consumer<D> readAccessModifier,
			Function<Organization, User> userCreator, int expectedCount) throws Exception
	{
		OrganizationDao organizationDao = new OrganizationDaoJdbc(getDefaultDataSource(),
				getPermanentDeleteDataSource(), getFhirContext());
		Organization org = new Organization();
		Organization createdOrg = organizationDao.create(org);

		D d = createResource();
		readAccessModifier.accept(d);

		D createdD = getDao().create(d);
		assertReadAccessEntryCount(1, 1, createdD, accessType);

		SearchQuery<D> query = getDao().createSearchQuery(userCreator.apply(createdOrg), 1, 20)
				.configureParameters(Map.of("id", Collections.singletonList(createdD.getIdElement().getIdPart())));
		PartialResult<D> searchResult = getDao().search(query);
		assertNotNull(searchResult);
		assertEquals(expectedCount, searchResult.getTotal());
		assertNotNull(searchResult.getPartialResult());
		assertEquals(expectedCount, searchResult.getPartialResult().size());
	}

	@Test
	default void testSearchWithUserFilterAfterReadAccessTriggerAllWithLocalUser() throws Exception
	{
		testSearchWithUserFilterAfterReadAccessTrigger(READ_ACCESS_TAG_VALUE_ALL, new ReadAccessHelperImpl()::addAll,
				User::local, 1);
	}

	@Test
	default void testSearchWithUserFilterAfterReadAccessTriggerLocalwithLocalUser() throws Exception
	{
		testSearchWithUserFilterAfterReadAccessTrigger(READ_ACCESS_TAG_VALUE_LOCAL,
				new ReadAccessHelperImpl()::addLocal, User::local, 1);
	}

	@Test
	default void testSearchWithUserFilterAfterReadAccessTriggerAllWithRemoteUser() throws Exception
	{
		testSearchWithUserFilterAfterReadAccessTrigger(READ_ACCESS_TAG_VALUE_ALL, new ReadAccessHelperImpl()::addAll,
				User::remote, 1);
	}

	@Test
	default void testSearchWithUserFilterAfterReadAccessTriggerLocalWithRemoteUser() throws Exception
	{
		testSearchWithUserFilterAfterReadAccessTrigger(READ_ACCESS_TAG_VALUE_LOCAL,
				new ReadAccessHelperImpl()::addLocal, User::remote, 0);
	}
}
