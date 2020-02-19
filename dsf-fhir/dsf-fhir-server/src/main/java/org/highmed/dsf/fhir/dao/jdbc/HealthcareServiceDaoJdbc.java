package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.dao.HealthcareServiceDao;
import org.highmed.dsf.fhir.search.parameters.HealthcareServiceActive;
import org.highmed.dsf.fhir.search.parameters.HealthcareServiceIdentifier;
import org.highmed.dsf.fhir.search.parameters.user.HealthcareServiceUserFilter;
import org.hl7.fhir.r4.model.HealthcareService;

import ca.uhn.fhir.context.FhirContext;

public class HealthcareServiceDaoJdbc extends AbstractResourceDaoJdbc<HealthcareService> implements HealthcareServiceDao
{
	public HealthcareServiceDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext,
			OrganizationType organizationType)
	{
		super(dataSource, fhirContext, HealthcareService.class, "healthcare_services", "healthcare_service",
				"healthcare_service_id", organizationType, HealthcareServiceUserFilter::new,
				with(HealthcareServiceIdentifier::new, HealthcareServiceActive::new), with());
	}

	@Override
	protected HealthcareService copy(HealthcareService resource)
	{
		return resource.copy();
	}
}
