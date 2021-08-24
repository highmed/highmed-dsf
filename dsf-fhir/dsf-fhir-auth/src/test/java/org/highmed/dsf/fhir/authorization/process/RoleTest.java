package org.highmed.dsf.fhir.authorization.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.junit.Test;

public class RoleTest
{
	private static final String CONSORTIUM_IDENTIFIER = "consortium.org";
	private static final String MEMBER_IDENTIFIER = "member.com";
	private static final String MEMBER_ROLE_SYSTEM = "roleSystem";
	private static final String MEMBER_ROLE_CODE = "roleCode";

	private static final Role local = new Role(UserRole.LOCAL, CONSORTIUM_IDENTIFIER, MEMBER_ROLE_SYSTEM,
			MEMBER_ROLE_CODE);
	private static final Role remote = new Role(UserRole.REMOTE, CONSORTIUM_IDENTIFIER, MEMBER_ROLE_SYSTEM,
			MEMBER_ROLE_CODE);

	private static org.hl7.fhir.r4.model.Organization createFhirOrganization(String identifierValue)
	{
		return createFhirOrganization(identifierValue, ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM);
	}

	private static org.hl7.fhir.r4.model.Organization createFhirOrganization(String identifierValue,
			String identifierSystem)
	{
		var o = new org.hl7.fhir.r4.model.Organization();
		o.setActive(true);
		o.getIdentifierFirstRep().setSystem(identifierSystem).setValue(identifierValue);
		return o;
	}

	private static final User LOCAL_ORG_ACTIVE = User.local(createFhirOrganization(MEMBER_IDENTIFIER));
	private static final User LOCAL_ORG_NOT_ACTIVE = User
			.local(createFhirOrganization(MEMBER_IDENTIFIER).setActive(false));
	private static final User LOCAL_NO_ORG = User.local(null);
	private static final User LOCAL_ORG_BAD_IDENTIFIER = User.local(createFhirOrganization("wrong.identifier"));
	private static final User LOCAL_ORG_BAD_IDENTIFIER_SYSTEM = User
			.local(createFhirOrganization(MEMBER_IDENTIFIER, "bad.system"));
	private static final User REMOTE_ORG_ACTIVE = User.remote(createFhirOrganization(MEMBER_IDENTIFIER));
	private static final User REMOTE_ORG_NOT_ACTIVE = User
			.remote(createFhirOrganization(MEMBER_IDENTIFIER).setActive(false));
	private static final User REMOTE_NO_ORG = User.remote((Organization) null);
	private static final User REMOTE_ORG_BAD_IDENTIFIER = User.remote(createFhirOrganization("wrong.identifier"));
	private static final User REMOTE_ORG_BAD_IDENTIFIER_SYSTEM = User
			.remote(createFhirOrganization(MEMBER_IDENTIFIER, "bad.system"));

	private static OrganizationAffiliation createOrganizationAffiliation(String consortiumIdentifier,
			String memberIdentifier, String memberRoleSystem, String memberRoleCode)
	{
		var a = new OrganizationAffiliation();
		a.setActive(true);
		a.getOrganization().setType("Organization").getIdentifier()
				.setSystem(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM).setValue(consortiumIdentifier);
		a.getParticipatingOrganization().setType("Organization").getIdentifier()
				.setSystem(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM).setValue(memberIdentifier);
		a.getCodeFirstRep().getCodingFirstRep().setSystem(memberRoleSystem).setCode(memberRoleCode);

		return a;
	}

	private static final OrganizationAffiliation OK_AFFILIATION = createOrganizationAffiliation(CONSORTIUM_IDENTIFIER,
			MEMBER_IDENTIFIER, MEMBER_ROLE_SYSTEM, MEMBER_ROLE_CODE);

	private static Stream<OrganizationAffiliation> okAffiliation()
	{
		return Stream.of(OK_AFFILIATION);
	}

	@Test
	public void testLocalRoleRecipientOk() throws Exception
	{
		assertTrue(local.isRecipientAuthorized(LOCAL_ORG_ACTIVE, okAffiliation()));
	}

	@Test
	public void testLocalRoleRecipientNotOkOrganizationNotActive() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(LOCAL_ORG_NOT_ACTIVE, okAffiliation()));
	}

	@Test
	public void testLocalRoleRecipientNotOkNoOrganization() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(LOCAL_NO_ORG, okAffiliation()));
	}

	@Test
	public void testLocalRoleRecipientNotOkNoUser() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(null, okAffiliation()));
	}

	@Test
	public void testLocalRoleRecipientNotOkRemoteOrganization() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(REMOTE_ORG_ACTIVE, okAffiliation()));
	}

	@Test
	public void testLocalRoleRecipientNotOkNoAffiliations() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(LOCAL_ORG_ACTIVE, Stream.empty()));
	}

	@Test
	public void testLocalRoleRecipientNotOkAffiliationsNull() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(LOCAL_ORG_ACTIVE, (Stream<OrganizationAffiliation>) null));
	}

	@Test
	public void testLocalRoleRecipientNotOkBadMemberIdentifier() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(LOCAL_ORG_BAD_IDENTIFIER, okAffiliation()));
	}

	@Test
	public void testLocalRoleRecipientNotOkBadMemberIdentifierSystem() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(LOCAL_ORG_BAD_IDENTIFIER_SYSTEM, okAffiliation()));
	}

	@Test
	public void testLocalRoleRecipientNotOkBadMemberRoleCode() throws Exception
	{
		Stream<OrganizationAffiliation> affiliations = Stream.of(createOrganizationAffiliation(CONSORTIUM_IDENTIFIER,
				MEMBER_IDENTIFIER, MEMBER_ROLE_SYSTEM, "bad.roleCode"));

		assertFalse(local.isRecipientAuthorized(LOCAL_ORG_ACTIVE, affiliations));
	}

	@Test
	public void testLocalRoleRecipientNotOkBadMemberRoleSystem() throws Exception
	{
		Stream<OrganizationAffiliation> affiliations = Stream.of(createOrganizationAffiliation(CONSORTIUM_IDENTIFIER,
				MEMBER_IDENTIFIER, "bad.roleSystem", MEMBER_ROLE_CODE));

		assertFalse(local.isRecipientAuthorized(LOCAL_ORG_ACTIVE, affiliations));
	}

	// ---

	@Test
	public void testRemoteRoleRecipientOk() throws Exception
	{
		assertTrue(remote.isRecipientAuthorized(REMOTE_ORG_ACTIVE, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRecipientNotOkOrganizationNotActive() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(REMOTE_ORG_NOT_ACTIVE, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRecipientNotOkNoOrganization() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(REMOTE_NO_ORG, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRecipientNotOkNoUser() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(null, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRecipientNotOkLocalOrganization() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(LOCAL_ORG_ACTIVE, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRecipientNotOkNoAffiliations() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(REMOTE_ORG_ACTIVE, Stream.empty()));
	}

	@Test
	public void testRemoteRoleRecipientNotOkAffiliationsNull() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(REMOTE_ORG_ACTIVE, (Stream<OrganizationAffiliation>) null));
	}

	@Test
	public void testRemoteRoleRecipientNotOkBadMemberIdentifier() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(REMOTE_ORG_BAD_IDENTIFIER, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRecipientNotOkBadMemberIdentifierSystem() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(REMOTE_ORG_BAD_IDENTIFIER_SYSTEM, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRecipientNotOkBadMemberRoleCode() throws Exception
	{
		Stream<OrganizationAffiliation> affiliations = Stream.of(createOrganizationAffiliation(CONSORTIUM_IDENTIFIER,
				MEMBER_IDENTIFIER, MEMBER_ROLE_SYSTEM, "bad.roleCode"));

		assertFalse(remote.isRecipientAuthorized(REMOTE_ORG_ACTIVE, affiliations));
	}

	@Test
	public void testRemoteRoleRecipientNotOkMemberRoleSystem() throws Exception
	{
		Stream<OrganizationAffiliation> affiliations = Stream.of(createOrganizationAffiliation(CONSORTIUM_IDENTIFIER,
				MEMBER_IDENTIFIER, "bad.roleSystem", MEMBER_ROLE_CODE));

		assertFalse(remote.isRecipientAuthorized(REMOTE_ORG_ACTIVE, affiliations));
	}

	// --- --- ---

	@Test
	public void testLocalRoleRequesterOk() throws Exception
	{
		assertTrue(local.isRequesterAuthorized(LOCAL_ORG_ACTIVE, okAffiliation()));
	}

	@Test
	public void testLocalRoleRequesterNotOkOrganizationNotActive() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(LOCAL_ORG_NOT_ACTIVE, okAffiliation()));
	}

	@Test
	public void testLocalRoleRequesterNotOkNoOrganization() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(LOCAL_NO_ORG, okAffiliation()));
	}

	@Test
	public void testLocalRoleRequesterNotOkNoUser() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(null, okAffiliation()));
	}

	@Test
	public void testLocalRoleRequesterNotOkRemoteOrganization() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(REMOTE_ORG_ACTIVE, okAffiliation()));
	}

	@Test
	public void testLocalRoleRequesterNotOkNoAffiliations() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(LOCAL_ORG_ACTIVE, Stream.empty()));
	}

	@Test
	public void testLocalRoleRequesterNotOkAffiliationsNull() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(LOCAL_ORG_ACTIVE, (Stream<OrganizationAffiliation>) null));
	}

	@Test
	public void testLocalRoleRequesterNotOkBadMemberIdentifier() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(LOCAL_ORG_BAD_IDENTIFIER, okAffiliation()));
	}

	@Test
	public void testLocalRoleRequesterNotOkBadMemberIdentifierSystem() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(LOCAL_ORG_BAD_IDENTIFIER_SYSTEM, okAffiliation()));
	}

	@Test
	public void testLocalRoleRequesterNotOkBadMemberRoleCode() throws Exception
	{
		Stream<OrganizationAffiliation> affiliations = Stream.of(createOrganizationAffiliation(CONSORTIUM_IDENTIFIER,
				MEMBER_IDENTIFIER, MEMBER_ROLE_SYSTEM, "bad.roleCode"));

		assertFalse(local.isRequesterAuthorized(LOCAL_ORG_ACTIVE, affiliations));
	}

	@Test
	public void testLocalRoleRequesterNotOkMemberRoleSystem() throws Exception
	{
		Stream<OrganizationAffiliation> affiliations = Stream.of(createOrganizationAffiliation(CONSORTIUM_IDENTIFIER,
				MEMBER_IDENTIFIER, "bad.roleSystem", MEMBER_ROLE_CODE));

		assertFalse(local.isRequesterAuthorized(LOCAL_ORG_ACTIVE, affiliations));
	}

	// ---

	@Test
	public void testRemoteRoleRequesterOk() throws Exception
	{
		assertTrue(remote.isRequesterAuthorized(REMOTE_ORG_ACTIVE, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRequesterNotOkOrganizationNotActive() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(REMOTE_ORG_NOT_ACTIVE, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRequesterNotOkNoOrganization() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(REMOTE_NO_ORG, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRequesterNotOkNoUser() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(null, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRequesterNotOkLocalOrganization() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(LOCAL_ORG_ACTIVE, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRequesterNotOkNoAffiliations() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(REMOTE_ORG_ACTIVE, Stream.empty()));
	}

	@Test
	public void testRemoteRoleRequesterNotOkAffiliationsNull() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(REMOTE_ORG_ACTIVE, (Stream<OrganizationAffiliation>) null));
	}

	@Test
	public void testRemoteRoleRequesterNotOkBadMemberIdentifier() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(REMOTE_ORG_BAD_IDENTIFIER, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRequesterNotOkBadMemberIdentifierSystem() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(REMOTE_ORG_BAD_IDENTIFIER_SYSTEM, okAffiliation()));
	}

	@Test
	public void testRemoteRoleRequesterNotOkBadMemberRoleCode() throws Exception
	{
		Stream<OrganizationAffiliation> affiliations = Stream.of(createOrganizationAffiliation(CONSORTIUM_IDENTIFIER,
				MEMBER_IDENTIFIER, MEMBER_ROLE_SYSTEM, "bad.roleCode"));

		assertFalse(remote.isRequesterAuthorized(REMOTE_ORG_ACTIVE, affiliations));
	}

	@Test
	public void testRemoteRoleRequesterNotOkMemberRoleSystem() throws Exception
	{
		Stream<OrganizationAffiliation> affiliations = Stream.of(createOrganizationAffiliation(CONSORTIUM_IDENTIFIER,
				MEMBER_IDENTIFIER, "bad.roleSystem", MEMBER_ROLE_CODE));

		assertFalse(remote.isRequesterAuthorized(REMOTE_ORG_ACTIVE, affiliations));
	}
}
