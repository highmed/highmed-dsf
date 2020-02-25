package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.NamingSystemDao;
import org.highmed.dsf.fhir.search.parameters.NamingSystemName;
import org.highmed.dsf.fhir.search.parameters.user.NamingSystemUserFilter;
import org.hl7.fhir.r4.model.NamingSystem;

import ca.uhn.fhir.context.FhirContext;

public class NamingSystemDaoJdbc extends AbstractResourceDaoJdbc<NamingSystem> implements NamingSystemDao
{
	public NamingSystemDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, NamingSystem.class, "naming_systems", "naming_system", "naming_system_id",
				NamingSystemUserFilter::new, with(NamingSystemName::new), with());
	}

	@Override
	protected NamingSystem copy(NamingSystem resource)
	{
		return resource.copy();
	}
}
