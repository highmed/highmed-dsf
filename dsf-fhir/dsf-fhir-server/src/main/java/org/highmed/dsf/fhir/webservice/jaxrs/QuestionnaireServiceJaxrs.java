package org.highmed.dsf.fhir.webservice.jaxrs;

import jakarta.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.QuestionnaireService;
import org.hl7.fhir.r4.model.Questionnaire;

@Path(QuestionnaireServiceJaxrs.PATH)
public class QuestionnaireServiceJaxrs extends AbstractResourceServiceJaxrs<Questionnaire, QuestionnaireService>
		implements QuestionnaireService
{
	public static final String PATH = "Questionnaire";

	public QuestionnaireServiceJaxrs(QuestionnaireService delegate)
	{
		super(delegate);
	}
}
