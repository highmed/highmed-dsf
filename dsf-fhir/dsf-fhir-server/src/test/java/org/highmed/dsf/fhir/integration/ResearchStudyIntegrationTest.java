package org.highmed.dsf.fhir.integration;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.highmed.dsf.fhir.dao.GroupDao;
import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResearchStudy.ResearchStudyStatus;
import org.junit.Test;

public class ResearchStudyIntegrationTest extends AbstractIntegrationTest
{
	@Test
	public void testSearchByGroupId() throws Exception
	{
		Group g = new Group();
		readAccessHelper.addLocal(g);
		GroupDao groupDao = getSpringWebApplicationContext().getBean(GroupDao.class);
		String groupId = groupDao.create(g).getIdElement().getIdPart();

		ResearchStudy rs = new ResearchStudy();
		readAccessHelper.addLocal(rs);
		rs.getEnrollmentFirstRep().setReference("Group/" + groupId);
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		String researchStudyId = researchStudyDao.create(rs).getIdElement().getIdPart();

		Bundle resultBundle = getWebserviceClient().searchWithStrictHandling(ResearchStudy.class,
				Map.of("enrollment", Collections.singletonList(groupId)));

		assertNotNull(resultBundle);
		assertEquals(1, resultBundle.getTotal());
		assertNotNull(resultBundle.getEntryFirstRep());
		assertNotNull(resultBundle.getEntryFirstRep().getResource());
		assertEquals(researchStudyId, resultBundle.getEntryFirstRep().getResource().getIdElement().getIdPart());
	}

	@Test
	public void testCreateWithRelatedArtefactUnknownUrl() throws Exception
	{
		String url = "https://foo.bar";
		ResearchStudy researchStudy = getResearchStudy(url);

		ResearchStudy researchStudyResult = getWebserviceClient().create(researchStudy);

		assertNotNull(researchStudyResult);
		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();

		assertEquals(url, relatedArtifactResultUrl);
	}

	@Test
	public void testCreateViaBundleWithRelatedArtefactUnknownUrl() throws Exception
	{
		String url = "https://foo.bar";
		ResearchStudy researchStudy = getResearchStudy(url);

		Bundle bundle = new Bundle().setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(researchStudy).getRequest()
				.setUrl("ResearchStudy").setMethod(HTTPVerb.POST);

		Bundle resultBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(resultBundle);
		assertEquals(1, resultBundle.getEntry().size());
		assertNotNull(resultBundle.getEntry().get(0));
		assertNotNull(resultBundle.getEntry().get(0).getResource());
		assertTrue(resultBundle.getEntry().get(0).getResource() instanceof ResearchStudy);

		ResearchStudy researchStudyResult = (ResearchStudy) resultBundle.getEntry().get(0).getResource();

		assertNotNull(researchStudyResult);
		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();

		assertEquals(url, relatedArtifactResultUrl);
	}

	@Test
	public void testCreateViaBundleWithRelatedArtefactTemporaryUrl() throws Exception
	{
		String binaryUrl = "urn:uuid:" + UUID.randomUUID().toString();
		Binary binary = getBinary();

		ResearchStudy researchStudy = getResearchStudy(binaryUrl);

		Bundle bundle = new Bundle().setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(researchStudy).getRequest()
				.setUrl("ResearchStudy").setMethod(HTTPVerb.POST);
		bundle.addEntry().setFullUrl(binaryUrl).setResource(binary).getRequest().setUrl("Binary")
				.setMethod(HTTPVerb.POST);

		Bundle resultBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(resultBundle);
		assertEquals(2, resultBundle.getEntry().size());
		assertNotNull(resultBundle.getEntry().get(0));
		assertNotNull(resultBundle.getEntry().get(0).getResource());
		assertTrue(resultBundle.getEntry().get(0).getResource() instanceof ResearchStudy);
		assertNotNull(resultBundle.getEntry().get(1));
		assertNotNull(resultBundle.getEntry().get(1).getResource());
		assertTrue(resultBundle.getEntry().get(1).getResource() instanceof Binary);

		ResearchStudy researchStudyResult = (ResearchStudy) resultBundle.getEntry().get(0).getResource();
		Binary binaryResult = (Binary) resultBundle.getEntry().get(1).getResource();

		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();
		String binaryResultUrl = binaryResult.getIdElement()
				.withServerBase(getWebserviceClient().getBaseUrl(), binaryResult.getResourceType().name())
				.toVersionless().getValue();

		assertEquals(binaryResultUrl, relatedArtifactResultUrl);
		assertTrue(new IdType(relatedArtifactResultUrl).isAbsolute());
	}

	@Test
	public void testCreateViaBundleWithRelatedArtefactConditionalUrl() throws Exception
	{
		Binary binary = getBinary();
		Binary binaryResult = getWebserviceClient().create(binary);

		assertNotNull(binaryResult);

		ResearchStudy researchStudy = getResearchStudy("Binary?_id=" + binaryResult.getIdElement().getIdPart());

		Bundle bundle = new Bundle().setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(researchStudy).getRequest()
				.setUrl("ResearchStudy").setMethod(HTTPVerb.POST);

		Bundle resultBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(resultBundle);
		assertEquals(1, resultBundle.getEntry().size());
		assertNotNull(resultBundle.getEntry().get(0));
		assertNotNull(resultBundle.getEntry().get(0).getResource());
		assertTrue(resultBundle.getEntry().get(0).getResource() instanceof ResearchStudy);

		ResearchStudy researchStudyResult = (ResearchStudy) resultBundle.getEntry().get(0).getResource();

		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();
		String binaryResultUrl = binaryResult.getIdElement()
				.withServerBase(getWebserviceClient().getBaseUrl(), binaryResult.getResourceType().name())
				.toVersionless().getValue();

		assertEquals(binaryResultUrl, relatedArtifactResultUrl);
		assertTrue(new IdType(relatedArtifactResultUrl).isAbsolute());
	}

	@Test
	public void testCreateViaBundleWithRelatedArtefactRelativeLiteralInternalUrl() throws Exception
	{
		Binary binary = getBinary();
		Binary binaryResult = getWebserviceClient().create(binary);

		assertNotNull(binaryResult);

		ResearchStudy researchStudy = getResearchStudy(
				binaryResult.getIdElement().toUnqualifiedVersionless().getValue());
		Bundle bundle = new Bundle().setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(researchStudy).getRequest()
				.setUrl("ResearchStudy").setMethod(HTTPVerb.POST);

		Bundle resultBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(resultBundle);
		assertEquals(1, resultBundle.getEntry().size());
		assertNotNull(resultBundle.getEntry().get(0));
		assertNotNull(resultBundle.getEntry().get(0).getResource());
		assertTrue(resultBundle.getEntry().get(0).getResource() instanceof ResearchStudy);

		ResearchStudy researchStudyResult = (ResearchStudy) resultBundle.getEntry().get(0).getResource();

		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();

		String binaryResultUrl = binaryResult.getIdElement()
				.withServerBase(getWebserviceClient().getBaseUrl(), binaryResult.getResourceType().name())
				.toVersionless().getValue();
		assertEquals(binaryResultUrl, relatedArtifactResultUrl);
		assertTrue(new IdType(relatedArtifactResultUrl).isAbsolute());
	}

	@Test
	public void testCreateViaBundleWithRelatedArtefactAbsoluteLiteralInternalUrl() throws Exception
	{
		Binary binary = getBinary();
		Binary binaryResult = getWebserviceClient().create(binary);

		assertNotNull(binaryResult);

		String binaryResultUrl = binaryResult.getIdElement()
				.withServerBase(getWebserviceClient().getBaseUrl(), binaryResult.getResourceType().name())
				.toVersionless().getValue();

		ResearchStudy researchStudy = getResearchStudy(binaryResultUrl);

		Bundle bundle = new Bundle().setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(researchStudy).getRequest()
				.setUrl("ResearchStudy").setMethod(HTTPVerb.POST);

		Bundle resultBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(resultBundle);
		assertEquals(1, resultBundle.getEntry().size());
		assertNotNull(resultBundle.getEntry().get(0));
		assertNotNull(resultBundle.getEntry().get(0).getResource());
		assertTrue(resultBundle.getEntry().get(0).getResource() instanceof ResearchStudy);

		ResearchStudy researchStudyResult = (ResearchStudy) resultBundle.getEntry().get(0).getResource();

		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();

		assertEquals(binaryResultUrl, relatedArtifactResultUrl);
		assertTrue(new IdType(relatedArtifactResultUrl).isAbsolute());
	}

	@Test
	public void testCreateWithRelatedArtefactRelativeLiteralInternalUrl() throws Exception
	{
		Binary binary = getBinary();
		Binary binaryResult = getWebserviceClient().create(binary);

		assertNotNull(binaryResult);

		ResearchStudy researchStudy = getResearchStudy(
				binaryResult.getIdElement().toUnqualifiedVersionless().getValue());
		ResearchStudy researchStudyResult = getWebserviceClient().create(researchStudy);

		assertNotNull(researchStudyResult);
		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();
		String binaryResultUrl = binaryResult.getIdElement()
				.withServerBase(getWebserviceClient().getBaseUrl(), binaryResult.getResourceType().name())
				.toVersionless().getValue();

		assertEquals(binaryResultUrl, relatedArtifactResultUrl);
		assertTrue(new IdType(relatedArtifactResultUrl).isAbsolute());
	}

	@Test
	public void testCreateWithRelatedArtefactRelativeLiteralInternalUrlNonExisting() throws Exception
	{
		ResearchStudy researchStudy = getResearchStudy("Binary/" + UUID.randomUUID().toString());

		expectForbidden(() -> getWebserviceClient().create(researchStudy));
	}

	@Test
	public void testCreateViaBundleWithRelatedArtefactRelativeLiteralInternalUrlNonExisting() throws Exception
	{
		ResearchStudy researchStudy = getResearchStudy("Binary/" + UUID.randomUUID().toString());

		Bundle bundle = new Bundle().setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(researchStudy).getRequest()
				.setUrl("ResearchStudy").setMethod(HTTPVerb.POST);

		expectForbidden(() -> getWebserviceClient().postBundle(bundle));
	}

	@Test
	public void testCreateWithRelatedArtefactAbsoluteLiteralInternalUrl() throws Exception
	{
		Binary binary = getBinary();
		Binary binaryResult = getWebserviceClient().create(binary);

		assertNotNull(binaryResult);

		String binaryResultUrl = binaryResult.getIdElement()
				.withServerBase(getWebserviceClient().getBaseUrl(), binaryResult.getResourceType().name())
				.toVersionless().getValue();

		ResearchStudy researchStudy = getResearchStudy(binaryResultUrl);
		ResearchStudy researchStudyResult = getWebserviceClient().create(researchStudy);

		assertNotNull(researchStudyResult);
		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();

		assertTrue(new IdType(relatedArtifactResultUrl).isAbsolute());
		assertEquals(binaryResultUrl, relatedArtifactResultUrl);
	}

	@Test
	public void testCreateWithRelatedArtefactAbsoluteLiteralExternalUrl() throws Exception
	{
		String literalExternalUrl = "https://www.foo.bar/fhir/Binary/" + UUID.randomUUID().toString();
		ResearchStudy researchStudy = getResearchStudy(literalExternalUrl);
		ResearchStudy researchStudyResult = getWebserviceClient().create(researchStudy);

		assertNotNull(researchStudyResult);
		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();

		assertTrue(new IdType(relatedArtifactResultUrl).isAbsolute());
		assertEquals(literalExternalUrl, relatedArtifactResultUrl);
	}

	@Test
	public void testCreateViaBundleWithRelatedArtefactAbsoluteLiteralExternalUrl() throws Exception
	{
		String literalExternalUrl = "https://www.foo.bar/fhir/Binary/" + UUID.randomUUID().toString();
		ResearchStudy researchStudy = getResearchStudy(literalExternalUrl);

		Bundle bundle = new Bundle().setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(researchStudy).getRequest()
				.setUrl("ResearchStudy").setMethod(HTTPVerb.POST);

		Bundle bundleResult = getWebserviceClient().postBundle(bundle);

		assertEquals(1, bundleResult.getEntry().size());
		assertTrue(bundleResult.getEntry().get(0).getResource() instanceof ResearchStudy);

		ResearchStudy researchStudyResult = (ResearchStudy) bundle.getEntry().get(0).getResource();

		assertNotNull(researchStudyResult);
		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();

		assertTrue(new IdType(relatedArtifactResultUrl).isAbsolute());
		assertEquals(literalExternalUrl, relatedArtifactResultUrl);
	}

	@Test
	public void testDeletePermanentlyByLocalDeletionUser() throws Exception
	{
		ResearchStudy researchStudy = getResearchStudy(null);
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		String researchStudyId = researchStudyDao.create(researchStudy).getIdElement().getIdPart();
		researchStudyDao.delete(UUID.fromString(researchStudyId));

		getWebserviceClient().deletePermanently(ResearchStudy.class, researchStudyId);

		Optional<ResearchStudy> result = researchStudyDao.read(UUID.fromString(researchStudyId));

		assertTrue(result.isEmpty());
	}

	@Test
	public void testDeletePermanentlyByLocalDeletionUserNotMarkedAsDeleted() throws Exception
	{
		ResearchStudy researchStudy = getResearchStudy(null);
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		String researchStudyId = researchStudyDao.create(researchStudy).getIdElement().getIdPart();

		expectBadRequest(() -> getWebserviceClient().deletePermanently(ResearchStudy.class, researchStudyId));
	}

	@Test
	public void testDeletePermanentlyByExternalUser() throws Exception
	{
		ResearchStudy researchStudy = getResearchStudy(null);
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		String researchStudyId = researchStudyDao.create(researchStudy).getIdElement().getIdPart();
		researchStudyDao.delete(UUID.fromString(researchStudyId));

		expectForbidden(() -> getExternalWebserviceClient().deletePermanently(ResearchStudy.class, researchStudyId));
	}

	@Test
	public void testReadAllowedForLocalUserBasedOnVersion() throws Exception
	{
		String researchStudyId = prepareDbWith2VersionsOfResearchStudyWithVersion1AllAccessAndVersion2LocalAccess();

		ResearchStudy readCurrent = getWebserviceClient().read(ResearchStudy.class, researchStudyId);

		assertEquals(researchStudyId, readCurrent.getIdElement().getIdPart());
		assertEquals("2", readCurrent.getMeta().getVersionId());

		ResearchStudy readV1 = getWebserviceClient().read(ResearchStudy.class, researchStudyId, "1");

		assertEquals(researchStudyId, readV1.getIdElement().getIdPart());
		assertEquals("1", readV1.getMeta().getVersionId());

		ResearchStudy readV2 = getWebserviceClient().read(ResearchStudy.class, researchStudyId, "2");

		assertEquals(researchStudyId, readV2.getIdElement().getIdPart());
		assertEquals("2", readV2.getMeta().getVersionId());
	}

	@Test
	public void testReadAllowedForRemoteUserBasedOnVersion() throws Exception
	{
		String researchStudyId = prepareDbWith2VersionsOfResearchStudyWithVersion1AllAccessAndVersion2LocalAccess();

		expectForbidden(() -> getExternalWebserviceClient().read(ResearchStudy.class, researchStudyId));
		expectForbidden(() -> getExternalWebserviceClient().read(ResearchStudy.class, researchStudyId, "2"));

		ResearchStudy readV1 = getExternalWebserviceClient().read(ResearchStudy.class, researchStudyId, "1");

		assertEquals(researchStudyId, readV1.getIdElement().getIdPart());
		assertEquals("1", readV1.getMeta().getVersionId());
	}

	@Test
	public void testReadAllowedForLocalUserBasedOnHistory() throws Exception
	{
		String researchStudyId = prepareDbWith2VersionsOfResearchStudyWithVersion1AllAccessAndVersion2LocalAccess();

		Bundle bundle = getWebserviceClient().history(ResearchStudy.class, researchStudyId);

		assertEquals(2, bundle.getEntry().size());
		assertEquals(2, bundle.getEntry().stream().filter(e -> e.getResource() instanceof ResearchStudy).count());
		assertEquals(researchStudyId, bundle.getEntry().get(0).getResource().getIdElement().getIdPart());
		assertEquals(researchStudyId, bundle.getEntry().get(1).getResource().getIdElement().getIdPart());
		assertEquals("1", bundle.getEntry().get(0).getResource().getMeta().getVersionId());
		assertEquals("2", bundle.getEntry().get(1).getResource().getMeta().getVersionId());
	}

	@Test
	public void testReadAllowedForRemoteUserBasedOnHistory() throws Exception
	{
		String researchStudyId = prepareDbWith2VersionsOfResearchStudyWithVersion1AllAccessAndVersion2LocalAccess();

		Bundle bundle = getExternalWebserviceClient().history(ResearchStudy.class, researchStudyId);

		assertEquals(1, bundle.getEntry().size());
		assertEquals(1, bundle.getEntry().stream().filter(e -> e.getResource() instanceof ResearchStudy).count());
		assertEquals(researchStudyId, bundle.getEntry().get(0).getResource().getIdElement().getIdPart());
		assertEquals("1", bundle.getEntry().get(0).getResource().getMeta().getVersionId());
	}

	@Test
	public void testSearchAllowedForLocalUserBasedOnVersion() throws Exception
	{
		String researchStudyId = prepareDbWith2VersionsOfResearchStudyWithVersion1AllAccessAndVersion2LocalAccess();
		Map<String, List<String>> parameters = Map.of("_id", List.of(researchStudyId));

		assertEquals(1, getWebserviceClient().search(ResearchStudy.class, parameters).getEntry().stream()
				.filter(e -> e.getResource() instanceof ResearchStudy).count());
	}

	@Test
	public void testSearchAllowedForRemoteUserBasedOnVersion() throws Exception
	{
		String researchStudyId = prepareDbWith2VersionsOfResearchStudyWithVersion1AllAccessAndVersion2LocalAccess();
		Map<String, List<String>> parameters = Map.of("_id", List.of(researchStudyId));

		assertEquals(0, getExternalWebserviceClient().search(ResearchStudy.class, parameters).getEntry().stream()
				.filter(e -> e.getResource() instanceof ResearchStudy).count());
	}

	private Binary getBinary()
	{
		Binary binary = new Binary(new CodeType("application/pdf"));
		readAccessHelper.addLocal(binary);

		return binary;
	}

	private String prepareDbWith2VersionsOfResearchStudyWithVersion1AllAccessAndVersion2LocalAccess() throws Exception
	{
		ResearchStudy researchStudy = getResearchStudy("www.test.com");
		readAccessHelper.addAll(researchStudy);

		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		researchStudy = researchStudyDao.create(researchStudy);

		readAccessHelper.addLocal(researchStudy);
		researchStudy = researchStudyDao.update(researchStudy);

		return researchStudy.getIdElement().getIdPart();
	}

	private ResearchStudy getResearchStudy(String url)
	{
		ResearchStudy researchStudy = new ResearchStudy();
		researchStudy.setStatus(ResearchStudyStatus.ACTIVE);
		researchStudy.addIdentifier().setSystem("http://highmed.org/sid/research-study-identifier")
				.setValue(UUID.randomUUID().toString());
		researchStudy.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/extension-participating-ttp")
				.setValue(new Reference().setType("Organization").setIdentifier(new Identifier()
						.setSystem("http://highmed.org/sid/organization-identifier").setValue("Test_Organization")));
		if (!StringUtils.isBlank(url))
			researchStudy.addRelatedArtifact().setType(RelatedArtifactType.DOCUMENTATION).setUrl(url);

		researchStudy.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/extension-participating-medic")
				.setValue(new Reference().setType("Organization").setIdentifier(new Identifier()
						.setSystem("http://highmed.org/sid/organization-identifier").setValue("Test_Organization")));
		readAccessHelper.addLocal(researchStudy);

		return researchStudy;
	}
}
