package org.highmed.fhir.client;

import java.util.Optional;

public interface ClientProvider
{
	Optional<WebserviceClient> getClient(String serverBase);
}
