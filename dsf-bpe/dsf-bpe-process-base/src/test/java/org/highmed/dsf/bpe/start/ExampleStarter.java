package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.ENV_DSF_CLIENT_CERTIFICATE_PASSWORD;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.ENV_DSF_CLIENT_CERTIFICATE_PATH;

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
	/**
	 * Creates an object to send start-process-messages to a given FHIR-Endpoint baseUrl based on the provided
	 * client-certificate path and client-certificate password.
	 *
	 * The client-certificate path is first read from the environment variable
	 * {@link ConstantsExampleStarters#ENV_DSF_CLIENT_CERTIFICATE_PATH}. If args[0] is set, the environment variable is
	 * overwritten by args[0].
	 *
	 * The client-certificate password is first read from the environment variable
	 * {@link ConstantsExampleStarters#ENV_DSF_CLIENT_CERTIFICATE_PASSWORD}. If args[1] is set, the environment variable
	 * is overwritten by args[1].
	 *
	 * @param args
	 *            client-certificate arguments: args[0] can be the path of the client-certificate args[1] can be the
	 *            password of the client-certificate
	 * @param baseUrl
	 *            the baseUrl of the organization's FHIR-Endpoint
	 * @return initialized ExampleStarter instance
	 */
	public static ExampleStarter forServer(String[] args, String baseUrl)
	{
		String certificatePath = System.getenv(ENV_DSF_CLIENT_CERTIFICATE_PATH);
		String certificatePassword = System.getenv(ENV_DSF_CLIENT_CERTIFICATE_PASSWORD);

		if (args.length > 0 && !args[0].isBlank())
			certificatePath = args[0];

		if (args.length > 1 && !args[1].isBlank())
			certificatePassword = args[1];

		return ExampleStarter.forServer(certificatePath, certificatePassword, baseUrl);
	}

	/**
	 * Creates an object to send start-process-messages to a given FHIR-Endpoint baseUrl based on the provided
	 * client-certificate path and client-certificate password.
	 *
	 * @param certificatePath
	 *            the path of the client-certificate
	 * @param certificatePassword
	 *            the password of the client-certificate
	 * @param baseUrl
	 *            the baseUrl of the organization's FHIR-Endpoint
	 * @return initialized ExampleStarter instance
	 */
	public static ExampleStarter forServer(String certificatePath, String certificatePassword, String baseUrl)
	{
		if (certificatePath == null || certificatePath.isBlank())
			throw new IllegalArgumentException("certificatePath null or blank");

		if (certificatePassword == null || certificatePassword.isBlank())
			throw new IllegalArgumentException("certificatePassword null or blank");

		if (baseUrl == null || baseUrl.isBlank())
			throw new IllegalArgumentException("baseUrl null or blank");

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
				0, false, null, context, referenceCleaner);
	}
}
