package org.highmed.dsf.fhir.integration;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.highmed.dsf.fhir.dao.GroupDao;
import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.junit.Assert;
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
	public void testSearchResearchStudyDeletePermanentlyByLocalDeletionUser() throws Exception
	{
		ResearchStudy rs = new ResearchStudy();
		readAccessHelper.addLocal(rs);
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		String researchStudyId = researchStudyDao.create(rs).getIdElement().getIdPart();
		researchStudyDao.delete(UUID.fromString(researchStudyId));

		getWebserviceClient().deletePermanently(ResearchStudy.class, researchStudyId);

		Optional<ResearchStudy> result = researchStudyDao.read(UUID.fromString(researchStudyId));

		assertTrue(result.isEmpty());
	}

	@Test
	public void testSearchResearchStudyDeletePermanentlyByLocalDeletionUserNotMarkedAsDeleted() throws Exception
	{
		ResearchStudy rs = new ResearchStudy();
		readAccessHelper.addLocal(rs);
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		String researchStudyId = researchStudyDao.create(rs).getIdElement().getIdPart();

		try
		{
			getWebserviceClient().deletePermanently(ResearchStudy.class, researchStudyId);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
		}
	}

	@Test
	public void testSearchResearchStudyDeletePermanentlyByExternalUser() throws Exception
	{
		ResearchStudy rs = new ResearchStudy();
		readAccessHelper.addLocal(rs);
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		String researchStudyId = researchStudyDao.create(rs).getIdElement().getIdPart();
		researchStudyDao.delete(UUID.fromString(researchStudyId));

		try
		{
			getExternalWebserviceClient().deletePermanently(ResearchStudy.class, researchStudyId);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
		}
	}
}
