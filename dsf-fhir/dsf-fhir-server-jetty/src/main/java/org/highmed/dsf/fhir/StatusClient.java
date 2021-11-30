package org.highmed.dsf.fhir;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.highmed.dsf.fhir.webservice.specification.StatusService;

public class StatusClient
{
	public static void main(String[] args)
	{
		try
		{
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://localhost:" + StatusService.PORT + "/fhir/status"))
					.timeout(Duration.ofSeconds(10)).GET().build();

			HttpResponse<Void> response = client.send(request, BodyHandlers.discarding());
			if (response.statusCode() != 200)
			{
				System.err.println("Status service response code: " + response.statusCode());
				System.exit(1);
			}
			else
				System.exit(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
}
