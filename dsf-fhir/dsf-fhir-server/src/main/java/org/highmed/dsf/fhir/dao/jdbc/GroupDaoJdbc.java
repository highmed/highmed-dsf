package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.dao.GroupDao;
import org.highmed.dsf.fhir.search.parameters.rev.include.ResearchStudyEnrollmentRevInclude;
import org.highmed.dsf.fhir.search.parameters.user.GroupUserFilter;
import org.hl7.fhir.r4.model.Group;

import ca.uhn.fhir.context.FhirContext;

public class GroupDaoJdbc extends AbstractResourceDaoJdbc<Group> implements GroupDao
{
	public GroupDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext, OrganizationType organizationType)
	{
		super(dataSource, fhirContext, Group.class, "groups", "group_json", "group_id", organizationType,
				GroupUserFilter::new, with(), with(ResearchStudyEnrollmentRevInclude::new));
	}

	@Override
	protected Group copy(Group resource)
	{
		return resource.copy();
	}
}
