package org.highmed.dsf.fhir.client;

import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.IdType;

public interface FhirWebserviceClientProvider
{
	String getLocalBaseUrl();

	FhirWebserviceClient getLocalWebserviceClient();

	FhirWebserviceClient getRemoteWebserviceClient(IdType organizationReference);

	FhirWebserviceClient getRemoteWebserviceClient(String organizationIdentifierSystem, String organizationIdentifierValue);

	FhirWebserviceClient getRemoteWebserviceClient(String webserviceUrl);
}