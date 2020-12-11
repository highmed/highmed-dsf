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

public abstract class AbstractExampleStarter
{
	private final String certificatePath;
	private final char[] certificatePassword;

	protected AbstractExampleStarter()
	{
		this("../../dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12");
	}

	protected AbstractExampleStarter(String certificatePath)
	{
		this(certificatePath, "password");
	}

	protected AbstractExampleStarter(String certificatePath, String certificatePassword)
	{
		this.certificatePath = certificatePath;
		this.certificatePassword = certificatePassword.toCharArray();
	}

	/**
	 * Use this method to start a process at an organization's FHIR endpoint. The resource that will start
	 * the process will be created by calling the method {@link #createStartResource()}
	 *
	 * @param baseUrl the URL of the organization's FHIR endpoint, that should receive the resource
	 * @throws Exception if the client cannot be initialized
	 */
	public final void startAt(String baseUrl) throws Exception
	{
		FhirWebserviceClient client = createClient(baseUrl);
		Resource resource = createStartResource();

		if (resource instanceof Bundle)
		{
			Bundle bundle = (Bundle) resource;
			bundle.getEntry().stream().map(e -> e.getResource().getResourceType()).filter(t -> t == ResourceType.Task)
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Bundle does not contain a Task resource"));

			client.withMinimalReturn().postBundle(bundle);
		}
		else if (resource instanceof Task)
			client.withMinimalReturn().create(resource);
		else
			throw new IllegalArgumentException("Resource should be of type Bundle or Task");
	}

	/**
	 * Creates a FHIR Webservice client based on the supplied baseUrl.
	 *
	 * @param baseUrl the URL of the organization's FHIR endpoint to which the client should connect
	 * @return a {@link FhirWebserviceClient} based to the supplied baseUrl
	 * @throws Exception if the client cannot be initialized
	 */
	protected FhirWebserviceClient createClient(String baseUrl) throws Exception
	{
		KeyStore keyStore = CertificateReader.fromPkcs12(Paths.get(certificatePath), certificatePassword);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		FhirContext context = FhirContext.forR4();
		ReferenceCleaner referenceCleaner = new ReferenceCleanerImpl(new ReferenceExtractorImpl());

		return new FhirWebserviceClientJersey(baseUrl, trustStore, keyStore, certificatePassword, null, null, null, 0,
				0, null, context, referenceCleaner);
	}

	/**
	 * Returns a resource that can start a bpmn process. This should be either a {@link Task}
	 * or a {@link Bundle} containing a {@link Task} resource.
	 * <p>
	 * The task resource should be derived from the base profile
	 * "http://highmed.org/fhir/StructureDefinition/highmed-task-base"
	 *
	 * @return the resource that should be used to start a process
	 */
	protected abstract Resource createStartResource() throws Exception;
}
