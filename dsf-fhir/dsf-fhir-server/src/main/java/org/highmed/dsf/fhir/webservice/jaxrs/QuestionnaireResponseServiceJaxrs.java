package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.QuestionnaireResponseService;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

@Path(QuestionnaireResponseServiceJaxrs.PATH)
public class QuestionnaireResponseServiceJaxrs
		extends AbstractResourceServiceJaxrs<QuestionnaireResponse, QuestionnaireResponseService>
		implements QuestionnaireResponseService
{
	public static final String PATH = "QuestionnaireResponse";

	public QuestionnaireResponseServiceJaxrs(QuestionnaireResponseService delegate)
	{
		super(delegate);
	}
}
