package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.PractitionerRoleDao;
import org.highmed.dsf.fhir.search.parameters.PractitionerRoleActive;
import org.highmed.dsf.fhir.search.parameters.PractitionerRoleIdentifier;
import org.hl7.fhir.r4.model.PractitionerRole;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerRoleDaoJdbc extends AbstractResourceDaoJdbc<PractitionerRole>
		implements PractitionerRoleDao
{
	public PractitionerRoleDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, PractitionerRole.class, "practitioner_roles", "practitioner_role",
				"practitioner_role_id", PractitionerRoleIdentifier::new, PractitionerRoleActive::new);
	}

	@Override
	protected PractitionerRole copy(PractitionerRole resource)
	{
		return resource.copy();
	}
}
