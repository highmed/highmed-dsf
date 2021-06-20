package org.highmed.dsf.fhir.authorization.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.hl7.fhir.r4.model.Organization;
import org.junit.Test;

public class AllTest
{
	private All local = new All(UserRole.LOCAL);
	private All remote = new All(UserRole.REMOTE);

	@Test
	public void testLocalAllRecipientOk() throws Exception
	{
		assertTrue(local.isRecipientAuthorized(new User(new Organization().setActive(true), UserRole.LOCAL, "local"),
				Stream.empty()));
	}

	@Test
	public void testLocalAllRecipientNotOkOrganizationNotActive() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(new User(new Organization().setActive(false), UserRole.LOCAL, "local"),
				Stream.empty()));
	}

	@Test
	public void testLocalAllRecipientNotOkNoOrganization() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(new User(null, UserRole.LOCAL, "local"), Stream.empty()));
	}

	@Test
	public void testLocalAllRecipientNotOkNoUser() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(null, Stream.empty()));
	}

	@Test
	public void testLocalAllRecipientNotOkRemoteOrganization() throws Exception
	{
		assertFalse(local.isRecipientAuthorized(new User(new Organization().setActive(true), UserRole.REMOTE, "remote"),
				Stream.empty()));
	}

	@Test
	public void testRemoteAllRecipientOk() throws Exception
	{
		assertTrue(remote.isRecipientAuthorized(new User(new Organization().setActive(true), UserRole.REMOTE, "remote"),
				Stream.empty()));
	}

	@Test
	public void testRemoteAllRecipientNotOkOrganizationNotActive() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(
				new User(new Organization().setActive(false), UserRole.REMOTE, "remote"), Stream.empty()));
	}

	@Test
	public void testRemoteAllRecipientNotOkNoOrganization() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(new User(null, UserRole.REMOTE, "remote"), Stream.empty()));
	}

	@Test
	public void testRemoteAllRecipientNotOkNoUser() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(null, Stream.empty()));
	}

	@Test
	public void testRemoteAllRecipientNotOkLocalOrganization() throws Exception
	{
		assertFalse(remote.isRecipientAuthorized(new User(new Organization().setActive(true), UserRole.LOCAL, "local"),
				Stream.empty()));
	}

	@Test
	public void testLocalAllRequesterOk() throws Exception
	{
		assertTrue(local.isRequesterAuthorized(new User(new Organization().setActive(true), UserRole.LOCAL, "local"),
				Stream.empty()));
	}

	@Test
	public void testLocalAllRequesterNotOkOrganizationNotActive() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(new User(new Organization().setActive(false), UserRole.LOCAL, "local"),
				Stream.empty()));
	}

	@Test
	public void testLocalAllRequesterNotOkNoOrganization() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(new User(null, UserRole.LOCAL, "local"), Stream.empty()));
	}

	@Test
	public void testLocalAllRequesterNotOkNoUser() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(null, Stream.empty()));
	}

	@Test
	public void testLocalAllRequesterNotOkRemoteOrganization() throws Exception
	{
		assertFalse(local.isRequesterAuthorized(new User(new Organization().setActive(true), UserRole.REMOTE, "remote"),
				Stream.empty()));
	}

	@Test
	public void testRemoteAllRequesterOk() throws Exception
	{
		assertTrue(remote.isRequesterAuthorized(new User(new Organization().setActive(true), UserRole.REMOTE, "remote"),
				Stream.empty()));
	}

	@Test
	public void testRemoteAllRequesterNotOkOrganizationNotActive() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(
				new User(new Organization().setActive(false), UserRole.REMOTE, "remote"), Stream.empty()));
	}

	@Test
	public void testRemoteAllRequesterNotOkNoOrganization() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(new User(null, UserRole.REMOTE, "remote"), Stream.empty()));
	}

	@Test
	public void testRemoteAllRequesterNotOkNoUser() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(null, Stream.empty()));
	}

	@Test
	public void testRemoteAllRequesterNotOkLocalOrganization() throws Exception
	{
		assertFalse(remote.isRequesterAuthorized(new User(new Organization().setActive(true), UserRole.LOCAL, "local"),
				Stream.empty()));
	}
}
