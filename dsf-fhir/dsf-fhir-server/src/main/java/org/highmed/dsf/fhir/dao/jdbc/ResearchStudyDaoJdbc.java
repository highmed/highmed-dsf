package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.highmed.dsf.fhir.search.parameters.ResearchStudyEnrollment;
import org.highmed.dsf.fhir.search.parameters.ResearchStudyIdentifier;
import org.highmed.dsf.fhir.search.parameters.user.ResearchStudyUserFilter;
import org.hl7.fhir.r4.model.ResearchStudy;

import ca.uhn.fhir.context.FhirContext;

public class ResearchStudyDaoJdbc extends AbstractResourceDaoJdbc<ResearchStudy> implements ResearchStudyDao
{
	public ResearchStudyDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext, OrganizationType organizationType)
	{
		super(dataSource, fhirContext, ResearchStudy.class, "research_studies", "research_study", "research_study_id",
				organizationType, ResearchStudyUserFilter::new,
				with(ResearchStudyIdentifier::new, ResearchStudyEnrollment::new), with());
	}

	@Override
	protected ResearchStudy copy(ResearchStudy resource)
	{
		return resource.copy();
	}
}
