package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.ORGANIZATION_IDENTIFIER_SYSTEM;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.REQUEST_UPDATE_RESOURCES_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.REQUEST_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.REQUEST_UPDATE_RESOURCES_TASK_PROFILE;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.ORGANIZATION_IDENTIFIER_VALUE_TTP;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.TTP_FHIR_BASE_URL;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

public class UpdateResource3MedicTtpExampleStarter
{
	// Environment variable "DSF_CLIENT_CERTIFICATE_PATH" or args[0]: the path to the client-certificate
	//    highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12
	// Environment variable "DSF_CLIENT_CERTIFICATE_PASSWORD" or args[1]: the password of the client-certificate
	//    password
	public static void main(String[] args) throws Exception
	{
		ExampleStarter starter = ExampleStarter.forServer(args, TTP_FHIR_BASE_URL);
		Task task = createStartResource(starter);
		starter.startWith(task);
	}

	private static Task createStartResource(ExampleStarter starter) throws Exception
	{
		Bundle allowList = getAllowList(starter);

		Task task = new Task();
		task.getMeta().addProfile(REQUEST_UPDATE_RESOURCES_TASK_PROFILE);
		task.setInstantiatesUri(REQUEST_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION);
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

		return task;
	}

	private static Bundle getAllowList(ExampleStarter starter) throws Exception
	{
		FhirWebserviceClient client = starter.createClient(TTP_FHIR_BASE_URL);
		Bundle searchResult = client.searchWithStrictHandling(Bundle.class, Map.of("identifier",
				Collections.singletonList("http://highmed.org/fhir/CodeSystem/update-allow-list|highmed_allow_list")));

		if (searchResult.getTotal() != 1 && searchResult.getEntryFirstRep().getResource() instanceof Bundle)
			throw new IllegalStateException("Expected a single allow list Bundle");

		return (Bundle) searchResult.getEntryFirstRep().getResource();
	}
}
