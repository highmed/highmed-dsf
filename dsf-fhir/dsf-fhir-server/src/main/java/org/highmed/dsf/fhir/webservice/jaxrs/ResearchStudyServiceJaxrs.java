package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.ResearchStudyService;
import org.hl7.fhir.r4.model.ResearchStudy;

@Path(ResearchStudyServiceJaxrs.PATH)
public class ResearchStudyServiceJaxrs extends AbstractResourceServiceJaxrs<ResearchStudy, ResearchStudyService>
		implements ResearchStudyService
{
	public static final String PATH = "ResearchStudy";

	public ResearchStudyServiceJaxrs(ResearchStudyService delegate)
	{
		super(delegate);
	}
}
