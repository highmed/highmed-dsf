package org.highmed.dsf.fhir.client;

import org.highmed.fhir.client.FhirWebserviceClient;

public interface FhirWebserviceClientProvider
{
	String getLocalBaseUrl();

	FhirWebserviceClient getLocalWebserviceClient();

	FhirWebserviceClient getWebserviceClient(String webserviceUrl);
}