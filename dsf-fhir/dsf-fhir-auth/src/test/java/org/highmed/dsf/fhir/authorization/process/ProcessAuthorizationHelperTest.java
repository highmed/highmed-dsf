package org.highmed.dsf.fhir.authorization.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.authentication.User;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.ActivityDefinition.ActivityDefinitionKind;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;

public class ProcessAuthorizationHelperTest
{
	private final ProcessAuthorizationHelper helper = new ProcessAuthorizationHelperImpl();

	private ActivityDefinition createActivityDefinition()
	{
		var ad = new ActivityDefinition();
		ad.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/activity-definition");
		ad.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");
		ad.setUrl("http://highmed.org/bpe/Process/test");
		ad.setVersion("0.5.0");
		ad.setStatus(PublicationStatus.ACTIVE);
		ad.setKind(ActivityDefinitionKind.TASK);

		return ad;
	}

	@Test
	public void testActivityDefinitionNotValidWithProcessAuthorizationsSameMessageName() throws Exception
	{
		var ad = createActivityDefinition();

		Extension pa1 = ad.addExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-process-authorization");
		pa1.addExtension("message-name", new StringType("foo"));
		pa1.addExtension("task-profile", new CanonicalType("http://bar.org/fhir/StructureDefinition/baz"));
		pa1.addExtension("requester",
				new Coding("http://highmed.org/fhir/CodeSystem/process-authorization", "REMOTE_ALL", null));
		pa1.addExtension("recipient",
				new Coding("http://highmed.org/fhir/CodeSystem/process-authorization", "LOCAL_ALL", null));
		Extension pa2 = ad.addExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-process-authorization");
		pa2.addExtension("message-name", new StringType("foo"));
		pa2.addExtension("task-profile", new CanonicalType("http://bar.org/fhir/StructureDefinition/baz"));
		pa2.addExtension("requester",
				new Coding("http://highmed.org/fhir/CodeSystem/process-authorization", "REMOTE_ALL", null));
		pa2.addExtension("recipient",
				new Coding("http://highmed.org/fhir/CodeSystem/process-authorization", "LOCAL_ALL", null));

		assertFalse(helper.isValid(ad, p -> true, o -> true, c -> true));
	}

	@Test
	public void testActivityDefinitionRequesterRemoteAllRecipientLocalAllValidViaFile() throws Exception
	{
		try (InputStream in = Files.newInputStream(
				Paths.get("src/test/resources/authorization/process-authorization/req_remote_all_rec_local_all.xml")))
		{
			var ad = FhirContext.forR4().newXmlParser().parseResource(ActivityDefinition.class, in);

			assertTrue(helper.isValid(ad, p -> true, o -> true, c -> true));
		}
	}

	@Test
	public void testActivityDefinitionRequesterRemoteOrganizationRecipientLocalConsortiumRoleValidViaFile()
			throws Exception
	{
		try (InputStream in = Files.newInputStream(Paths.get(
				"src/test/resources/authorization/process-authorization/req_remote_organization_rec_local_role.xml")))
		{
			var ad = FhirContext.forR4().newXmlParser().parseResource(ActivityDefinition.class, in);

			assertTrue(helper.isValid(ad, c -> true, o -> true, c -> true));
		}
	}

	@Test
	public void testGetRequesterRemoteAllRecipientLocalAllViaFile() throws Exception
	{
		try (InputStream in = Files.newInputStream(
				Paths.get("src/test/resources/authorization/process-authorization/req_remote_all_rec_local_all.xml")))
		{
			var ad = FhirContext.forR4().newXmlParser().parseResource(ActivityDefinition.class, in);

			Stream<Requester> requesters = helper.getRequesters(ad, "http://highmed.org/bpe/Process/test", "0.5.0",
					"foo", "http://bar.org/fhir/StructureDefinition/baz");
			assertNotNull(requesters);
			List<Requester> requestersList = requesters.collect(Collectors.toList());
			assertEquals(1, requestersList.size());
			assertTrue(requestersList.get(0) instanceof All);
			assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ALL,
					requestersList.get(0).getProcessAuthorizationCode().getCode());
			assertTrue(requestersList.get(0).isRequesterAuthorized(
					User.remote(new org.hl7.fhir.r4.model.Organization().setActive(true)), Collections.emptyList()));

			Stream<Recipient> recipients = helper.getRecipients(ad, "http://highmed.org/bpe/Process/test", "0.5.0",
					"foo", "http://bar.org/fhir/StructureDefinition/baz");
			assertNotNull(recipients);
			List<Recipient> recipientsList = recipients.collect(Collectors.toList());
			assertEquals(1, recipientsList.size());
			assertTrue(recipientsList.get(0) instanceof All);
			assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ALL,
					recipientsList.get(0).getProcessAuthorizationCode().getCode());
			assertTrue(recipientsList.get(0).isRecipientAuthorized(
					User.local(new org.hl7.fhir.r4.model.Organization().setActive(true)), Collections.emptyList()));
		}
	}

	@Test
	public void testGetRequesterRemoteOrganizationRecipientLocalConsortiumRoleViaFile() throws Exception
	{
		try (InputStream in = Files.newInputStream(Paths.get(
				"src/test/resources/authorization/process-authorization/req_remote_organization_rec_local_role.xml")))
		{
			var ad = FhirContext.forR4().newXmlParser().parseResource(ActivityDefinition.class, in);

			Stream<Requester> requesters = helper.getRequesters(ad, "http://highmed.org/bpe/Process/test", "0.5.0",
					"foo", "http://bar.org/fhir/StructureDefinition/baz");
			assertNotNull(requesters);
			List<Requester> requestersList = requesters.collect(Collectors.toList());
			assertEquals(1, requestersList.size());
			assertTrue(requestersList.get(0) instanceof Organization);
			assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ORGANIZATION,
					requestersList.get(0).getProcessAuthorizationCode().getCode());
			User remoteUser = User.remote(new org.hl7.fhir.r4.model.Organization().setActive(true)
					.addIdentifier(new Identifier().setSystem(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM)
							.setValue("organization.com")));
			assertTrue(requestersList.get(0).isRequesterAuthorized(remoteUser, Collections.emptyList()));

			Stream<Recipient> recipients = helper.getRecipients(ad, "http://highmed.org/bpe/Process/test", "0.5.0",
					"foo", "http://bar.org/fhir/StructureDefinition/baz");
			assertNotNull(recipients);
			List<Recipient> recipientsList = recipients.collect(Collectors.toList());
			assertEquals(1, recipientsList.size());
			assertTrue(recipientsList.get(0) instanceof Role);
			assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ROLE,
					recipientsList.get(0).getProcessAuthorizationCode().getCode());
			User localUser = User.local(new org.hl7.fhir.r4.model.Organization().setActive(true)
					.addIdentifier(new Identifier().setSystem(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM)
							.setValue("member.com")));
			OrganizationAffiliation affiliation = new OrganizationAffiliation();
			affiliation.setActive(true);
			affiliation.getOrganization().getIdentifier()
					.setSystem(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM).setValue("consortium.org");
			affiliation.getParticipatingOrganization().getIdentifier()
					.setSystem(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM).setValue("member.com");
			affiliation.getCodeFirstRep().getCodingFirstRep()
					.setSystem("http://highmed.org/fhir/CodeSystem/organization-role").setCode("MeDIC");
			assertTrue(recipientsList.get(0).isRecipientAuthorized(localUser, Collections.singleton(affiliation)));
		}
	}

	@Test
	public void testAddRequesterLocalAllRecipientLocalAll() throws Exception
	{
		String messageName = "messageName";
		String taskProfile = "http://foo.com/fhir/StructureDefinition/bar";
		Requester requesterLocalAll = Requester.localAll();
		Recipient recipientLocalAll = Recipient.localAll();

		var ad = createActivityDefinition();

		ad = helper.add(ad, messageName, taskProfile, requesterLocalAll, recipientLocalAll);

		assertNotNull(ad);
		assertTrue(ad.hasExtension());
		assertNotNull(ad.getExtension());
		assertEquals(1, ad.getExtension().size());

		Extension authExt = ad.getExtension().get(0);
		assertNotNull(authExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION, authExt.getUrl());
		assertTrue(authExt.hasExtension());
		assertEquals(4, authExt.getExtension().size());

		Extension mnExt = authExt.getExtension().get(0);
		assertNotNull(mnExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_MESSAGE_NAME, mnExt.getUrl());
		assertTrue(mnExt.hasValue());
		assertNotNull(mnExt.getValue());
		assertTrue(mnExt.getValue() instanceof StringType);
		assertTrue(((StringType) mnExt.getValue()).hasValue());
		assertNotNull(((StringType) mnExt.getValue()).getValueAsString());
		assertEquals(messageName, ((StringType) mnExt.getValue()).getValueAsString());

		Extension tpExt = authExt.getExtension().get(1);
		assertNotNull(tpExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_TASK_PROFILE, tpExt.getUrl());
		assertTrue(tpExt.hasValue());
		assertNotNull(tpExt.getValue());
		assertTrue(tpExt.getValue() instanceof CanonicalType);
		assertTrue(((CanonicalType) tpExt.getValue()).hasValue());
		assertNotNull(((CanonicalType) tpExt.getValue()).getValueAsString());
		assertEquals(taskProfile, ((CanonicalType) tpExt.getValue()).getValueAsString());

		Extension reqExt = authExt.getExtension().get(2);
		assertNotNull(reqExt);
		assertTrue(reqExt.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_REQUESTER, reqExt.getUrl());
		assertTrue(reqExt.hasValue());
		assertNotNull(reqExt.getValue());
		assertTrue(reqExt.getValue() instanceof Coding);
		assertTrue(((Coding) reqExt.getValue()).hasSystem());
		assertTrue(((Coding) reqExt.getValue()).hasCode());
		assertNotNull(((Coding) reqExt.getValue()).getSystem());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM, ((Coding) reqExt.getValue()).getSystem());
		assertNotNull(((Coding) reqExt.getValue()).getCode());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ALL,
				((Coding) reqExt.getValue()).getCode());
		assertTrue(requesterLocalAll.requesterMatches(reqExt));
		assertTrue(requesterLocalAll.matches((Coding) reqExt.getValue()));

		Extension recExt = authExt.getExtension().get(3);
		assertNotNull(recExt);
		assertTrue(recExt.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_RECIPIENT, recExt.getUrl());
		assertTrue(recExt.hasValue());
		assertNotNull(recExt.getValue());
		assertTrue(recExt.getValue() instanceof Coding);
		assertTrue(((Coding) recExt.getValue()).hasSystem());
		assertTrue(((Coding) recExt.getValue()).hasCode());
		assertNotNull(((Coding) recExt.getValue()).getSystem());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM, ((Coding) recExt.getValue()).getSystem());
		assertNotNull(((Coding) recExt.getValue()).getCode());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ALL,
				((Coding) recExt.getValue()).getCode());
		assertTrue(recipientLocalAll.recipientMatches(recExt));
		assertTrue(recipientLocalAll.matches((Coding) recExt.getValue()));
	}

	@Test
	public void testAddRequesterLocalAllRemoteAllRecipientLocalAll() throws Exception
	{
		String messageName = "messageName";
		String taskProfile = "http://foo.com/fhir/StructureDefinition/bar";
		Requester requesterLocalAll = Requester.localAll();
		Requester requesterRemoteAll = Requester.remoteAll();
		Recipient recipientLocalAll = Recipient.localAll();

		var ad = createActivityDefinition();

		ad = helper.add(ad, messageName, taskProfile, Arrays.asList(requesterLocalAll, requesterRemoteAll),
				Collections.singleton(recipientLocalAll));

		assertNotNull(ad);
		assertTrue(ad.hasExtension());
		assertNotNull(ad.getExtension());
		assertEquals(1, ad.getExtension().size());

		Extension authExt = ad.getExtension().get(0);
		assertNotNull(authExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION, authExt.getUrl());
		assertTrue(authExt.hasExtension());
		assertEquals(5, authExt.getExtension().size());

		Extension mnExt = authExt.getExtension().get(0);
		assertNotNull(mnExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_MESSAGE_NAME, mnExt.getUrl());
		assertTrue(mnExt.hasValue());
		assertNotNull(mnExt.getValue());
		assertTrue(mnExt.getValue() instanceof StringType);
		assertTrue(((StringType) mnExt.getValue()).hasValue());
		assertNotNull(((StringType) mnExt.getValue()).getValueAsString());
		assertEquals(messageName, ((StringType) mnExt.getValue()).getValueAsString());

		Extension tpExt = authExt.getExtension().get(1);
		assertNotNull(tpExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_TASK_PROFILE, tpExt.getUrl());
		assertTrue(tpExt.hasValue());
		assertNotNull(tpExt.getValue());
		assertTrue(tpExt.getValue() instanceof CanonicalType);
		assertTrue(((CanonicalType) tpExt.getValue()).hasValue());
		assertNotNull(((CanonicalType) tpExt.getValue()).getValueAsString());
		assertEquals(taskProfile, ((CanonicalType) tpExt.getValue()).getValueAsString());

		Extension reqExt1 = authExt.getExtension().get(2);
		assertNotNull(reqExt1);
		assertTrue(reqExt1.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_REQUESTER, reqExt1.getUrl());
		assertTrue(reqExt1.hasValue());
		assertNotNull(reqExt1.getValue());
		assertTrue(reqExt1.getValue() instanceof Coding);
		assertTrue(((Coding) reqExt1.getValue()).hasSystem());
		assertTrue(((Coding) reqExt1.getValue()).hasCode());
		assertNotNull(((Coding) reqExt1.getValue()).getSystem());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM,
				((Coding) reqExt1.getValue()).getSystem());
		assertNotNull(((Coding) reqExt1.getValue()).getCode());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ALL,
				((Coding) reqExt1.getValue()).getCode());
		assertTrue(requesterLocalAll.requesterMatches(reqExt1));
		assertTrue(requesterLocalAll.matches((Coding) reqExt1.getValue()));

		Extension reqExt2 = authExt.getExtension().get(3);
		assertNotNull(reqExt2);
		assertTrue(reqExt2.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_REQUESTER, reqExt2.getUrl());
		assertTrue(reqExt2.hasValue());
		assertNotNull(reqExt2.getValue());
		assertTrue(reqExt2.getValue() instanceof Coding);
		assertTrue(((Coding) reqExt2.getValue()).hasSystem());
		assertTrue(((Coding) reqExt2.getValue()).hasCode());
		assertNotNull(((Coding) reqExt2.getValue()).getSystem());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM,
				((Coding) reqExt2.getValue()).getSystem());
		assertNotNull(((Coding) reqExt2.getValue()).getCode());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ALL,
				((Coding) reqExt2.getValue()).getCode());
		assertTrue(requesterRemoteAll.requesterMatches(reqExt2));
		assertTrue(requesterRemoteAll.matches((Coding) reqExt2.getValue()));

		Extension recExt = authExt.getExtension().get(4);
		assertNotNull(recExt);
		assertTrue(recExt.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_RECIPIENT, recExt.getUrl());
		assertTrue(recExt.hasValue());
		assertNotNull(recExt.getValue());
		assertTrue(recExt.getValue() instanceof Coding);
		assertTrue(((Coding) recExt.getValue()).hasSystem());
		assertTrue(((Coding) recExt.getValue()).hasCode());
		assertNotNull(((Coding) recExt.getValue()).getSystem());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM, ((Coding) recExt.getValue()).getSystem());
		assertNotNull(((Coding) recExt.getValue()).getCode());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ALL,
				((Coding) recExt.getValue()).getCode());
		assertTrue(recipientLocalAll.recipientMatches(recExt));
		assertTrue(recipientLocalAll.matches((Coding) recExt.getValue()));
	}

	@Test
	public void testAddRequesterLocalOrgRemoteOrgRecipientLocalOrg() throws Exception
	{
		String messageName = "messageName";
		String taskProfile = "http://foo.com/fhir/StructureDefinition/bar";
		String localOrg1 = "local-org1.com";
		String remoteOrg1 = "remote-org.com";
		String localOrg2 = "local-org2.com";
		Requester requesterLocalOrg = Requester.localOrganization(localOrg1);
		Requester requesterRemoteOrg = Requester.remoteOrganization(remoteOrg1);
		Recipient recipientLocalOrg = Recipient.localOrganization(localOrg2);

		var ad = createActivityDefinition();

		ad = helper.add(ad, messageName, taskProfile, Arrays.asList(requesterLocalOrg, requesterRemoteOrg),
				Collections.singleton(recipientLocalOrg));

		assertNotNull(ad);
		assertTrue(ad.hasExtension());
		assertNotNull(ad.getExtension());
		assertEquals(1, ad.getExtension().size());

		Extension authExt = ad.getExtension().get(0);
		assertNotNull(authExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION, authExt.getUrl());
		assertTrue(authExt.hasExtension());
		assertEquals(5, authExt.getExtension().size());

		Extension mnExt = authExt.getExtension().get(0);
		assertNotNull(mnExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_MESSAGE_NAME, mnExt.getUrl());
		assertTrue(mnExt.hasValue());
		assertNotNull(mnExt.getValue());
		assertTrue(mnExt.getValue() instanceof StringType);
		assertTrue(((StringType) mnExt.getValue()).hasValue());
		assertNotNull(((StringType) mnExt.getValue()).getValueAsString());
		assertEquals(messageName, ((StringType) mnExt.getValue()).getValueAsString());

		Extension tpExt = authExt.getExtension().get(1);
		assertNotNull(tpExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_TASK_PROFILE, tpExt.getUrl());
		assertTrue(tpExt.hasValue());
		assertNotNull(tpExt.getValue());
		assertTrue(tpExt.getValue() instanceof CanonicalType);
		assertTrue(((CanonicalType) tpExt.getValue()).hasValue());
		assertNotNull(((CanonicalType) tpExt.getValue()).getValueAsString());
		assertEquals(taskProfile, ((CanonicalType) tpExt.getValue()).getValueAsString());

		Extension reqExt1 = authExt.getExtension().get(2);
		assertNotNull(reqExt1);
		assertTrue(reqExt1.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_REQUESTER, reqExt1.getUrl());
		assertTrue(reqExt1.hasValue());
		assertNotNull(reqExt1.getValue());
		assertTrue(reqExt1.getValue() instanceof Coding);
		Coding reqCode1 = (Coding) reqExt1.getValue();
		assertTrue(reqCode1.hasSystem());
		assertTrue(reqCode1.hasCode());
		assertNotNull(reqCode1.getSystem());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM, reqCode1.getSystem());
		assertNotNull(reqCode1.getCode());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ORGANIZATION, reqCode1.getCode());
		assertTrue(requesterLocalOrg.requesterMatches(reqExt1));
		assertTrue(requesterLocalOrg.matches(reqCode1));
		assertTrue(reqCode1.hasExtension());
		assertNotNull(reqCode1.getExtension());
		assertEquals(1, reqCode1.getExtension().size());
		Extension reqCode1Ext = reqCode1.getExtension().get(0);
		assertNotNull(reqCode1Ext);
		assertTrue(reqCode1Ext.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_ORGANIZATION, reqCode1Ext.getUrl());
		assertFalse(reqCode1Ext.hasExtension());
		assertTrue(reqCode1Ext.hasValue());
		assertNotNull(reqCode1Ext.getValue());
		assertTrue(reqCode1Ext.getValue() instanceof Identifier);
		assertTrue(((Identifier) reqCode1Ext.getValue()).hasSystem());
		assertNotNull(((Identifier) reqCode1Ext.getValue()).getSystem());
		assertEquals(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM,
				((Identifier) reqCode1Ext.getValue()).getSystem());
		assertTrue(((Identifier) reqCode1Ext.getValue()).hasValue());
		assertNotNull(((Identifier) reqCode1Ext.getValue()).getValue());
		assertEquals(localOrg1, ((Identifier) reqCode1Ext.getValue()).getValue());

		Extension reqExt2 = authExt.getExtension().get(3);
		assertNotNull(reqExt2);
		assertTrue(reqExt2.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_REQUESTER, reqExt2.getUrl());
		assertTrue(reqExt2.hasValue());
		assertNotNull(reqExt2.getValue());
		assertTrue(reqExt2.getValue() instanceof Coding);
		Coding reqCode2 = (Coding) reqExt2.getValue();
		assertTrue(reqCode2.hasSystem());
		assertTrue(reqCode2.hasCode());
		assertNotNull(reqCode2.getSystem());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM, reqCode2.getSystem());
		assertNotNull(reqCode2.getCode());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ORGANIZATION, reqCode2.getCode());
		assertTrue(requesterRemoteOrg.requesterMatches(reqExt2));
		assertTrue(requesterRemoteOrg.matches(reqCode2));
		assertNotNull(reqCode2.getExtension());
		assertEquals(1, reqCode2.getExtension().size());
		Extension reqCode2Ext = reqCode2.getExtension().get(0);
		assertNotNull(reqCode2Ext);
		assertTrue(reqCode2Ext.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_ORGANIZATION, reqCode2Ext.getUrl());
		assertFalse(reqCode2Ext.hasExtension());
		assertTrue(reqCode2Ext.hasValue());
		assertNotNull(reqCode2Ext.getValue());
		assertTrue(reqCode2Ext.getValue() instanceof Identifier);
		assertTrue(((Identifier) reqCode2Ext.getValue()).hasSystem());
		assertNotNull(((Identifier) reqCode2Ext.getValue()).getSystem());
		assertEquals(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM,
				((Identifier) reqCode2Ext.getValue()).getSystem());
		assertTrue(((Identifier) reqCode2Ext.getValue()).hasValue());
		assertNotNull(((Identifier) reqCode2Ext.getValue()).getValue());
		assertEquals(remoteOrg1, ((Identifier) reqCode2Ext.getValue()).getValue());

		Extension recExt = authExt.getExtension().get(4);
		assertNotNull(recExt);
		assertTrue(recExt.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_RECIPIENT, recExt.getUrl());
		assertTrue(recExt.hasValue());
		assertNotNull(recExt.getValue());
		assertTrue(recExt.getValue() instanceof Coding);
		Coding recCode = (Coding) recExt.getValue();
		assertTrue(recCode.hasSystem());
		assertTrue(recCode.hasCode());
		assertNotNull(recCode.getSystem());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM, recCode.getSystem());
		assertNotNull(recCode.getCode());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ORGANIZATION, recCode.getCode());
		assertTrue(recipientLocalOrg.recipientMatches(recExt));
		assertTrue(recipientLocalOrg.matches((Coding) recExt.getValue()));
		assertNotNull(recCode.getExtension());
		assertEquals(1, recCode.getExtension().size());
		Extension recCodeExt = recCode.getExtension().get(0);
		assertNotNull(recCodeExt);
		assertTrue(recCodeExt.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_ORGANIZATION, recCodeExt.getUrl());
		assertFalse(recCodeExt.hasExtension());
		assertTrue(recCodeExt.hasValue());
		assertNotNull(recCodeExt.getValue());
		assertTrue(recCodeExt.getValue() instanceof Identifier);
		assertTrue(((Identifier) recCodeExt.getValue()).hasSystem());
		assertNotNull(((Identifier) recCodeExt.getValue()).getSystem());
		assertEquals(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM,
				((Identifier) recCodeExt.getValue()).getSystem());
		assertTrue(((Identifier) recCodeExt.getValue()).hasValue());
		assertNotNull(((Identifier) recCodeExt.getValue()).getValue());
		assertEquals(localOrg2, ((Identifier) recCodeExt.getValue()).getValue());
	}

	@Test
	public void testAddRequesterLocalRoleRemoteRoleRecipientLocalRole() throws Exception
	{
		String messageName = "messageName";
		String taskProfile = "http://foo.com/fhir/StructureDefinition/bar";
		String consortium1 = "consortium1.org";
		String role1System = "http://baz.com/fhir/CodeSystem/cs1";
		String role1Code = "code1";
		String consortium2 = "consortium2.org";
		String role2System = "http://baz.com/fhir/CodeSystem/cs2";
		String role2Code = "code2";
		String consortium3 = "consortium3.org";
		String role3System = "http://baz.com/fhir/CodeSystem/cs3";
		String role3Code = "code3";
		Requester requesterLocalRole = Requester.localRole(consortium1, role1System, role1Code);
		Requester requesterRemoteRole = Requester.remoteRole(consortium2, role2System, role2Code);
		Recipient recipientLocalRole = Recipient.localRole(consortium3, role3System, role3Code);

		var ad = createActivityDefinition();

		ad = helper.add(ad, messageName, taskProfile, Arrays.asList(requesterLocalRole, requesterRemoteRole),
				Collections.singleton(recipientLocalRole));

		assertNotNull(ad);
		assertTrue(ad.hasExtension());
		assertNotNull(ad.getExtension());
		assertEquals(1, ad.getExtension().size());

		Extension authExt = ad.getExtension().get(0);
		assertNotNull(authExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION, authExt.getUrl());
		assertTrue(authExt.hasExtension());
		assertEquals(5, authExt.getExtension().size());

		Extension mnExt = authExt.getExtension().get(0);
		assertNotNull(mnExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_MESSAGE_NAME, mnExt.getUrl());
		assertTrue(mnExt.hasValue());
		assertNotNull(mnExt.getValue());
		assertTrue(mnExt.getValue() instanceof StringType);
		assertTrue(((StringType) mnExt.getValue()).hasValue());
		assertNotNull(((StringType) mnExt.getValue()).getValueAsString());
		assertEquals(messageName, ((StringType) mnExt.getValue()).getValueAsString());

		Extension tpExt = authExt.getExtension().get(1);
		assertNotNull(tpExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_TASK_PROFILE, tpExt.getUrl());
		assertTrue(tpExt.hasValue());
		assertNotNull(tpExt.getValue());
		assertTrue(tpExt.getValue() instanceof CanonicalType);
		assertTrue(((CanonicalType) tpExt.getValue()).hasValue());
		assertNotNull(((CanonicalType) tpExt.getValue()).getValueAsString());
		assertEquals(taskProfile, ((CanonicalType) tpExt.getValue()).getValueAsString());

		Extension reqExt1 = authExt.getExtension().get(2);
		assertNotNull(reqExt1);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_REQUESTER, reqExt1.getUrl());
		assertTrue(reqExt1.hasValue());
		assertNotNull(reqExt1.getValue());
		assertTrue(reqExt1.getValue() instanceof Coding);
		Coding reqCode1 = (Coding) reqExt1.getValue();
		assertTrue(reqCode1.hasSystem());
		assertTrue(reqCode1.hasCode());
		assertNotNull(reqCode1.getSystem());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM, reqCode1.getSystem());
		assertNotNull(reqCode1.getCode());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ROLE, reqCode1.getCode());
		assertTrue(requesterLocalRole.requesterMatches(reqExt1));
		assertTrue(requesterLocalRole.matches((Coding) reqExt1.getValue()));
		assertTrue(reqCode1.hasExtension());
		assertNotNull(reqCode1.getExtension());
		assertEquals(1, reqCode1.getExtension().size());
		Extension reqCode1Ext = reqCode1.getExtension().get(0);
		assertNotNull(reqCode1Ext);
		assertTrue(reqCode1Ext.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_CONSORTIUM_ROLE, reqCode1Ext.getUrl());
		assertTrue(reqCode1Ext.hasExtension());
		assertFalse(reqCode1Ext.hasValue());
		assertNotNull(reqCode1Ext.getExtension());
		assertEquals(2, reqCode1Ext.getExtension().size());
		List<Extension> reqCode1ExtExts = reqCode1Ext.getExtension();
		Extension reqCode1ExtC = reqCode1ExtExts.get(0);
		assertTrue(reqCode1ExtC.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_CONSORTIUM_ROLE_CONSORTIUM,
				reqCode1ExtC.getUrl());
		Extension reqCode1ExtR = reqCode1ExtExts.get(1);
		assertTrue(reqCode1ExtR.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_CONSORTIUM_ROLE_ROLE,
				reqCode1ExtR.getUrl());
		assertTrue(reqCode1ExtC.hasValue());
		assertTrue(reqCode1ExtC.getValue() instanceof Identifier);
		assertTrue(((Identifier) reqCode1ExtC.getValue()).hasSystem());
		assertEquals(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM,
				((Identifier) reqCode1ExtC.getValue()).getSystem());
		assertTrue(((Identifier) reqCode1ExtC.getValue()).hasValue());
		assertEquals(consortium1, ((Identifier) reqCode1ExtC.getValue()).getValue());
		assertTrue(reqCode1ExtR.hasValue());
		assertTrue(reqCode1ExtR.getValue() instanceof Coding);
		assertTrue(((Coding) reqCode1ExtR.getValue()).hasSystem());
		assertEquals(role1System, ((Coding) reqCode1ExtR.getValue()).getSystem());
		assertTrue(((Coding) reqCode1ExtR.getValue()).hasCode());
		assertEquals(role1Code, ((Coding) reqCode1ExtR.getValue()).getCode());

		Extension reqExt2 = authExt.getExtension().get(3);
		assertNotNull(reqExt2);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_REQUESTER, reqExt2.getUrl());
		assertTrue(reqExt2.hasValue());
		assertNotNull(reqExt2.getValue());
		assertTrue(reqExt2.getValue() instanceof Coding);
		Coding reqCode2 = (Coding) reqExt2.getValue();
		assertTrue(reqCode2.hasSystem());
		assertTrue(reqCode2.hasCode());
		assertNotNull(reqCode2.getSystem());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM, reqCode2.getSystem());
		assertNotNull(reqCode2.getCode());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_REMOTE_ROLE, reqCode2.getCode());
		assertTrue(requesterRemoteRole.requesterMatches(reqExt2));
		assertTrue(requesterRemoteRole.matches((Coding) reqExt2.getValue()));
		assertTrue(reqCode2.hasExtension());
		assertNotNull(reqCode2.getExtension());
		assertEquals(1, reqCode2.getExtension().size());
		Extension reqCode2Ext = reqCode2.getExtension().get(0);
		assertNotNull(reqCode2Ext);
		assertTrue(reqCode2Ext.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_CONSORTIUM_ROLE, reqCode2Ext.getUrl());
		assertTrue(reqCode2Ext.hasExtension());
		assertFalse(reqCode2Ext.hasValue());
		assertNotNull(reqCode2Ext.getExtension());
		assertEquals(2, reqCode2Ext.getExtension().size());
		List<Extension> reqCode2ExtExts = reqCode2Ext.getExtension();
		Extension reqCode2ExtC = reqCode2ExtExts.get(0);
		assertTrue(reqCode2ExtC.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_CONSORTIUM_ROLE_CONSORTIUM,
				reqCode2ExtC.getUrl());
		Extension reqCode2ExtR = reqCode2ExtExts.get(1);
		assertTrue(reqCode2ExtR.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_CONSORTIUM_ROLE_ROLE,
				reqCode2ExtR.getUrl());
		assertTrue(reqCode2ExtC.hasValue());
		assertTrue(reqCode2ExtC.getValue() instanceof Identifier);
		assertTrue(((Identifier) reqCode2ExtC.getValue()).hasSystem());
		assertEquals(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM,
				((Identifier) reqCode2ExtC.getValue()).getSystem());
		assertTrue(((Identifier) reqCode2ExtC.getValue()).hasValue());
		assertEquals(consortium2, ((Identifier) reqCode2ExtC.getValue()).getValue());
		assertTrue(reqCode2ExtR.hasValue());
		assertTrue(reqCode2ExtR.getValue() instanceof Coding);
		assertTrue(((Coding) reqCode2ExtR.getValue()).hasSystem());
		assertEquals(role2System, ((Coding) reqCode2ExtR.getValue()).getSystem());
		assertTrue(((Coding) reqCode2ExtR.getValue()).hasCode());
		assertEquals(role2Code, ((Coding) reqCode2ExtR.getValue()).getCode());

		Extension recExt = authExt.getExtension().get(4);
		assertNotNull(recExt);
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_RECIPIENT, recExt.getUrl());
		assertTrue(recExt.hasValue());
		assertNotNull(recExt.getValue());
		assertTrue(recExt.getValue() instanceof Coding);
		Coding recCode = (Coding) recExt.getValue();
		assertTrue(recCode.hasSystem());
		assertTrue(recCode.hasCode());
		assertNotNull(recCode.getSystem());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_SYSTEM, recCode.getSystem());
		assertNotNull(recCode.getCode());
		assertEquals(ProcessAuthorizationHelper.PROCESS_AUTHORIZATION_VALUE_LOCAL_ROLE, recCode.getCode());
		assertTrue(recipientLocalRole.recipientMatches(recExt));
		assertTrue(recipientLocalRole.matches((Coding) recExt.getValue()));
		assertTrue(recCode.hasExtension());
		assertNotNull(recCode.getExtension());
		assertEquals(1, recCode.getExtension().size());
		Extension recCodeExt = recCode.getExtension().get(0);
		assertNotNull(recCodeExt);
		assertTrue(recCodeExt.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_CONSORTIUM_ROLE, recCodeExt.getUrl());
		assertTrue(recCodeExt.hasExtension());
		assertFalse(recCodeExt.hasValue());

		assertNotNull(recCodeExt.getExtension());
		assertEquals(2, recCodeExt.getExtension().size());
		List<Extension> recCodeExtExts = recCodeExt.getExtension();
		Extension recCodeExtC = recCodeExtExts.get(0);
		assertTrue(recCodeExtC.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_CONSORTIUM_ROLE_CONSORTIUM,
				recCodeExtC.getUrl());
		Extension recCodeExtR = recCodeExtExts.get(1);
		assertTrue(recCodeExtR.hasUrl());
		assertEquals(ProcessAuthorizationHelper.EXTENSION_PROCESS_AUTHORIZATION_CONSORTIUM_ROLE_ROLE,
				recCodeExtR.getUrl());
		assertTrue(recCodeExtC.hasValue());
		assertTrue(recCodeExtC.getValue() instanceof Identifier);
		assertTrue(((Identifier) recCodeExtC.getValue()).hasSystem());
		assertEquals(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM,
				((Identifier) recCodeExtC.getValue()).getSystem());
		assertTrue(((Identifier) recCodeExtC.getValue()).hasValue());
		assertEquals(consortium3, ((Identifier) recCodeExtC.getValue()).getValue());
		assertTrue(recCodeExtR.hasValue());
		assertTrue(recCodeExtR.getValue() instanceof Coding);
		assertTrue(((Coding) recCodeExtR.getValue()).hasSystem());
		assertEquals(role3System, ((Coding) recCodeExtR.getValue()).getSystem());
		assertTrue(((Coding) recCodeExtR.getValue()).hasCode());
		assertEquals(role3Code, ((Coding) recCodeExtR.getValue()).getCode());
	}
}
