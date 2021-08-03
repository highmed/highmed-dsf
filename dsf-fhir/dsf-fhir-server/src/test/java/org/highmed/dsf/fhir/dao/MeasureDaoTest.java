package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.highmed.dsf.fhir.dao.jdbc.MeasureDaoJdbc;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Test;

public class MeasureDaoTest extends AbstractResourceDaoTest<Measure, MeasureDao>
		implements ReadByUrlDaoTest<Measure>, ReadAccessDaoTest<Measure>
{
	private static final String name = "Demo Measure";
	private static final String description = "Demo Measure Description";

	public MeasureDaoTest()
	{
		super(Measure.class, MeasureDaoJdbc::new);
	}

	@Override
	public Measure createResource()
	{
		Measure measure = new Measure();
		measure.setName(name);
		return measure;
	}

	@Override
	protected void checkCreated(Measure resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected Measure updateResource(Measure resource)
	{
		resource.setDescription(description);
		return resource;
	}

	@Override
	protected void checkUpdates(Measure resource)
	{
		assertEquals(description, resource.getDescription());
	}

	@Override
	public Measure createResourceWithUrlAndVersion()
	{
		Measure resource = createResource();
		resource.setUrl(getUrl());
		resource.setVersion(getVersion());
		return resource;
	}

	@Override
	public String getUrl()
	{
		return "http://test.com/fhir/Measure/test-measure";
	}

	@Override
	public String getVersion()
	{
		return "0.5.0";
	}

	@Override
	public ReadByUrlDao<Measure> readByUrlDao()
	{
		return getDao();
	}

	@Override
	@Test
	public void testReadByUrlAndVersionWithUrl1() throws Exception
	{
		ReadByUrlDaoTest.super.testReadByUrlAndVersionWithUrl1();
	}

	@Override
	@Test
	public void testReadByUrlAndVersionWithUrlAndVersion1() throws Exception
	{
		ReadByUrlDaoTest.super.testReadByUrlAndVersionWithUrlAndVersion1();
	}

	@Override
	@Test
	public void testReadByUrlAndVersionWithUrl2() throws Exception
	{
		ReadByUrlDaoTest.super.testReadByUrlAndVersionWithUrl2();
	}

	@Override
	@Test
	public void testReadByUrlAndVersionWithUrlAndVersion2() throws Exception
	{
		ReadByUrlDaoTest.super.testReadByUrlAndVersionWithUrlAndVersion2();
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
