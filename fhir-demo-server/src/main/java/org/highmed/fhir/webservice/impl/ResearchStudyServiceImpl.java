package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.ResearchStudyDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.ResearchStudyService;
import org.hl7.fhir.r4.model.ResearchStudy;

public class ResearchStudyServiceImpl extends AbstractServiceImpl<ResearchStudyDao, ResearchStudy>
		implements ResearchStudyService
{
	public ResearchStudyServiceImpl(String serverBase, int defaultPageCount, ResearchStudyDao researchStudyDao,
			ResourceValidator validator, EventManager eventManager, ServiceHelperImpl<ResearchStudy> serviceHelper)
	{
		super(serverBase, defaultPageCount, researchStudyDao, validator, eventManager, serviceHelper);
	}
}
