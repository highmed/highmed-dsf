package org.highmed.fhir.client;

public interface FhirWebserviceClient extends BasicFhirWebserviceClient, RetryClient<BasicFhirWebserviceClient>
{
	String getBaseUrl();

	PreferReturnOutcomeWithRetry withOperationOutcomeReturn();

	PreferReturnMinimalWithRetry withMinimalReturn();
}
