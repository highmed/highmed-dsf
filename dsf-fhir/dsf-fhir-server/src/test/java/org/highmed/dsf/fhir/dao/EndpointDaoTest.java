package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.highmed.dsf.fhir.dao.jdbc.EndpointDaoJdbc;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Endpoint.EndpointStatus;
import org.junit.Test;

public class EndpointDaoTest extends AbstractResourceDaoTest<Endpoint, EndpointDao>
		implements ReadAccessDaoTest<Endpoint>
{
	private static final String name = "Demo Endpoint Name";
	private static final String address = "https://foo.bar/baz";

	public EndpointDaoTest()
	{
		super(Endpoint.class, EndpointDaoJdbc::new);
	}

	@Override
	public Endpoint createResource()
	{
		Endpoint endpoint = new Endpoint();
		endpoint.setName(name);
		return endpoint;
	}

	@Override
	protected void checkCreated(Endpoint resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected Endpoint updateResource(Endpoint resource)
	{
		resource.setAddress(address);
		return resource;
	}

	@Override
	protected void checkUpdates(Endpoint resource)
	{
		assertEquals(address, resource.getAddress());
	}

	@Test
	public void testExistsActiveNotDeletedByAddress() throws Exception
	{
		String address = "http://test/fhir";

		Endpoint e = new Endpoint();
		e.setStatus(EndpointStatus.ACTIVE);
		e.setAddress(address);

		Endpoint created = dao.create(e);
		assertNotNull(created);

		assertTrue(dao.existsActiveNotDeletedByAddress(address));
	}

	@Test
	public void testExistsActiveNotDeletedByAddressNotActive() throws Exception
	{
		String address = "http://test/fhir";

		Endpoint e = new Endpoint();
		e.setStatus(EndpointStatus.OFF);
		e.setAddress(address);

		Endpoint created = dao.create(e);
		assertNotNull(created);

		assertFalse(dao.existsActiveNotDeletedByAddress(address));
	}

	@Test
	public void testExistsActiveNotDeletedByAddressDeleted() throws Exception
	{
		String address = "http://test/fhir";

		Endpoint e = new Endpoint();
		e.setStatus(EndpointStatus.ACTIVE);
		e.setAddress(address);

		Endpoint created = dao.create(e);
		assertNotNull(created);
		dao.delete(UUID.fromString(created.getIdElement().getIdPart()));

		assertFalse(dao.existsActiveNotDeletedByAddress(address));
	}

	@Override
	@Test
	public void testReadAccessTriggerAll() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerAll();
	}

	@Override
	@Test
	public void testReadAccessTriggerLocal() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerLocal();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganization() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganization();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganizationResourceFirst() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganizationResourceFirst();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganization2Organizations1Matching() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganization2Organizations1Matching();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganization2Organizations2Matching() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganization2Organizations2Matching();
	}

	@Override
	@Test
	public void testReadAccessTriggerRole() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRole();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleResourceFirst() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleResourceFirst();
	}

	@Override
	@Test
	public void testReadAccessTriggerRole2Organizations1Matching() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRole2Organizations1Matching();
	}

	@Override
	@Test
	public void testReadAccessTriggerRole2Organizations2Matching() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRole2Organizations2Matching();
	}

	@Override
	@Test
	public void testReadAccessTriggerAllUpdate() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerAllUpdate();
	}

	@Override
	@Test
	public void testReadAccessTriggerLocalUpdate() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerLocalUpdate();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganizationUpdate() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganizationUpdate();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleUpdate() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleUpdate();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleUpdateMemberOrganizationNonActive() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleUpdateMemberOrganizationNonActive();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleUpdateParentOrganizationNonActive() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleUpdateParentOrganizationNonActive();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleUpdateMemberAndParentOrganizationNonActive() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleUpdateMemberAndParentOrganizationNonActive();
	}

	@Override
	@Test
	public void testReadAccessTriggerAllDelete() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerAllDelete();
	}

	@Override
	@Test
	public void testReadAccessTriggerLocalDelete() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerLocalDelete();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganizationDelete() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganizationDelete();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleDelete() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleDelete();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleDeleteMember() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleDeleteMember();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleDeleteParent() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleDeleteParent();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleDeleteMemberAndParent() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleDeleteMemberAndParent();
	}

	@Override
	@Test
	public void testSearchWithUserFilterAfterReadAccessTriggerAllWithLocalUser() throws Exception
	{
		ReadAccessDaoTest.super.testSearchWithUserFilterAfterReadAccessTriggerAllWithLocalUser();
	}

	@Override
	@Test
	public void testSearchWithUserFilterAfterReadAccessTriggerLocalwithLocalUser() throws Exception
	{
		ReadAccessDaoTest.super.testSearchWithUserFilterAfterReadAccessTriggerLocalwithLocalUser();
	}

	@Override
	@Test
	public void testSearchWithUserFilterAfterReadAccessTriggerAllWithRemoteUser() throws Exception
	{
		ReadAccessDaoTest.super.testSearchWithUserFilterAfterReadAccessTriggerAllWithRemoteUser();
	}

	@Override
	@Test
	public void testSearchWithUserFilterAfterReadAccessTriggerLocalWithRemoteUser() throws Exception
	{
		ReadAccessDaoTest.super.testSearchWithUserFilterAfterReadAccessTriggerLocalWithRemoteUser();
	}
}
