package org.highmed.dsf.bpe.message;

import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;

public class SendPing extends AbstractTaskMessageSend
{
	public SendPing(OrganizationProvider organizationProvider, WebserviceClientProvider clientProvider)
	{
		super(organizationProvider, clientProvider);
	}
}
