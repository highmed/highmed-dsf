package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.ORGANIZATION_IDENTIFIER_SYSTEM;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.CERTIFICATE_PASSWORD;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.CERTIFICATE_PATH;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.ORGANIZATION_IDENTIFIER_VALUE_TTP;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.TTP_FHIR_BASE_URL;
import static org.highmed.dsf.bpe.variables.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE;
import static org.highmed.dsf.bpe.variables.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE;
import static org.highmed.dsf.bpe.variables.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER;
import static org.highmed.dsf.bpe.variables.ConstantsUpdateResources.REQUEST_UPDATE_RESOURCES_MESSAGE_NAME;
import static org.highmed.dsf.bpe.variables.ConstantsUpdateResources.REQUEST_UPDATE_RESOURCES_PROCESS_LATEST_VERSION;
import static org.highmed.dsf.bpe.variables.ConstantsUpdateResources.REQUEST_UPDATE_RESOURCES_PROCESS_URI;
import static org.highmed.dsf.bpe.variables.ConstantsUpdateResources.REQUEST_UPDATE_RESOURCES_TASK_PROFILE;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceCleanerImpl;
import org.highmed.dsf.fhir.service.ReferenceExtractorImpl;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

import ca.uhn.fhir.context.FhirContext;

import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

public class UpdateResource3MedicTtpExampleStarter
{
	public static void main(String[] args)
			throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
	{
		KeyStore keyStore = CertificateReader.fromPkcs12(Paths.get(CERTIFICATE_PATH), CERTIFICATE_PASSWORD);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		FhirContext context = FhirContext.forR4();
		ReferenceCleaner referenceCleaner = new ReferenceCleanerImpl(new ReferenceExtractorImpl());
		FhirWebserviceClient client = new FhirWebserviceClientJersey(TTP_FHIR_BASE_URL, trustStore, keyStore,
				CERTIFICATE_PASSWORD, null, null, null, 0, 0, null, context, referenceCleaner);

		Bundle searchResult = client.searchWithStrictHandling(Bundle.class, Map.of("identifier",
				Collections.singletonList("http://highmed.org/fhir/CodeSystem/update-allow-list|highmed_allow_list")));

		if (searchResult.getTotal() != 1 && searchResult.getEntryFirstRep().getResource() instanceof Bundle)
			throw new IllegalStateException("Expected a single allow list Bundle");
		Bundle allowList = (Bundle) searchResult.getEntryFirstRep().getResource();

		System.out.println(context.newXmlParser().encodeResourceToString(allowList));

		Task task = new Task();
		task.getMeta().addProfile(REQUEST_UPDATE_RESOURCES_TASK_PROFILE);
		task.setInstantiatesUri(
				REQUEST_UPDATE_RESOURCES_PROCESS_URI + "/" + REQUEST_UPDATE_RESOURCES_PROCESS_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue(ORGANIZATION_IDENTIFIER_VALUE_TTP);
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue(ORGANIZATION_IDENTIFIER_VALUE_TTP);

		task.addInput().setValue(new StringType(REQUEST_UPDATE_RESOURCES_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);

		task.addInput().setValue(new Reference(
				new IdType(ResourceType.Bundle.name(), allowList.getIdElement().getIdPart(),
						allowList.getIdElement().getVersionIdPart()))).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_UPDATE_RESOURCE)
				.setCode(CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE);

		task.addInput().setValue(new StringType("http://highmed.org/fhir/NamingSystem/organization-identifier|"))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_UPDATE_RESOURCE)
				.setCode(CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER);

		client.withMinimalReturn().create(task);
	}
}
