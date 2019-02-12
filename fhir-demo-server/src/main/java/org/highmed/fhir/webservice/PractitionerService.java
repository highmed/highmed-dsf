package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.PractitionerDao;
import org.hl7.fhir.r4.model.Practitioner;

@Path(PractitionerService.RESOURCE_TYPE_NAME)
public class PractitionerService extends AbstractService<PractitionerDao, Practitioner>
{
	public static final String RESOURCE_TYPE_NAME = "Practitioner";

	public PractitionerService(String serverBase, PractitionerDao practitionerDao)
	{
		super(serverBase, RESOURCE_TYPE_NAME, practitionerDao);
	}
}
