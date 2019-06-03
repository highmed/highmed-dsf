package org.highmed.dsf.fhir.client;

import java.util.Optional;

import org.highmed.fhir.client.WebserviceClient;

public interface ClientProvider
{
	Optional<WebserviceClient> getClient(String serverBase);
}
