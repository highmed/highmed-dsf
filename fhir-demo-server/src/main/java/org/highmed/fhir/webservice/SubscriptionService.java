package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.BasicCrudDao;
import org.hl7.fhir.r4.model.Subscription;

@Path(SubscriptionService.RESOURCE_TYPE)
public class SubscriptionService extends AbstractService<Subscription>
{
	public static final String RESOURCE_TYPE = "Subscription";

	public SubscriptionService(String serverBase, BasicCrudDao<Subscription> crudDao)
	{
		super(serverBase, RESOURCE_TYPE, crudDao);
	}
}
