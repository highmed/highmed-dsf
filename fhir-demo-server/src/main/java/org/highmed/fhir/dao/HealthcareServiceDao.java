package org.highmed.fhir.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.search.parameters.HealthcareServiceActive;
import org.highmed.fhir.search.parameters.HealthcareServiceIdentifier;
import org.hl7.fhir.r4.model.HealthcareService;

import ca.uhn.fhir.context.FhirContext;

public class HealthcareServiceDao extends AbstractDomainResourceDao<HealthcareService>
{
	public HealthcareServiceDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, HealthcareService.class, "healthcare_services", "healthcare_service",
				"healthcare_service_id", HealthcareServiceIdentifier::new, HealthcareServiceActive::new);
	}

	@Override
	protected HealthcareService copy(HealthcareService resource)
	{
		return resource.copy();
	}
}
