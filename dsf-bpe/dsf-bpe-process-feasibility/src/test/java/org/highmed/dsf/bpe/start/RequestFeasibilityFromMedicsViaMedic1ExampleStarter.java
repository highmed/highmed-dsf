package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.CODE_TYPE_AQL_QUERY;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_QUERY;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_PARTICIPATING_MEDIC;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_PARTICIPATING_TTP;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.PROFILE_HIGHEMD_RESEARCH_STUDY;
import static org.highmed.dsf.bpe.ConstantsBase.PROFILE_HIGHMED_GROUP;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.MEDIC_1_FHIR_BASE_URL;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_2;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_3;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP;

import java.util.Date;
import java.util.UUID;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Group.GroupType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResearchStudy.ResearchStudyStatus;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

public class RequestFeasibilityFromMedicsViaMedic1ExampleStarter
{
	// Environment variable "DSF_CLIENT_CERTIFICATE_PATH" or args[0]: the path to the client-certificate
	//    highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12
	// Environment variable "DSF_CLIENT_CERTIFICATE_PASSWORD" or args[1]: the password of the client-certificate
	//    password
	public static void main(String[] args) throws Exception
	{
		Bundle bundle = createStartResource();
		ExampleStarter.forServer(args, MEDIC_1_FHIR_BASE_URL).startWith(bundle);
	}

	private static Bundle createStartResource()
	{
		Group group1 = createGroup("Group 1");
		Group group2 = createGroup("Group 2");
		ResearchStudy researchStudy = createResearchStudy(group1, group2);
		Task task = createTask(researchStudy);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setResource(group1).setFullUrl(group1.getIdElement().getIdPart()).getRequest()
				.setMethod(HTTPVerb.POST).setUrl(ResourceType.Group.name());
		bundle.addEntry().setResource(group2).setFullUrl(group2.getIdElement().getIdPart()).getRequest()
				.setMethod(HTTPVerb.POST).setUrl(ResourceType.Group.name());
		bundle.addEntry().setResource(researchStudy).setFullUrl(researchStudy.getIdElement().getIdPart()).getRequest()
				.setMethod(HTTPVerb.POST).setUrl(ResourceType.ResearchStudy.name());
		bundle.addEntry().setResource(task).setFullUrl(task.getIdElement().getIdPart()).getRequest()
				.setMethod(HTTPVerb.POST).setUrl(ResourceType.Task.name());

		return bundle;
	}

	private static Group createGroup(String name)
	{
		Group group = new Group();
		group.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		group.getMeta().addProfile(PROFILE_HIGHMED_GROUP);
		group.getText().getDiv().addText("This is the description");
		group.getText().setStatus(Narrative.NarrativeStatus.ADDITIONAL);
		group.setType(GroupType.PERSON);
		group.setActual(false);
		group.setActive(true);
		group.addExtension().setUrl(EXTENSION_HIGHMED_QUERY).setValue(
				new Expression().setLanguageElement(CODE_TYPE_AQL_QUERY).setExpression("SELECT COUNT(e) FROM EHR e"));
		group.setName(name);

		return group;
	}

	private static ResearchStudy createResearchStudy(Group group1, Group group2)
	{
		ResearchStudy researchStudy = new ResearchStudy();
		researchStudy.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		researchStudy.getMeta().addProfile(PROFILE_HIGHEMD_RESEARCH_STUDY);
		researchStudy.addIdentifier().setSystem(NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER)
				.setValue(UUID.randomUUID().toString());
		researchStudy.setStatus(ResearchStudyStatus.ACTIVE);
		researchStudy.addEnrollment().setReference(group1.getIdElement().getIdPart());
		researchStudy.addEnrollment().setReference(group2.getIdElement().getIdPart());

		researchStudy.addExtension().setUrl(EXTENSION_HIGHMED_PARTICIPATING_MEDIC).setValue(
				new Reference().setType(ResourceType.Organization.name()).setIdentifier(
						new Identifier().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
								.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1)));
		researchStudy.addExtension().setUrl(EXTENSION_HIGHMED_PARTICIPATING_MEDIC).setValue(
				new Reference().setType(ResourceType.Organization.name()).setIdentifier(
						new Identifier().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
								.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_2)));
		researchStudy.addExtension().setUrl(EXTENSION_HIGHMED_PARTICIPATING_MEDIC).setValue(
				new Reference().setType(ResourceType.Organization.name()).setIdentifier(
						new Identifier().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
								.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_3)));
		researchStudy.addExtension().setUrl(EXTENSION_HIGHMED_PARTICIPATING_TTP).setValue(
				new Reference().setType(ResourceType.Organization.name()).setIdentifier(
						new Identifier().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
								.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP)));

		return researchStudy;
	}

	private static Task createTask(ResearchStudy researchStudy)
	{
		Task task = new Task();
		task.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new Reference().setReference(researchStudy.getIdElement().getIdPart())
				.setType(ResourceType.ResearchStudy.name())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE);
		task.addInput().setValue(new BooleanType(true)).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE);
		task.addInput().setValue(new BooleanType(true)).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK);

		return task;
	}
}
