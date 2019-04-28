package org.highmed.bpe.message;

import org.highmed.fhir.client.WebserviceClientProvider;
import org.highmed.fhir.organization.OrganizationProvider;
import org.highmed.fhir.task.AbstractTaskMessageSend;

public class SendPing extends AbstractTaskMessageSend
{
	public SendPing(OrganizationProvider organizationProvider, WebserviceClientProvider clientProvider)
	{
		super(organizationProvider, clientProvider);
	}
}
