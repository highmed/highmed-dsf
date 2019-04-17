package org.highmed.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.EndpointDao;
import org.highmed.fhir.search.parameters.EndpointIdentifier;
import org.highmed.fhir.search.parameters.EndpointName;
import org.highmed.fhir.search.parameters.EndpointOrganization;
import org.hl7.fhir.r4.model.Endpoint;

import ca.uhn.fhir.context.FhirContext;

public class EndpointDaoJdbc extends AbstractDomainResourceDaoJdbc<Endpoint> implements EndpointDao
{
	public EndpointDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Endpoint.class, "endpoints", "endpoint", "endpoint_id",
				EndpointOrganization::new, EndpointIdentifier::new, EndpointName::new);
	}

	@Override
	protected Endpoint copy(Endpoint resource)
	{
		return resource.copy();
	}
}
