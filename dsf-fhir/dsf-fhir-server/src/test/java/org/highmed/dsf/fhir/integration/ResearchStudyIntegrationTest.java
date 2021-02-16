package org.highmed.dsf.fhir.integration;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.Map;

import org.highmed.dsf.fhir.dao.GroupDao;
import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.junit.Test;

public class ResearchStudyIntegrationTest extends AbstractIntegrationTest
{
	@Test
	public void testSearchResearchStudyByGroupId() throws Exception
	{
		Group g = new Group();
		GroupDao groupDao = getSpringWebApplicationContext().getBean(GroupDao.class);
		String groupId = groupDao.create(g).getIdElement().getIdPart();

		ResearchStudy rs = new ResearchStudy();
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
}
