package org.highmed.dsf.fhir.dao;

import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.ACTIVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Optional;

import org.highmed.dsf.fhir.dao.jdbc.NamingSystemDaoJdbc;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.NamingSystem.NamingSystemIdentifierType;
import org.junit.Test;

public class NamingSystemDaoTest extends AbstractResourceDaoTest<NamingSystem, NamingSystemDao>
		implements ReadAccessDaoTest<NamingSystem>
{
	private static final String name = "Demo NamingSystem Name";
	private static final String description = "Demo NamingSystem Description";
	private static final String uniqueIdValue = "http://foo.bar/sid/test";

	public NamingSystemDaoTest()
	{
		super(NamingSystem.class, NamingSystemDaoJdbc::new);
	}

	@Override
	public NamingSystem createResource()
	{
		NamingSystem namingSystem = new NamingSystem();
		namingSystem.setName(name);
		return namingSystem;
	}

	@Override
	protected void checkCreated(NamingSystem resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected NamingSystem updateResource(NamingSystem resource)
	{
		resource.setDescription(description);
		return resource;
	}

	@Override
	protected void checkUpdates(NamingSystem resource)
	{
		assertEquals(description, resource.getDescription());
	}

	@Test
	public void testReadByName() throws Exception
	{
		NamingSystem newResource = createResource();
		dao.create(newResource);

		Optional<NamingSystem> readByName = dao.readByName(name);
		assertTrue(readByName.isPresent());
	}

	@Test
	public void testExistsWithUniqueIdUriEntry() throws Exception
	{
		NamingSystem newResource = createResource();
		newResource.setStatus(ACTIVE);
		newResource.addUniqueId().setValue(uniqueIdValue).setType(NamingSystemIdentifierType.URI).addModifierExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-check-logical-reference")
				.setValue(new BooleanType(true));
		dao.create(newResource);

		try (Connection connection = defaultDataSource.getConnection())
		{
			assertTrue(dao.existsWithUniqueIdUriEntry(connection, uniqueIdValue));
		}
	}

	@Test
	public void testExistsWithUniqueIdUriEntryTwoEntries() throws Exception
	{
		NamingSystem newResource = createResource();
		newResource.setStatus(ACTIVE);
		newResource.addUniqueId().setValue(uniqueIdValue).setType(NamingSystemIdentifierType.URI).addModifierExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-check-logical-reference")
				.setValue(new BooleanType(true));
		newResource.addUniqueId().setValue(uniqueIdValue + "foo").setType(NamingSystemIdentifierType.URI);
		dao.create(newResource);

		try (Connection connection = defaultDataSource.getConnection())
		{
			assertTrue(dao.existsWithUniqueIdUriEntry(connection, uniqueIdValue));
		}
	}

	@Test
	public void testExistsWithUniqueIdUriEntryNotExisting() throws Exception
	{
		try (Connection connection = defaultDataSource.getConnection())
		{
			assertFalse(dao.existsWithUniqueIdUriEntry(connection, uniqueIdValue));
		}
	}

	@Test
	public void testExistsWithUniqueIdUriEntryResolvable() throws Exception
	{
		NamingSystem newResource = createResource();
		newResource.setStatus(ACTIVE);
		newResource.addUniqueId().setValue(uniqueIdValue).setType(NamingSystemIdentifierType.URI).addModifierExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-check-logical-reference")
				.setValue(new BooleanType(true));
		dao.create(newResource);

		try (Connection connection = defaultDataSource.getConnection())
		{
			assertTrue(dao.existsWithUniqueIdUriEntryResolvable(connection, uniqueIdValue));
		}
	}

	@Test
	public void testExistsWithUniqueIdUriEntryResolvableSecondUniquIdNotResolvable() throws Exception
	{
		NamingSystem newResource = createResource();
		newResource.setStatus(ACTIVE);
		newResource.addUniqueId().setValue(uniqueIdValue).setType(NamingSystemIdentifierType.URI).addModifierExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-check-logical-reference")
				.setValue(new BooleanType(true));
		newResource.addUniqueId().setValue(uniqueIdValue + "foo").setType(NamingSystemIdentifierType.URI)
				.addModifierExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-check-logical-reference")
				.setValue(new BooleanType(false));
		dao.create(newResource);

		try (Connection connection = defaultDataSource.getConnection())
		{
			assertTrue(dao.existsWithUniqueIdUriEntryResolvable(connection, uniqueIdValue));
		}
	}

	@Test
	public void testExistsWithUniqueIdUriEntryResolvableSecondUniquIdNotResolvableNoExtension() throws Exception
	{
		NamingSystem newResource = createResource();
		newResource.setStatus(ACTIVE);
		newResource.addUniqueId().setValue(uniqueIdValue).setType(NamingSystemIdentifierType.URI).addModifierExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-check-logical-reference")
				.setValue(new BooleanType(true));
		newResource.addUniqueId().setValue(uniqueIdValue + "foo").setType(NamingSystemIdentifierType.URI);
		dao.create(newResource);

		try (Connection connection = defaultDataSource.getConnection())
		{
			assertTrue(dao.existsWithUniqueIdUriEntryResolvable(connection, uniqueIdValue));
		}
	}

	@Test
	public void testExistsWithUniqueIdUriEntryResolvableWithoutUniqueId() throws Exception
	{
		NamingSystem newResource = createResource();
		newResource.setStatus(ACTIVE);
		dao.create(newResource);

		try (Connection connection = defaultDataSource.getConnection())
		{
			assertFalse(dao.existsWithUniqueIdUriEntryResolvable(connection, uniqueIdValue));
		}
	}

	@Test
	public void testExistsWithUniqueIdUriEntryResolvableWithoutModifierExtension() throws Exception
	{
		NamingSystem newResource = createResource();
		newResource.setStatus(ACTIVE);
		newResource.addUniqueId().setValue(uniqueIdValue).setType(NamingSystemIdentifierType.URI);
		dao.create(newResource);

		try (Connection connection = defaultDataSource.getConnection())
		{
			assertFalse(dao.existsWithUniqueIdUriEntryResolvable(connection, uniqueIdValue));
		}
	}

	@Test
	public void testExistsWithUniqueIdUriEntryResolvableWithModifierExtensionOfValueFalse() throws Exception
	{
		NamingSystem newResource = createResource();
		newResource.setStatus(ACTIVE);
		newResource.addUniqueId().setValue(uniqueIdValue).setType(NamingSystemIdentifierType.URI).addModifierExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-check-logical-reference")
				.setValue(new BooleanType(false));
		dao.create(newResource);

		try (Connection connection = defaultDataSource.getConnection())
		{
			assertFalse(dao.existsWithUniqueIdUriEntryResolvable(connection, uniqueIdValue));
		}
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
