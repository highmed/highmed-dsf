package org.highmed.fhir.dao;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.search.PartialResult;
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

	public PartialResult<ResearchStudy> search(int page, int count) throws SQLException
	{
		return search(createSearchQueryFactory(page, count).build());
	}
}
