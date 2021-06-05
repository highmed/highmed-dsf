package org.highmed.dsf.fhir.search.parameters.rev.include;

import java.sql.Connection;

import org.highmed.dsf.fhir.search.IncludeParameterDefinition;
import org.highmed.dsf.fhir.search.IncludeParts;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;

@IncludeParameterDefinition(resourceType = ResearchStudy.class, parameterName = "enrollment", targetResourceTypes = Group.class)
public class ResearchStudyEnrollmentRevInclude extends AbstractRevIncludeParameterFactory
{
	public ResearchStudyEnrollmentRevInclude()
	{
		super("ResearchStudy", "enrollment", "Group");
	}

	@Override
	protected String getRevIncludeSql(IncludeParts includeParts)
	{
		return "(SELECT jsonb_agg(research_study) FROM current_research_studies WHERE research_study->'enrollment' @> concat('[{\"reference\": \"Group/', group_json->>'id', '\"}]')::jsonb) AS research_studies";
	}

	@Override
	protected void modifyIncludeResource(Resource resource, Connection connection)
	{
		// Nothing to do for groups
	}
}
