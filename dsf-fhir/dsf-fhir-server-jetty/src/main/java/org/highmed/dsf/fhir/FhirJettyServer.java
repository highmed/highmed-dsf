package org.highmed.dsf.fhir;

import java.io.IOException;

public class FhirJettyServer
{
	public static void main(String[] args) throws IOException
	{
		FhirServer.startHttpServer();
	}
}
