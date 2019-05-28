package org.highmed.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.ResearchStudyDao;
import org.highmed.fhir.search.parameters.ResearchStudyIdentifier;
import org.hl7.fhir.r4.model.ResearchStudy;

import ca.uhn.fhir.context.FhirContext;

public class ResearchStudyDaoJdbc extends AbstractDomainResourceDaoJdbc<ResearchStudy> implements ResearchStudyDao
{
	public ResearchStudyDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, ResearchStudy.class, "research_studies", "research_study", "research_study_id",
				ResearchStudyIdentifier::new);
	}

	@Override
	protected ResearchStudy copy(ResearchStudy resource)
	{
		return resource.copy();
	}
}
