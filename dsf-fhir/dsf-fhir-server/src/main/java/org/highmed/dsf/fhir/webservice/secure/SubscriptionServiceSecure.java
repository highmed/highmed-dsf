package org.highmed.dsf.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.SubscriptionService;
import org.hl7.fhir.r4.model.Subscription;

public class SubscriptionServiceSecure extends AbstractServiceSecure<Subscription, SubscriptionService>
		implements SubscriptionService
{
	public SubscriptionServiceSecure(SubscriptionService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}

	@Override
	public Response create(Subscription resource, UriInfo uri, HttpHeaders headers)
	{
		// check subscription.channel.payload null or one of the supported mimetypes
		// check subscription.channel.type = websocket
		// check subscription.criteria is implemented as search query
		// check if subscription.channel.type = websocket, Task unique on subscription.criteria

		// TODO Auto-generated method stub
		return super.create(resource, uri, headers);
	}

	@Override
	public Response update(String id, Subscription resource, UriInfo uri, HttpHeaders headers)
	{
		// see create

		// TODO Auto-generated method stub
		return super.update(id, resource, uri, headers);
	}

	@Override
	public Response update(Subscription resource, UriInfo uri, HttpHeaders headers)
	{
		// see create

		// TODO Auto-generated method stub
		return super.update(resource, uri, headers);
	}
}
