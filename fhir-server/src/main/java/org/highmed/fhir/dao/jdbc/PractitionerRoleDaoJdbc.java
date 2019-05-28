package org.highmed.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.PractitionerRoleDao;
import org.highmed.fhir.search.parameters.PractitionerRoleActive;
import org.highmed.fhir.search.parameters.PractitionerRoleIdentifier;
import org.hl7.fhir.r4.model.PractitionerRole;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerRoleDaoJdbc extends AbstractDomainResourceDaoJdbc<PractitionerRole>
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
