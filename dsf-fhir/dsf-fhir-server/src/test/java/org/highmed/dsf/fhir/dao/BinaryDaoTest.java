package org.highmed.dsf.fhir.dao;

import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM;
import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.READ_ACCESS_TAG_VALUE_ALL;
import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.READ_ACCESS_TAG_VALUE_LOCAL;
import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.READ_ACCESS_TAG_VALUE_ORGANIZATION;
import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.READ_ACCESS_TAG_VALUE_ROLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelperImpl;
import org.highmed.dsf.fhir.dao.jdbc.BinaryDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationAffiliationDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.ResearchStudyDaoJdbc;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.junit.Test;

public class BinaryDaoTest extends AbstractResourceDaoTest<Binary, BinaryDao> implements ReadAccessDaoTest<Binary>
{
	private static final String CONTENT_TYPE = "text/plain";
	private static final byte[] DATA1 = "1234567890".getBytes();
	private static final byte[] DATA2 = "VBERi0xLjUNJeLjz9MNCjEwIDAgb2JqDTw8L0xpbmVhcml6ZWQgMS9MIDEzMDA2OC9PIDEyL0UgMTI1NzM1L04gMS9UIDEyOTc2NC9IIFsgNTQ2IDIwNF"
			.getBytes();

	private final OrganizationDao organizationDao = new OrganizationDaoJdbc(defaultDataSource,
			permanentDeleteDataSource, fhirContext);
	private final ResearchStudyDao researchStudyDao = new ResearchStudyDaoJdbc(defaultDataSource,
			permanentDeleteDataSource, fhirContext);
	private final OrganizationAffiliationDao organizationAffiliationDao = new OrganizationAffiliationDaoJdbc(
			defaultDataSource, permanentDeleteDataSource, fhirContext);

	public BinaryDaoTest()
	{
		super(Binary.class, BinaryDaoJdbc::new);
	}

	@Override
	public Binary createResource()
	{
		Binary binary = new Binary();
		binary.setContentType(CONTENT_TYPE);
		binary.setData(DATA1);
		return binary;
	}

	@Override
	protected void checkCreated(Binary resource)
	{
		assertNotNull(resource.getContentType());
		assertEquals(CONTENT_TYPE, resource.getContentType());
		assertNotNull(resource.getData());
		assertTrue(Arrays.equals(DATA1, resource.getData()));
	}

	@Override
	protected Binary updateResource(Binary resource)
	{
		resource.setData(DATA2);
		return resource;
	}

	@Override
	protected void checkUpdates(Binary resource)
	{
		assertNotNull(resource.getData());
		assertTrue(Arrays.equals(DATA2, resource.getData()));
	}

	@Test
	public void testCreateCheckDataNullInJsonColumn() throws Exception
	{
		Binary newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		Binary createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());
		assertEquals("1", createdResource.getIdElement().getVersionIdPart());
		assertEquals("1", createdResource.getMeta().getVersionId());

		try (Connection connection = defaultDataSource.getConnection();
				PreparedStatement statement = connection
						.prepareStatement("SELECT binary_json, binary_data FROM binaries");
				ResultSet result = statement.executeQuery())
		{
			assertTrue(result.next());

			String json = result.getString(1);
			Binary readResource = fhirContext.newJsonParser().parseResource(Binary.class, json);
			assertNotNull(readResource);
			assertNull(readResource.getData());

			byte[] data = result.getBytes(2);
			assertNotNull(data);
			assertTrue(Arrays.equals(DATA1, data));

			assertFalse(result.next());
		}
	}

	@Test
	public void testSearch() throws Exception
	{
		Organization org = new Organization();
		org.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");
		org.setActive(true);
		org.addIdentifier().setSystem("http://highmed.org/sid/organization-identifier").setValue("Test_Organization");

		Organization createdOrg = organizationDao.create(org);
		assertNotNull(createdOrg);

		Binary b = createResource();
		b.getSecurityContext().setReference("Organization/" + createdOrg.getIdElement().getIdPart());
		Binary createdB = dao.create(b);
		assertNotNull(createdB);

		SearchQuery<Binary> query = dao.createSearchQuery(User.local(org), 1, 1);
		query.configureParameters(Collections.emptyMap());
		assertNotNull(query);

		PartialResult<Binary> result = dao.search(query);
		assertNotNull(result);
	}

	@Test
	public void testSearchBinaryWithSecurityContext() throws Exception
	{
		Organization org = new Organization();
		org.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");
		org.setActive(true);
		org.addIdentifier().setSystem("http://highmed.org/sid/organization-identifier").setValue("Test_Organization");
		Organization createdOrg = organizationDao.create(org);
		assertNotNull(createdOrg);

		ResearchStudy rs = new ResearchStudy();
		rs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("LOCAL");
		ResearchStudy createdRs = researchStudyDao.create(rs);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));
		Binary createdB = dao.create(b);
		assertNotNull(createdB);

		SearchQuery<Binary> query = dao.createSearchQuery(User.local(org), 1, 1);
		query.configureParameters(Collections.emptyMap());
		assertNotNull(query);

		PartialResult<Binary> result = dao.search(query);
		assertNotNull(result);
		assertEquals(1, result.getTotal());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Binary foundBinary = result.getPartialResult().get(0);
		assertNotNull(foundBinary);
		assertEquals(createdB.getContentAsBase64(), foundBinary.getContentAsBase64());
	}

	@Test
	public void testSearchBinaryWithSecurityContextOrganization() throws Exception
	{
		Organization org = new Organization();
		org.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");
		org.setActive(true);
		org.addIdentifier().setSystem("http://highmed.org/sid/organization-identifier").setValue("Test_Organization");
		Organization createdOrg = organizationDao.create(org);
		assertNotNull(createdOrg);

		ResearchStudy rs = new ResearchStudy();
		rs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("LOCAL");
		rs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ORGANIZATION")
				.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/extension-read-access-organization")
				.setValue(new Identifier().setSystem("http://highmed.org/sid/organization-identifier")
						.setValue("Test_Organization"));
		ResearchStudy createdRs = researchStudyDao.create(rs);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));
		Binary createdB = dao.create(b);
		assertNotNull(createdB);

		SearchQuery<Binary> query = dao.createSearchQuery(User.local(createdOrg), 1, 1);
		query.configureParameters(Collections.emptyMap());
		assertNotNull(query);

		PartialResult<Binary> result = dao.search(query);
		assertNotNull(result);
		assertEquals(1, result.getTotal());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Binary foundBinary = result.getPartialResult().get(0);
		assertNotNull(foundBinary);
		assertEquals(createdB.getContentAsBase64(), foundBinary.getContentAsBase64());
	}

	@Test
	public void testSearchBinaryWithSecurityContextRole() throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem("http://highmed.org/sid/organization-identifier")
				.setValue("Test_Consortium");

		Organization memberOrg = new Organization();
		memberOrg.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem("http://highmed.org/sid/organization-identifier")
				.setValue("Test_Organization");

		Organization createdParentOrg = organizationDao.create(parentOrg);
		assertNotNull(createdParentOrg);
		Organization createdMemberOrg = organizationDao.create(memberOrg);
		assertNotNull(createdMemberOrg);

		OrganizationAffiliation affiliation = new OrganizationAffiliation();
		affiliation.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");
		affiliation.setActive(true);
		affiliation.setOrganization(new Reference(createdParentOrg.getIdElement().toVersionless()));
		affiliation.setParticipatingOrganization(new Reference(createdMemberOrg.getIdElement().toVersionless()));
		affiliation.getCodeFirstRep().getCodingFirstRep()
				.setSystem("http://highmed.org/fhir/CodeSystem/organization-role").setCode("MeDIC");

		organizationAffiliationDao.create(affiliation);

		ResearchStudy rs = new ResearchStudy();
		rs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("LOCAL");
		Extension ex = rs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag")
				.setCode("ROLE").addExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-read-access-consortium-role");
		ex.addExtension().setUrl("consortium").setValue(new Identifier()
				.setSystem("http://highmed.org/sid/organization-identifier").setValue("Test_Consortium"));
		ex.addExtension().setUrl("role").setValue(
				new Coding().setSystem("http://highmed.org/fhir/CodeSystem/organization-role").setCode("MeDIC"));
		ResearchStudy createdRs = researchStudyDao.create(rs);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));
		Binary createdB = dao.create(b);
		assertNotNull(createdB);

		SearchQuery<Binary> query = dao.createSearchQuery(User.local(createdMemberOrg), 1, 1);
		query.configureParameters(Collections.emptyMap());
		assertNotNull(query);

		PartialResult<Binary> result = dao.search(query);
		assertNotNull(result);
		assertEquals(1, result.getTotal());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Binary foundBinary = result.getPartialResult().get(0);
		assertNotNull(foundBinary);
		assertEquals(createdB.getContentAsBase64(), foundBinary.getContentAsBase64());
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

	private void testReadAccessTriggerSecurityContext(String accessType, Consumer<ResearchStudy> readAccessModifier,
			Function<ResearchStudy, IdType> securityContext) throws Exception
	{
		ResearchStudy rS = new ResearchStudy();
		readAccessModifier.accept(rS);
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		assertReadAccessEntryCount(1, 1, createdRs, accessType);

		Binary b = createResource();
		b.setSecurityContext(new Reference(securityContext.apply(createdRs)));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(2, 1, createdRs, accessType);
		assertReadAccessEntryCount(2, 1, createdB, accessType);
	}

	@Test
	public void testReadAccessTriggerSecurityContextAll() throws Exception
	{
		testReadAccessTriggerSecurityContext(READ_ACCESS_TAG_VALUE_ALL, new ReadAccessHelperImpl()::addAll,
				rs -> rs.getIdElement().toUnqualifiedVersionless());
	}

	@Test
	public void testReadAccessTriggerSecurityContextLocal() throws Exception
	{
		testReadAccessTriggerSecurityContext(READ_ACCESS_TAG_VALUE_LOCAL, new ReadAccessHelperImpl()::addLocal,
				rs -> rs.getIdElement().toUnqualifiedVersionless());
	}

	@Test
	public void testReadAccessTriggerSecurityContextVersionSpecificAll() throws Exception
	{
		testReadAccessTriggerSecurityContext(READ_ACCESS_TAG_VALUE_ALL, new ReadAccessHelperImpl()::addAll,
				rs -> rs.getIdElement());
	}

	@Test
	public void testReadAccessTriggerSecurityContextVersionSpecificLocal() throws Exception
	{
		testReadAccessTriggerSecurityContext(READ_ACCESS_TAG_VALUE_LOCAL, new ReadAccessHelperImpl()::addLocal,
				rs -> rs.getIdElement());
	}

	private void testReadAccessTriggerSecurityContextOrganization(Function<ResearchStudy, IdType> securityContext)
			throws SQLException, Exception
	{
		Organization org = new Organization();
		org.setActive(true);
		org.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org.com");
		Organization createdOrg = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(org);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addOrganization(rS, createdOrg);
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
	}

	@Test
	public void testReadAccessTriggerSecurityContextOrganization() throws Exception
	{
		testReadAccessTriggerSecurityContextOrganization(rs -> rs.getIdElement().toUnqualifiedVersionless());
	}

	@Test
	public void testReadAccessTriggerSecurityContextVersionSpecificOrganization() throws Exception
	{
		testReadAccessTriggerSecurityContextOrganization(rs -> rs.getIdElement());
	}

	@Test
	public void testReadAccessTriggerSecurityContextOrganization2Organizations1Matching() throws Exception
	{
		OrganizationDaoJdbc organizationDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource,
				fhirContext);

		Organization org1 = new Organization();
		org1.setActive(true);
		org1.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org1.com");
		Organization createdOrg1 = organizationDao.create(org1);

		Organization org2 = new Organization();
		org2.setActive(true);
		org2.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org2.com");
		Organization createdOrg2 = organizationDao.create(org2);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addOrganization(rS, createdOrg1);
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg1);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg1);
		assertReadAccessEntryCount(4, 0, createdRs, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg2);
		assertReadAccessEntryCount(4, 0, createdB, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg2);
	}

	@Test
	public void testReadAccessTriggerSecurityContextOrganization2Organizations2Matching() throws Exception
	{
		OrganizationDaoJdbc organizationDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource,
				fhirContext);

		Organization org1 = new Organization();
		org1.setActive(true);
		org1.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org1.com");
		Organization createdOrg1 = organizationDao.create(org1);

		Organization org2 = new Organization();
		org2.setActive(true);
		org2.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org2.com");
		Organization createdOrg2 = organizationDao.create(org2);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addOrganization(rS, createdOrg1);
		new ReadAccessHelperImpl().addOrganization(rS, createdOrg2);
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(6, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(6, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(6, 1, createdRs, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg1);
		assertReadAccessEntryCount(6, 1, createdB, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg1);
		assertReadAccessEntryCount(6, 1, createdRs, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg2);
		assertReadAccessEntryCount(6, 1, createdB, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg2);
	}

	private void testReadAccessTriggerSecurityContextRole(Function<ResearchStudy, IdType> securityContext)
			throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = new OrganizationAffiliationDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext).create(aff);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addRole(rS, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(securityContext.apply(createdRs)));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}

	@Test
	public void testReadAccessTriggerSecurityContextRole() throws Exception
	{
		testReadAccessTriggerSecurityContextRole(r -> r.getIdElement().toUnqualifiedVersionless());
	}

	@Test
	public void testReadAccessTriggerSecurityContextVersionSpecificRole() throws Exception
	{
		testReadAccessTriggerSecurityContextRole(r -> r.getIdElement());
	}

	@Test
	public void testReadAccessTriggerSecurityContextRole2Organizations1Matching() throws Exception
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

		OrganizationDao orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
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

		OrganizationAffiliation createdAff1 = new OrganizationAffiliationDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext).create(aff1);

		OrganizationAffiliation aff2 = new OrganizationAffiliation();
		aff2.setActive(true);
		aff2.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("TTP");
		aff2.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff2.getParticipatingOrganization()
				.setReference("Organization/" + createdMemberOrg2.getIdElement().getIdPart());

		OrganizationAffiliation createdAff2 = new OrganizationAffiliationDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext).create(aff2);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addRole(rS, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg1, createdAff1);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg1, createdAff1);

		assertReadAccessEntryCount(4, 0, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg2, createdAff2);
		assertReadAccessEntryCount(4, 0, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg2, createdAff2);
	}

	@Test
	public void testReadAccessTriggerSecurityContextRole2Organizations2Matching() throws Exception
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

		OrganizationDao orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
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

		OrganizationAffiliation createdAff1 = new OrganizationAffiliationDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext).create(aff1);

		OrganizationAffiliation aff2 = new OrganizationAffiliation();
		aff2.setActive(true);
		aff2.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff2.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff2.getParticipatingOrganization()
				.setReference("Organization/" + createdMemberOrg2.getIdElement().getIdPart());

		OrganizationAffiliation createdAff2 = new OrganizationAffiliationDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext).create(aff2);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addRole(rS, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(6, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(6, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(6, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg1, createdAff1);
		assertReadAccessEntryCount(6, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg1, createdAff1);

		assertReadAccessEntryCount(6, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg2, createdAff2);
		assertReadAccessEntryCount(6, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg2, createdAff2);
	}

	private void testReadAccessTriggerSecurityContextUpdate(String accessType,
			Consumer<ResearchStudy> readAccessModifier) throws Exception
	{
		final ResearchStudyDaoJdbc researchStudyDao = new ResearchStudyDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext);

		ResearchStudy rS = new ResearchStudy();
		readAccessModifier.accept(rS);
		ResearchStudy v1 = researchStudyDao.create(rS);
		assertEquals(1L, (long) v1.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(1, 1, v1, accessType);

		Binary b = createResource();
		b.setSecurityContext(new Reference(v1.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(2, 1, v1, accessType);
		assertReadAccessEntryCount(2, 1, createdB, accessType);

		v1.getMeta().setTag(Collections.emptyList());
		ResearchStudy v2 = researchStudyDao.update(v1);
		assertEquals(2L, (long) v2.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(1, 1, v1, accessType);
		assertReadAccessEntryCount(1, 0, v2, accessType);
		assertReadAccessEntryCount(1, 0, createdB, accessType);

		readAccessModifier.accept(v2);
		ResearchStudy v3 = researchStudyDao.update(v2);
		assertEquals(3L, (long) v3.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(3, 1, v1, accessType);
		assertReadAccessEntryCount(3, 0, v2, accessType);
		assertReadAccessEntryCount(3, 1, v3, accessType);
		assertReadAccessEntryCount(3, 1, createdB, accessType);
	}

	@Test
	public void testReadAccessTriggerSecurityContextAllUpdate() throws Exception
	{
		testReadAccessTriggerSecurityContextUpdate(READ_ACCESS_TAG_VALUE_ALL, new ReadAccessHelperImpl()::addAll);
	}

	@Test
	public void testReadAccessTriggerSecurityContextLocalUpdate() throws Exception
	{
		testReadAccessTriggerSecurityContextUpdate(READ_ACCESS_TAG_VALUE_LOCAL, new ReadAccessHelperImpl()::addLocal);
	}

	private void testReadAccessTriggerSecurityContextVersionSpecificUpdate(String accessType,
			Consumer<ResearchStudy> readAccessModifier) throws Exception
	{
		final ResearchStudyDaoJdbc researchStudyDao = new ResearchStudyDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext);

		ResearchStudy rS = new ResearchStudy();
		readAccessModifier.accept(rS);
		ResearchStudy v1 = researchStudyDao.create(rS);
		assertEquals(1L, (long) v1.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(1, 1, v1, accessType);

		Binary b = createResource();
		b.setSecurityContext(new Reference(v1.getIdElement()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(2, 1, v1, accessType);
		assertReadAccessEntryCount(2, 1, createdB, accessType);

		v1.getMeta().setTag(Collections.emptyList());
		ResearchStudy v2 = researchStudyDao.update(v1);
		assertEquals(2L, (long) v2.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(2, 1, v1, accessType);
		assertReadAccessEntryCount(2, 0, v2, accessType);
		assertReadAccessEntryCount(2, 1, createdB, accessType);

		readAccessModifier.accept(v2);
		ResearchStudy v3 = researchStudyDao.update(v2);
		assertEquals(3L, (long) v3.getIdElement().getVersionIdPartAsLong());

		assertReadAccessEntryCount(3, 1, v1, accessType);
		assertReadAccessEntryCount(3, 0, v2, accessType);
		assertReadAccessEntryCount(3, 1, v3, accessType);
		assertReadAccessEntryCount(3, 1, createdB, accessType);
	}

	@Test
	public void testReadAccessTriggerSecurityContextVersionSpecificAllUpdate() throws Exception
	{
		testReadAccessTriggerSecurityContextVersionSpecificUpdate(READ_ACCESS_TAG_VALUE_ALL,
				new ReadAccessHelperImpl()::addAll);
	}

	@Test
	public void testReadAccessTriggerSecurityContextVersionSpecificLocalUpdate() throws Exception
	{
		testReadAccessTriggerSecurityContextVersionSpecificUpdate(READ_ACCESS_TAG_VALUE_LOCAL,
				new ReadAccessHelperImpl()::addLocal);
	}

	@Test
	public void testReadAccessTriggerSecurityContextOrganizationUpdate() throws Exception
	{
		final OrganizationDaoJdbc organizationDao = new OrganizationDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext);

		Organization org = new Organization();
		org.setActive(true);
		org.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org.com");
		Organization createdOrg = organizationDao.create(org);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addOrganization(rS, createdOrg);
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);

		createdOrg.setActive(false);
		Organization updatedOrg = organizationDao.update(createdOrg);

		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 0, createdRs, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
		assertReadAccessEntryCount(2, 0, createdB, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);

		updatedOrg.setActive(true);
		organizationDao.update(updatedOrg);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
	}

	@Test
	public void testReadAccessTriggerSecurityContextRoleUpdate() throws Exception
	{
		final OrganizationAffiliationDaoJdbc organizationAffiliationDao = new OrganizationAffiliationDaoJdbc(
				defaultDataSource, permanentDeleteDataSource, fhirContext);

		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = organizationAffiliationDao.create(aff);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addRole(rS, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		createdAff.setActive(false);
		OrganizationAffiliation updatedAff = organizationAffiliationDao.update(createdAff);

		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(2, 0, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, updatedAff);
		assertReadAccessEntryCount(2, 0, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, updatedAff);

		updatedAff.setActive(true);
		organizationAffiliationDao.update(updatedAff);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, updatedAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, updatedAff);
	}

	@Test
	public void testReadAccessTriggerSecurityContextRoleUpdateMemberOrganizationNonActive() throws Exception
	{
		final OrganizationAffiliationDaoJdbc organizationAffiliationDao = new OrganizationAffiliationDaoJdbc(
				defaultDataSource, permanentDeleteDataSource, fhirContext);

		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = organizationAffiliationDao.create(aff);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addRole(rS, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		createdMemberOrg.setActive(false);
		Organization updatedMemberOrg = orgDao.update(createdMemberOrg);

		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 0, createdRs, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg, createdAff);
		assertReadAccessEntryCount(2, 0, createdB, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg, createdAff);

		updatedMemberOrg.setActive(true);
		orgDao.update(updatedMemberOrg);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg, createdAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg, createdAff);
	}

	@Test
	public void testReadAccessTriggerSecurityContextRoleUpdateParentOrganizationNonActive() throws Exception
	{
		final OrganizationAffiliationDaoJdbc organizationAffiliationDao = new OrganizationAffiliationDaoJdbc(
				defaultDataSource, permanentDeleteDataSource, fhirContext);

		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = organizationAffiliationDao.create(aff);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addRole(rS, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		createdParentOrg.setActive(false);
		Organization updatedParentOrg = orgDao.update(createdParentOrg);

		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 0, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(2, 0, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		updatedParentOrg.setActive(true);
		orgDao.update(updatedParentOrg);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}

	@Test
	public void testReadAccessTriggerSecurityContextRoleUpdateMemberAndParentOrganizationNonActive() throws Exception
	{
		final OrganizationAffiliationDaoJdbc organizationAffiliationDao = new OrganizationAffiliationDaoJdbc(
				defaultDataSource, permanentDeleteDataSource, fhirContext);

		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		OrganizationAffiliation createdAff = organizationAffiliationDao.create(aff);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addRole(rS, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);
		System.out.println(fhirContext.newJsonParser().encodeResourceToString(createdB));

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		createdParentOrg.setActive(false);
		createdMemberOrg.setActive(false);
		Organization updatedParentOrg = orgDao.update(createdParentOrg);
		Organization updatedMemberOrg = orgDao.update(createdMemberOrg);

		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 0, createdRs, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg, createdAff);
		assertReadAccessEntryCount(2, 0, createdB, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg, createdAff);

		updatedParentOrg.setActive(true);
		orgDao.update(updatedParentOrg);

		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 0, createdRs, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg, createdAff);
		assertReadAccessEntryCount(2, 0, createdB, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg, createdAff);

		updatedMemberOrg.setActive(true);
		Organization updatedMemberOrg2 = orgDao.update(updatedMemberOrg);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg2, createdAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, updatedMemberOrg2, createdAff);
	}

	private void testReadAccessTriggerSecurityContextDelete(String accessType,
			Consumer<ResearchStudy> readAccessModifier) throws Exception
	{
		final ResearchStudyDaoJdbc researchStudyDao = new ResearchStudyDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext);

		ResearchStudy rS = new ResearchStudy();
		readAccessModifier.accept(rS);
		ResearchStudy createdRs = researchStudyDao.create(rS);

		assertReadAccessEntryCount(1, 1, createdRs, accessType);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(2, 1, createdRs, accessType);
		assertReadAccessEntryCount(2, 1, createdB, accessType);

		researchStudyDao.delete(UUID.fromString(createdRs.getIdElement().getIdPart()));

		assertReadAccessEntryCount(1, 1, createdRs, accessType);
		assertReadAccessEntryCount(1, 0, createdB, accessType);
	}

	@Test
	public void testReadAccessTriggerSecurityContextAllDelete() throws Exception
	{
		testReadAccessTriggerSecurityContextDelete(READ_ACCESS_TAG_VALUE_ALL, new ReadAccessHelperImpl()::addAll);
	}

	@Test
	public void testReadAccessTriggerSecurityContextLocalDelete() throws Exception
	{
		testReadAccessTriggerSecurityContextUpdate(READ_ACCESS_TAG_VALUE_LOCAL, new ReadAccessHelperImpl()::addLocal);
	}

	@Test
	public void testReadAccessTriggerSecurityContextOrganizationDelete() throws Exception
	{
		final OrganizationDaoJdbc organizationDao = new OrganizationDaoJdbc(getDefaultDataSource(),
				getPermanentDeleteDataSource(), getFhirContext());

		Organization org = new Organization();
		org.setActive(true);
		org.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("org.com");
		Organization createdOrg = organizationDao.create(org);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addOrganization(rS, createdOrg);
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);

		organizationDao.delete(UUID.fromString(createdOrg.getIdElement().getIdPart()));

		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 0, createdRs, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
		assertReadAccessEntryCount(2, 0, createdB, READ_ACCESS_TAG_VALUE_ORGANIZATION, createdOrg);
	}

	@Test
	public void testReadAccessTriggerSecurityContextRoleDelete() throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		final OrganizationAffiliationDaoJdbc orgAffDao = new OrganizationAffiliationDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext);

		OrganizationAffiliation createdAff = orgAffDao.create(aff);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addRole(rS, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		orgAffDao.delete(UUID.fromString(createdAff.getIdElement().getIdPart()));

		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(2, 0, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(2, 0, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}

	@Test
	public void testReadAccessTriggerSecurityContextRoleDeleteMember() throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		final OrganizationAffiliationDaoJdbc orgAffDao = new OrganizationAffiliationDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext);

		OrganizationAffiliation createdAff = orgAffDao.create(aff);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addRole(rS, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		orgDao.delete(UUID.fromString(createdMemberOrg.getIdElement().getIdPart()));

		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(2, 0, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(2, 0, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}

	@Test
	public void testReadAccessTriggerSecurityContextRoleDeleteParent() throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		final OrganizationAffiliationDaoJdbc orgAffDao = new OrganizationAffiliationDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext);

		OrganizationAffiliation createdAff = orgAffDao.create(aff);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addRole(rS, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		orgDao.delete(UUID.fromString(createdParentOrg.getIdElement().getIdPart()));

		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(2, 0, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(2, 0, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}

	@Test
	public void testReadAccessTriggerSecurityContextRoleDeleteMemberAndParent() throws Exception
	{
		Organization parentOrg = new Organization();
		parentOrg.setActive(true);
		parentOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("parent.com");

		Organization memberOrg = new Organization();
		memberOrg.setActive(true);
		memberOrg.addIdentifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");

		OrganizationDao orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext);
		Organization createdParentOrg = orgDao.create(parentOrg);
		Organization createdMemberOrg = orgDao.create(memberOrg);

		OrganizationAffiliation aff = new OrganizationAffiliation();
		aff.setActive(true);
		aff.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		aff.getOrganization().setReference("Organization/" + createdParentOrg.getIdElement().getIdPart());
		aff.getParticipatingOrganization().setReference("Organization/" + createdMemberOrg.getIdElement().getIdPart());

		final OrganizationAffiliationDaoJdbc orgAffDao = new OrganizationAffiliationDaoJdbc(defaultDataSource,
				permanentDeleteDataSource, fhirContext);

		OrganizationAffiliation createdAff = orgAffDao.create(aff);

		ResearchStudy rS = new ResearchStudy();
		new ReadAccessHelperImpl().addRole(rS, "parent.com", "http://highmed.org/fhir/CodeSystem/organization-role",
				"MeDIC");
		ResearchStudy createdRs = new ResearchStudyDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext)
				.create(rS);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toUnqualifiedVersionless()));
		Binary createdB = dao.create(b);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(4, 1, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(4, 1, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);

		orgDao.delete(UUID.fromString(createdMemberOrg.getIdElement().getIdPart()));
		orgDao.delete(UUID.fromString(createdParentOrg.getIdElement().getIdPart()));

		assertReadAccessEntryCount(2, 1, createdRs, READ_ACCESS_TAG_VALUE_LOCAL);
		assertReadAccessEntryCount(2, 1, createdB, READ_ACCESS_TAG_VALUE_LOCAL);

		assertReadAccessEntryCount(2, 0, createdRs, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
		assertReadAccessEntryCount(2, 0, createdB, READ_ACCESS_TAG_VALUE_ROLE, createdMemberOrg, createdAff);
	}
}
