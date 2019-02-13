package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.HealthcareServiceDao;
import org.hl7.fhir.r4.model.HealthcareService;

@Path(HealthcareServiceService.RESOURCE_TYPE_NAME)
public class HealthcareServiceService extends AbstractService<HealthcareServiceDao, HealthcareService>
{
	public static final String RESOURCE_TYPE_NAME = "HealthcareService";

	public HealthcareServiceService(String serverBase, int defaultPageCount, HealthcareServiceDao healthcareServiceDao)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE_NAME, healthcareServiceDao);
	}
}
