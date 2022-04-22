package org.highmed.dsf.fhir.client;

import java.util.Optional;

import org.highmed.fhir.client.FhirWebserviceClient;

public interface ClientProvider
{
	Optional<FhirWebserviceClient> getClient(String serverBase);

	boolean endpointExists(String serverBase);
}
