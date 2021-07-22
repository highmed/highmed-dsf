package org.highmed.dsf.fhir.integration;

import static junit.framework.TestCase.assertEquals;
import static org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION;
import static org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.DOCUMENTATION;
import static org.hl7.fhir.r4.model.ResearchStudy.ResearchStudyStatus.ACTIVE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.highmed.dsf.fhir.dao.GroupDao;
import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.Test;

public class ResearchStudyIntegrationTest extends AbstractIntegrationTest
{
	@Test
	public void testSearchResearchStudyByGroupId() throws Exception
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
	public void testResearchStudyRelatedArtefactUnknownUrl() throws Exception
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
	public void testResearchStudyRelatedArtefactTemporaryUrlResolution() throws Exception
	{
		String binaryUrl = "urn:uuid:" + UUID.randomUUID().toString();
		Binary binary = getBinary();

		ResearchStudy researchStudy = getResearchStudy(binaryUrl);

		Bundle bundle = new Bundle().setType(TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(researchStudy).getRequest()
				.setUrl("ResearchStudy").setMethod(Bundle.HTTPVerb.POST);
		bundle.addEntry().setFullUrl(binaryUrl).setResource(binary).getRequest().setUrl("Binary")
				.setMethod(Bundle.HTTPVerb.POST);

		Bundle resultBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(resultBundle);
		assertEquals(2, resultBundle.getEntry().size());
		assertEquals(ResourceType.ResearchStudy, resultBundle.getEntry().get(0).getResource().getResourceType());
		assertEquals(ResourceType.Binary, resultBundle.getEntry().get(1).getResource().getResourceType());

		ResearchStudy researchStudyResult = (ResearchStudy) resultBundle.getEntry().get(0).getResource();
		Binary binaryResult = (Binary) resultBundle.getEntry().get(1).getResource();

		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();
		String binaryResultUrl = binaryResult.getIdElement()
				.withServerBase(getWebserviceClient().getBaseUrl(), ResourceType.Binary.name()).toVersionless()
				.getValue();

		assertTrue(new IdType(relatedArtifactResultUrl).isAbsolute());
		assertEquals(binaryResultUrl, relatedArtifactResultUrl);
	}

	@Test
	public void testResearchStudyRelatedArtefactConditionalUrlResolution() throws Exception
	{
		Binary binary = getBinary();
		Binary binaryResult = getWebserviceClient().create(binary);

		assertNotNull(binaryResult);

		ResearchStudy researchStudy = getResearchStudy("Binary?_id=" + binaryResult.getIdElement().getIdPart());

		Bundle bundle = new Bundle().setType(TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(researchStudy).getRequest()
				.setUrl("ResearchStudy").setMethod(Bundle.HTTPVerb.POST);

		Bundle resultBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(resultBundle);
		assertEquals(1, resultBundle.getEntry().size());
		assertEquals(ResourceType.ResearchStudy, resultBundle.getEntry().get(0).getResource().getResourceType());

		ResearchStudy researchStudyResult = (ResearchStudy) resultBundle.getEntry().get(0).getResource();

		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();
		String binaryResultUrl = binaryResult.getIdElement()
				.withServerBase(getWebserviceClient().getBaseUrl(), ResourceType.Binary.name()).toVersionless()
				.getValue();

		assertTrue(new IdType(relatedArtifactResultUrl).isAbsolute());
		assertEquals(binaryResultUrl, relatedArtifactResultUrl);
	}

	@Test
	public void testResearchStudyRelatedArtefactLiteralInternalUrlResolution() throws Exception
	{
		Binary binary = getBinary();
		Binary binaryResult = getWebserviceClient().create(binary);

		assertNotNull(binaryResult);

		ResearchStudy researchStudy = getResearchStudy(
				binaryResult.getIdElement().toUnqualifiedVersionless().getValue());
		Bundle bundle = new Bundle().setType(TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(researchStudy).getRequest()
				.setUrl("ResearchStudy").setMethod(Bundle.HTTPVerb.POST);

		Bundle resultBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(resultBundle);
		assertEquals(1, resultBundle.getEntry().size());
		assertEquals(ResourceType.ResearchStudy, resultBundle.getEntry().get(0).getResource().getResourceType());

		ResearchStudy researchStudyResult = (ResearchStudy) resultBundle.getEntry().get(0).getResource();

		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();
		String binaryResultUrl = binaryResult.getIdElement()
				.withServerBase(getWebserviceClient().getBaseUrl(), ResourceType.Binary.name()).toVersionless()
				.getValue();

		assertTrue(new IdType(relatedArtifactResultUrl).isAbsolute());
		assertEquals(binaryResultUrl, relatedArtifactResultUrl);
	}

	@Test
	public void testResearchStudyRelatedArtefactLiteralInternalUrlResolution2() throws Exception
	{
		Binary binary = getBinary();
		Binary binaryResult = getWebserviceClient().create(binary);

		assertNotNull(binaryResult);

		String binaryResultUrl = binaryResult.getIdElement()
				.withServerBase(getWebserviceClient().getBaseUrl(), ResourceType.Binary.name()).toVersionless()
				.getValue();

		ResearchStudy researchStudy = getResearchStudy(binaryResultUrl);

		Bundle bundle = new Bundle().setType(TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(researchStudy).getRequest()
				.setUrl("ResearchStudy").setMethod(Bundle.HTTPVerb.POST);

		Bundle resultBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(resultBundle);
		assertEquals(1, resultBundle.getEntry().size());
		assertEquals(ResourceType.ResearchStudy, resultBundle.getEntry().get(0).getResource().getResourceType());

		ResearchStudy researchStudyResult = (ResearchStudy) resultBundle.getEntry().get(0).getResource();

		assertEquals(1, researchStudyResult.getRelatedArtifact().size());

		String relatedArtifactResultUrl = researchStudyResult.getRelatedArtifact().get(0).getUrl();

		assertTrue(new IdType(relatedArtifactResultUrl).isAbsolute());
		assertEquals(binaryResultUrl, relatedArtifactResultUrl);
	}

	private Binary getBinary()
	{
		Binary binary = new Binary(new CodeType("application/pdf"));
		readAccessHelper.addLocal(binary);

		return binary;
	}

	private ResearchStudy getResearchStudy(String url)
	{
		ResearchStudy researchStudy = new ResearchStudy();
		researchStudy.setStatus(ACTIVE);
		researchStudy.addIdentifier().setSystem("http://highmed.org/sid/research-study-identifier")
				.setValue(UUID.randomUUID().toString());
		researchStudy.addRelatedArtifact().setType(DOCUMENTATION).setUrl(url);
		researchStudy.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/extension-participating-ttp")
				.setValue(new Reference().setType("Organization").setIdentifier(new Identifier()
						.setSystem("http://highmed.org/sid/organization-identifier").setValue("Test_Organization")));
		readAccessHelper.addLocal(researchStudy);

		return researchStudy;
	}
}
