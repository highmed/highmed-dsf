package org.highmed.bpe.message;

import org.highmed.fhir.client.WebserviceClientProvider;
import org.highmed.fhir.organization.OrganizationProvider;
import org.highmed.fhir.task.AbstractTaskMessageSend;

public class SendRequest extends AbstractTaskMessageSend
{
	public SendRequest(OrganizationProvider organizationProvider, WebserviceClientProvider clientProvider)
	{
		super(organizationProvider, clientProvider);
	}
}
