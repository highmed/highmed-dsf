package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.jdbc.BinaryDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationAffiliationDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.ResearchStudyDaoJdbc;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.junit.Test;

public class BinaryDaoTest extends AbstractResourceDaoTest<Binary, BinaryDao>
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
	protected Binary createResource()
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
				.setSystem("http://highmed.org/fhir/CodeSystem/organization-type").setCode("MeDIC");

		organizationAffiliationDao.create(affiliation);

		ResearchStudy rs = new ResearchStudy();
		rs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("LOCAL");
		Extension ex = rs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag")
				.setCode("ROLE").addExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-read-access-consortium-role");
		ex.addExtension().setUrl("consortium").setValue(new Identifier()
				.setSystem("http://highmed.org/sid/organization-identifier").setValue("Test_Consortium"));
		ex.addExtension().setUrl("role").setValue(
				new Coding().setSystem("http://highmed.org/fhir/CodeSystem/organization-type").setCode("MeDIC"));
		ResearchStudy createdRs = researchStudyDao.create(rs);

		Binary b = createResource();
		b.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));
		Binary createdB = dao.create(b);
		assertNotNull(createdB);

		SearchQuery<Binary> query = dao.createSearchQuery(User.local(memberOrg), 1, 1);
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
}
