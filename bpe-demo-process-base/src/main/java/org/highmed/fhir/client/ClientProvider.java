package org.highmed.fhir.client;

import org.hl7.fhir.r4.model.IdType;

public interface ClientProvider
{
	WebserviceClient getLocalClient();

	WebserviceClient getRemoteClient(String baseUrl);

	WebserviceClient getRemoteClient(IdType organizationReference);
}