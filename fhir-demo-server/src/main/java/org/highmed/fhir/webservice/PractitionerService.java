package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.PractitionerDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.service.ResourceValidator;
import org.hl7.fhir.r4.model.Practitioner;

@Path(PractitionerService.RESOURCE_TYPE_NAME)
public class PractitionerService extends AbstractService<PractitionerDao, Practitioner>
{
	public static final String RESOURCE_TYPE_NAME = "Practitioner";

	public PractitionerService(String serverBase, int defaultPageCount, PractitionerDao practitionerDao,
			ResourceValidator validator, EventManager eventManager)
	{
		super(serverBase, defaultPageCount, Practitioner.class, practitionerDao, validator, eventManager);
	}
}
