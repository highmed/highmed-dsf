package org.highmed.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.QuestionnaireResponseDao;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireResponseAuthored;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireResponseIdentifier;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireResponseQuestionnaire;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireResponseStatus;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireResponseSubject;
import org.highmed.dsf.fhir.search.parameters.user.QuestionnaireResponseUserFilter;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

import ca.uhn.fhir.context.FhirContext;

public class QuestionnaireResponseDaoJdbc extends AbstractResourceDaoJdbc<QuestionnaireResponse>
		implements QuestionnaireResponseDao
{
	public QuestionnaireResponseDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource,
			FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, QuestionnaireResponse.class,
				"questionnaire_responses", "questionnaire_response", "questionnaire_response_id",
				QuestionnaireResponseUserFilter::new,
				with(QuestionnaireResponseAuthored::new, QuestionnaireResponseIdentifier::new,
						QuestionnaireResponseQuestionnaire::new, QuestionnaireResponseStatus::new,
						QuestionnaireResponseSubject::new),
				with());
	}

	@Override
	protected QuestionnaireResponse copy(QuestionnaireResponse resource)
	{
		return resource.copy();
	}
}
