package org.highmed.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.GroupDao;
import org.highmed.dsf.fhir.search.parameters.rev.include.ResearchStudyEnrollmentRevInclude;
import org.highmed.dsf.fhir.search.parameters.user.GroupUserFilter;
import org.hl7.fhir.r4.model.Group;

import ca.uhn.fhir.context.FhirContext;

public class GroupDaoJdbc extends AbstractResourceDaoJdbc<Group> implements GroupDao
{
	public GroupDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, Group.class, "groups", "group_json", "group_id",
				GroupUserFilter::new, with(), with(ResearchStudyEnrollmentRevInclude::new));
	}

	@Override
	protected Group copy(Group resource)
	{
		return resource.copy();
	}
}
