package org.highmed.bpe.message;

import org.highmed.fhir.client.ClientProvider;
import org.highmed.fhir.organization.OrganizationProvider;
import org.highmed.fhir.task.AbstractTaskMessageSend;

public class SendPong extends AbstractTaskMessageSend
{
	public SendPong(OrganizationProvider organizationProvider, ClientProvider clientProvider)
	{
		super(organizationProvider, clientProvider);
	}
}
