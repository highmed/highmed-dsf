package org.highmed.fhir.webservice.secure;

import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.webservice.specification.ResearchStudyService;
import org.hl7.fhir.r4.model.ResearchStudy;

public class ResearchStudyServiceSecure extends AbstractServiceSecure<ResearchStudy, ResearchStudyService>
		implements ResearchStudyService
{
	public ResearchStudyServiceSecure(ResearchStudyService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
