package org.highmed.fhir.dao;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.r4.model.HealthcareService;

import ca.uhn.fhir.context.FhirContext;

public class HealthcareServiceDao extends AbstractDao<HealthcareService>
{
	public HealthcareServiceDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, HealthcareService.class, "healthcare_services", "healthcare_service",
				"healthcare_service_id");
	}

	@Override
	protected HealthcareService copy(HealthcareService resource)
	{
		return resource.copy();
	}

	public PartialResult<HealthcareService> search(int page, int count) throws SQLException
	{
		return search(createSearchQueryFactory(page, count).build());
	}
}
