package org.highmed.dsf.fhir.hapi;

import java.util.Date;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.ActivityDefinition.ActivityDefinitionKind;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class ActivityDefinitionWithExtension
{
	private static final Logger logger = LoggerFactory.getLogger(ActivityDefinitionWithExtension.class);

	@Test
	public void test() throws Exception
	{
		ActivityDefinition a = new ActivityDefinition();
		a.getMeta().addTag("http://highmed.org/fhir/CodeSystem/authorization-role", "REMOTE", null);
		a.setUrl("http://highmed.org/bpe/Process/ping");
		a.setVersion("1.0.0");
		a.setName("PingProcess");
		a.setTitle("PING process");
		a.setSubtitle("Communication Testing Process");
		a.setStatus(PublicationStatus.DRAFT);
		a.setExperimental(true);
		a.setDate(new Date());
		a.setPublisher("HiGHmed");
		a.getContactFirstRep().setName("HiGHmed").getTelecomFirstRep().setSystem(ContactPointSystem.EMAIL)
				.setValue("pmo@highmed.org");
		a.setDescription(
				"Process to send PING messages to remote Organizations and to receive corresponding PONG message");
		a.setKind(ActivityDefinitionKind.TASK);

		Extension e1 = a.addExtension();
		e1.setUrl("http://highmed.org/fhir/StructureDefinition/extension-process-authorization");
		e1.addExtension("message-name", new StringType("startPingProcessMessage"));
		e1.addExtension("authorization-role",
				new Coding("http://highmed.org/fhir/CodeSystem/authorization-role", "LOCAL", null));
		Extension ot12 = e1.addExtension();
		ot12.setUrl("organization-types");
		ot12.addExtension("organization-type",
				new Coding("http://highmed.org/fhir/CodeSystem/authorization-role", "TTP", null));
		ot12.addExtension("organization-type",
				new Coding("http://highmed.org/fhir/CodeSystem/authorization-role", "MeDIC", null));
		e1.addExtension("task-profile",
				new CanonicalType("http://highmed.org/fhir/StructureDefinition/task-start-ping-process"));

		Extension e2 = a.addExtension();
		e2.setUrl("http://highmed.org/fhir/StructureDefinition/extension-process-authorization");
		e2.addExtension("message-name", new StringType("pongMessage"));
		e2.addExtension("authorization-role",
				new Coding("http://highmed.org/fhir/CodeSystem/authorization-role", "REMOTE", null));
		Extension ot22 = e2.addExtension();
		ot22.setUrl("organization-types");
		ot22.addExtension("organization-type",
				new Coding("http://highmed.org/fhir/CodeSystem/authorization-role", "TTP", null));
		ot22.addExtension("organization-type",
				new Coding("http://highmed.org/fhir/CodeSystem/authorization-role", "MeDIC", null));
		e2.addExtension("task-profile", new CanonicalType("http://highmed.org/fhir/StructureDefinition/task-pong"));

		String xml = FhirContext.forR4().newXmlParser().setPrettyPrint(true).encodeResourceToString(a);
		logger.debug(xml);
	}
}
