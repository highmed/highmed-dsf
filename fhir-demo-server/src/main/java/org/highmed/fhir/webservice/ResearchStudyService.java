package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.ResearchStudyDao;
import org.hl7.fhir.r4.model.ResearchStudy;

@Path(ResearchStudyService.RESOURCE_TYPE_NAME)
public class ResearchStudyService extends AbstractService<ResearchStudyDao, ResearchStudy>
{
	public static final String RESOURCE_TYPE_NAME = "ResearchStudy";

	public ResearchStudyService(String serverBase, int defaultPageCount, ResearchStudyDao researchStudyDao)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE_NAME, researchStudyDao);
	}
}
