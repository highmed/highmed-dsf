package org.highmed.fhir.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.ResearchStudy;

import ca.uhn.fhir.context.FhirContext;

public class ResearchStudyDao extends AbstractDao<ResearchStudy>
{
	public ResearchStudyDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, ResearchStudy.class, "research_studies", "research_study", "research_study_id");
	}

	@Override
	protected ResearchStudy copy(ResearchStudy resource)
	{
		return resource.copy();
	}
}
