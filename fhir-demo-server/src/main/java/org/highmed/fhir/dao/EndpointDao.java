package org.highmed.fhir.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.search.parameters.EndpointOrganization;
import org.hl7.fhir.r4.model.Endpoint;

import ca.uhn.fhir.context.FhirContext;

public class EndpointDao extends AbstractDomainResourceDao<Endpoint>
{
	public EndpointDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Endpoint.class, "endpoints", "endpoint", "endpoint_id",
				EndpointOrganization::new);
	}

	@Override
	protected Endpoint copy(Endpoint resource)
	{
		return resource.copy();
	}
}
