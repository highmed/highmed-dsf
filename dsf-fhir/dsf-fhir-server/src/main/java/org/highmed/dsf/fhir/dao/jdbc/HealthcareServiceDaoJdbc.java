package org.highmed.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.HealthcareServiceDao;
import org.highmed.dsf.fhir.search.parameters.HealthcareServiceActive;
import org.highmed.dsf.fhir.search.parameters.HealthcareServiceIdentifier;
import org.highmed.dsf.fhir.search.parameters.user.HealthcareServiceUserFilter;
import org.hl7.fhir.r4.model.HealthcareService;

import ca.uhn.fhir.context.FhirContext;

public class HealthcareServiceDaoJdbc extends AbstractResourceDaoJdbc<HealthcareService> implements HealthcareServiceDao
{
	public HealthcareServiceDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource,
			FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, HealthcareService.class, "healthcare_services",
				"healthcare_service", "healthcare_service_id", HealthcareServiceUserFilter::new,
				with(HealthcareServiceActive::new, HealthcareServiceIdentifier::new), with());
	}

	@Override
	protected HealthcareService copy(HealthcareService resource)
	{
		return resource.copy();
	}
}
