package org.highmed.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.highmed.dsf.fhir.search.parameters.ResearchStudyEnrollment;
import org.highmed.dsf.fhir.search.parameters.ResearchStudyIdentifier;
import org.highmed.dsf.fhir.search.parameters.ResearchStudyPrincipalInvestigator;
import org.highmed.dsf.fhir.search.parameters.user.ResearchStudyUserFilter;
import org.hl7.fhir.r4.model.ResearchStudy;

import ca.uhn.fhir.context.FhirContext;

public class ResearchStudyDaoJdbc extends AbstractResourceDaoJdbc<ResearchStudy> implements ResearchStudyDao
{
	public ResearchStudyDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, ResearchStudy.class, "research_studies",
				"research_study", "research_study_id", ResearchStudyUserFilter::new, with(ResearchStudyEnrollment::new,
						ResearchStudyIdentifier::new, ResearchStudyPrincipalInvestigator::new),
				with());
	}

	@Override
	protected ResearchStudy copy(ResearchStudy resource)
	{
		return resource.copy();
	}
}
