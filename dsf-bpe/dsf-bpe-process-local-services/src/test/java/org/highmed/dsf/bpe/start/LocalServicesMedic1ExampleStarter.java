package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_QUERY_TYPE;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_QUERY_TYPE_AQL;
import static org.highmed.dsf.bpe.ConstantsBase.ORGANIZATION_IDENTIFIER_SYSTEM;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.CERTIFICATE_PASSWORD;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.CERTIFICATE_PATH;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.MEDIC_1_FHIR_BASE_URL;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.variables.ConstantsLocalServices.LOCAL_SERVICES_MESSAGE_NAME;
import static org.highmed.dsf.bpe.variables.ConstantsLocalServices.LOCAL_SERVICES_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.variables.ConstantsLocalServices.LOCAL_SERVICES_TASK_PROFILE;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.ws.rs.WebApplicationException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.highmed.dsf.bpe.variables.BloomFilterConfig;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceCleanerImpl;
import org.highmed.dsf.fhir.service.ReferenceExtractorImpl;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

public class LocalServicesMedic1ExampleStarter
{
	public static void main(String[] args)
			throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException
	{
		KeyStore keyStore = CertificateReader.fromPkcs12(Paths.get(CERTIFICATE_PATH), CERTIFICATE_PASSWORD);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		FhirContext context = FhirContext.forR4();
		ReferenceCleaner referenceCleaner = new ReferenceCleanerImpl(new ReferenceExtractorImpl());
		FhirWebserviceClient client = new FhirWebserviceClientJersey(MEDIC_1_FHIR_BASE_URL, trustStore, keyStore,
				CERTIFICATE_PASSWORD, null, null, null, 0, 0, null, context, referenceCleaner);

		try
		{
			Task task = createTask(true, true);
			client.withMinimalReturn().create(task);
		}
		catch (WebApplicationException e)
		{
			if (e.getResponse() != null && e.getResponse().hasEntity())
			{
				OperationOutcome outcome = e.getResponse().readEntity(OperationOutcome.class);
				String xml = context.newXmlParser().setPrettyPrint(true).encodeResourceToString(outcome);
				System.out.println(xml);
			}
			else
				e.printStackTrace();
		}
	}

	private static Task createTask(boolean needsConsentCheck, boolean needsRecordLinkage)
			throws NoSuchAlgorithmException
	{
		Task task = new Task();

		task.getMeta().addProfile(LOCAL_SERVICES_TASK_PROFILE);
		task.setInstantiatesUri(LOCAL_SERVICES_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setIntent(Task.TaskIntent.ORDER);
		task.setAuthoredOn(new Date());

		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue(ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue(ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);

		task.addInput().setValue(new StringType(LOCAL_SERVICES_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);

		task.addInput().setValue(new StringType("SELECT COUNT(e) FROM EHR e;")).getType().addCoding()
				.setSystem(CODESYSTEM_QUERY_TYPE).setCode(CODESYSTEM_QUERY_TYPE_AQL);
		task.addInput().setValue(new BooleanType(needsConsentCheck)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK);
		task.addInput().setValue(new BooleanType(needsRecordLinkage)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE);

		if (needsRecordLinkage)
		{
			BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
			BloomFilterConfig bloomFilterConfig = new BloomFilterConfig(new Random().nextLong(),
					KeyGenerator.getInstance("HmacSHA256", bouncyCastleProvider).generateKey(),
					KeyGenerator.getInstance("HmacSHA3-256", bouncyCastleProvider).generateKey());

			task.addInput().setValue(new Base64BinaryType(bloomFilterConfig.toBytes())).getType().addCoding()
					.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
					.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG);
		}

		return task;
	}
}
