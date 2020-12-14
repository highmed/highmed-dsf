package org.highmed.dsf.bpe.start;

import java.nio.file.Paths;
import java.security.KeyStore;

import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceCleanerImpl;
import org.highmed.dsf.fhir.service.ReferenceExtractorImpl;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

public class ExampleStarter
{
	public static ExampleStarter forServer(String[] args, String baseUrl)
	{
		if (args.length != 2)
			throw new IllegalArgumentException(
					"Expects 2 args: 1. certificate path, 2. certificate password, found " + args.length);

		return ExampleStarter.forServer(args[0], args[1], baseUrl);
	}

	public static ExampleStarter forServer(String certificatePath, String certificatePassword, String baseUrl)
	{
		return new ExampleStarter(certificatePath, certificatePassword, baseUrl);
	}

	private final String certificatePath;
	private final char[] certificatePassword;
	private final String baseUrl;

	private ExampleStarter(String certificatePath, String certificatePassword, String baseUrl)
	{
		this.certificatePath = certificatePath;
		this.certificatePassword = certificatePassword.toCharArray();
		this.baseUrl = baseUrl;
	}

	public void startWith(Task task) throws Exception
	{
		start(task);
	}

	public void startWith(Bundle bundle) throws Exception
	{
		start(bundle);
	}

	private void start(Resource resource) throws Exception
	{
		FhirWebserviceClient client = createClient(baseUrl);

		if (resource instanceof Bundle)
		{
			Bundle bundle = (Bundle) resource;
			bundle.getEntry().stream().map(e -> e.getResource().getResourceType()).filter(ResourceType.Task::equals)
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Bundle does not contain a Task resource"));

			client.withMinimalReturn().postBundle(bundle);
		}
		else if (resource instanceof Task)
			client.withMinimalReturn().create(resource);
		else
			throw new IllegalArgumentException("Resource should be of type Bundle or Task");
	}

	public FhirWebserviceClient createClient(String baseUrl) throws Exception
	{
		KeyStore keyStore = CertificateReader.fromPkcs12(Paths.get(certificatePath), certificatePassword);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		FhirContext context = FhirContext.forR4();
		ReferenceCleaner referenceCleaner = new ReferenceCleanerImpl(new ReferenceExtractorImpl());

		return new FhirWebserviceClientJersey(baseUrl, trustStore, keyStore, certificatePassword, null, null, null, 0,
				0, null, context, referenceCleaner);
	}
}
