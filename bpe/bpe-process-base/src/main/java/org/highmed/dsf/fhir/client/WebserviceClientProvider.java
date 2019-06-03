package org.highmed.dsf.fhir.client;

import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.IdType;

public interface WebserviceClientProvider
{
	String getLocalBaseUrl();

	WebserviceClient getLocalWebserviceClient();

	WebserviceClient getRemoteWebserviceClient(IdType organizationReference);

	WebserviceClient getRemoteWebserviceClient(String organizationIdentifierSystem, String organizationIdentifierValue);

	WebserviceClient getRemoteWebserviceClient(String webserviceUrl);
}