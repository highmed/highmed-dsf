package org.highmed.dsf.fhir.authorization.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.junit.Test;

public class OrganizationTest
{
	private static final String IDENTIFIER = "organization.com";

	private Organization local = new Organization(UserRole.LOCAL, IDENTIFIER);
	private Organization remote = new Organization(UserRole.REMOTE, IDENTIFIER);

	private org.hl7.fhir.r4.model.Organization createFhirOrganization(String identifier)
	{
		var o = new org.hl7.fhir.r4.model.Organization();
		o.setActive(true);
		o.getIdentifierFirstRep().setSystem(ProcessAuthorizationHelper.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue(identifier);
		return o;
	}

	@Test
	public void testLocalOrganizationRecipientOk() throws Exception
	{
		assertTrue(local.isRecipientAuthorized(new User(createFhirOrganization(IDENTIFIER), UserRole.LOCAL, "local"),
				Stream.empty()));
	}

	@Test
	public void testLocalOrganizationRecipientNotOkOrganizationNotActive() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(
				new User(createFhirOrganization(IDENTIFIER).setActive(false), UserRole.LOCAL, "local"),
				Stream.empty()));
	}

	@Test
	public void testLocalOrganizationRecipientNotOkNoOrganization() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(new User(null, UserRole.LOCAL, "local"), Stream.empty()));
	}

	@Test
	public void testLocalOrganizationRecipientNotOkNoUser() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(null, Stream.empty()));
	}

	@Test
	public void testLocalOrganizationRecipientNotOkRemoteOrganization() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(new User(createFhirOrganization(IDENTIFIER), UserRole.REMOTE, "remote"),
				Stream.empty()));
	}

	@Test
	public void testLocalOrganizationRecipientNotOkIdentifierWrong() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(
				new User(createFhirOrganization("bad.identifier"), UserRole.LOCAL, "local"), Stream.empty()));
	}

	@Test
	public void testRemoteOrganizationRecipientOk() throws Exception
	{
		assertTrue(remote.isRecipientAuthorized(new User(createFhirOrganization(IDENTIFIER), UserRole.REMOTE, "remote"),
				Stream.empty()));
	}

	@Test
	public void testRemoteOrganizationRecipientNotOkOrganizationNotActive() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(
				new User(createFhirOrganization(IDENTIFIER).setActive(false), UserRole.REMOTE, "remote"),
				Stream.empty()));
	}

	@Test
	public void testRemoteOrganizationRecipientNotOkNoOrganization() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(new User(null, UserRole.REMOTE, "remote"), Stream.empty()));
	}

	@Test
	public void testRemoteOrganizationRecipientNotOkNoUser() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(null, Stream.empty()));
	}

	@Test
	public void testRemoteOrganizationRecipientNotOkLocalOrganization() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(new User(createFhirOrganization(IDENTIFIER), UserRole.LOCAL, "local"),
				Stream.empty()));
	}

	@Test
	public void testRemoteOrganizationRecipientNotOkIdentifierWrong() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(
				new User(createFhirOrganization("bad.identifier"), UserRole.REMOTE, "remote"), Stream.empty()));
	}

	@Test
	public void testLocalOrganizationRequesterOk() throws Exception
	{
		assertTrue(local.isRequesterAuthorized(new User(createFhirOrganization(IDENTIFIER), UserRole.LOCAL, "local"),
				Stream.empty()));
	}

	@Test
	public void testLocalOrganizationRequesterNotOkOrganizationNotActive() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(
				new User(createFhirOrganization(IDENTIFIER).setActive(false), UserRole.LOCAL, "local"),
				Stream.empty()));
	}

	@Test
	public void testLocalOrganizationRequesterNotOkNoOrganization() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(new User(null, UserRole.LOCAL, "local"), Stream.empty()));
	}

	@Test
	public void testLocalOrganizationRequesterNotOkNoUser() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(null, Stream.empty()));
	}

	@Test
	public void testLocalOrganizationRequesterNotOkRemoteOrganization() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(new User(createFhirOrganization(IDENTIFIER), UserRole.REMOTE, "remote"),
				Stream.empty()));
	}

	@Test
	public void testLocalOrganizationRequesterNotOkIdentifierWrong() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(
				new User(createFhirOrganization("bad.identifier"), UserRole.LOCAL, "local"), Stream.empty()));
	}

	@Test
	public void testRemoteOrganizationRequesterOk() throws Exception
	{
		assertTrue(remote.isRequesterAuthorized(new User(createFhirOrganization(IDENTIFIER), UserRole.REMOTE, "remote"),
				Stream.empty()));
	}

	@Test
	public void testRemoteOrganizationRequesterNotOkOrganizationNotActive() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(
				new User(createFhirOrganization(IDENTIFIER).setActive(false), UserRole.REMOTE, "remote"),
				Stream.empty()));
	}

	@Test
	public void testRemoteOrganizationRequesterNotOkNoOrganization() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(new User(null, UserRole.REMOTE, "remote"), Stream.empty()));
	}

	@Test
	public void testRemoteOrganizationRequesterNotOkNoUser() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(null, Stream.empty()));
	}

	@Test
	public void testRemoteOrganizationRequesterNotOkLocalOrganization() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(new User(createFhirOrganization(IDENTIFIER), UserRole.LOCAL, "local"),
				Stream.empty()));
	}

	@Test
	public void testRemoteOrganizationRequesterNotOkIdentifierWrong() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(
				new User(createFhirOrganization("bad.identifier"), UserRole.REMOTE, "remote"), Stream.empty()));
	}
}
