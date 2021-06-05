package org.highmed.dsf.fhir.authorization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Collections;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelperImpl;
import org.highmed.dsf.fhir.dao.OrganizationAffiliationDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.junit.Test;

public class CodeSystemAuthorizationRuleTest
{
	private DaoProvider daoProvider = mock(DaoProvider.class);
	private ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
	private OrganizationProvider organizationProvider = mock(OrganizationProvider.class);
	private ReadAccessHelper readAccessHelper = new ReadAccessHelperImpl();

	private CodeSystemAuthorizationRule rule = new CodeSystemAuthorizationRule(daoProvider, "http://server.base/fhir",
			referenceResolver, organizationProvider, readAccessHelper);

	@Test
	public void testReasonReadAllowedForLocalUserByLocalTag() throws Exception
	{
		Organization organization = new Organization();
		organization.setName("Local Org");
		User user = new User(organization, UserRole.LOCAL, "local");

		CodeSystem codeSystem = new CodeSystem();
		readAccessHelper.addLocal(codeSystem);

		Optional<String> reason = rule.reasonReadAllowed(user, codeSystem);

		assertTrue(reason.isPresent());
		assertFalse(reason.get().isBlank());
	}

	@Test
	public void testReasonReadNotAllowedForRemoteUserByLocalTag() throws Exception
	{
		Organization organization = new Organization();
		organization.setName("Remote Org");
		User user = new User(organization, UserRole.REMOTE, "remote");

		CodeSystem codeSystem = new CodeSystem();
		readAccessHelper.addLocal(codeSystem);

		Optional<String> reason = rule.reasonReadAllowed(user, codeSystem);

		assertFalse(reason.isPresent());
	}

	@Test
	public void testReasonReadAllowedForLocalUserByAllTag() throws Exception
	{
		Organization organization = new Organization();
		organization.setName("Local Org");
		User user = new User(organization, UserRole.LOCAL, "local");

		CodeSystem codeSystem = new CodeSystem();
		readAccessHelper.addAll(codeSystem);

		Optional<String> reason = rule.reasonReadAllowed(user, codeSystem);

		assertTrue(reason.isPresent());
		assertFalse(reason.get().isBlank());
	}

	@Test
	public void testReasonReadAllowedForRemoteUserByAllTag() throws Exception
	{
		Organization organization = new Organization();
		organization.setName("Remote Org");
		User user = new User(organization, UserRole.REMOTE, "remote");

		CodeSystem codeSystem = new CodeSystem();
		readAccessHelper.addAll(codeSystem);

		Optional<String> reason = rule.reasonReadAllowed(user, codeSystem);

		assertTrue(reason.isPresent());
		assertFalse(reason.get().isBlank());
	}

	@Test
	public void testReasonReadAllowedForRemoteUserByOrganizationTag() throws Exception
	{
		String organizationIdentifier = "test.org";

		Organization organization = new Organization();
		organization.setName("Remote Org");
		organization.addIdentifier().setSystem(AbstractAuthorizationRule.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue(organizationIdentifier);
		User user = new User(organization, UserRole.REMOTE, "remote");

		CodeSystem codeSystem = new CodeSystem();
		readAccessHelper.addOrganization(codeSystem, organizationIdentifier);

		Optional<String> reason = rule.reasonReadAllowed(user, codeSystem);

		assertTrue(reason.isPresent());
		assertFalse(reason.get().isBlank());
	}

	@Test
	public void testReasonReadAllowedForLocalUserByOrganizationTag() throws Exception
	{
		String organizationIdentifier = "test.org";

		Organization organization = new Organization();
		organization.setName("Local Org");
		organization.addIdentifier().setSystem(AbstractAuthorizationRule.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue(organizationIdentifier);
		User user = new User(organization, UserRole.LOCAL, "local");

		CodeSystem codeSystem = new CodeSystem();
		readAccessHelper.addOrganization(codeSystem, organizationIdentifier);

		Optional<String> reason = rule.reasonReadAllowed(user, codeSystem);

		assertTrue(reason.isPresent());
		assertFalse(reason.get().isBlank());
	}

	@Test
	public void testReasonReadNotAllowedForRemoteUserByOrganizationTag() throws Exception
	{
		String organizationIdentifier = "test.org";

		Organization organization = new Organization();
		organization.setName("Remote Org");
		organization.addIdentifier().setSystem(AbstractAuthorizationRule.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue(organizationIdentifier);
		User user = new User(organization, UserRole.REMOTE, "remote");

		CodeSystem codeSystem = new CodeSystem();
		readAccessHelper.addOrganization(codeSystem, "not-allowed." + organizationIdentifier);

		Optional<String> reason = rule.reasonReadAllowed(user, codeSystem);

		assertFalse(reason.isPresent());
	}

	@Test
	public void testReasonReadNotAllowedForLocalUserByOrganizationTag() throws Exception
	{
		String organizationIdentifier = "test.org";

		Organization organization = new Organization();
		organization.setName("Local Org");
		organization.addIdentifier().setSystem(AbstractAuthorizationRule.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue(organizationIdentifier);
		User user = new User(organization, UserRole.LOCAL, "local");

		CodeSystem codeSystem = new CodeSystem();
		readAccessHelper.addOrganization(codeSystem, "not-allowed." + organizationIdentifier);

		Optional<String> reason = rule.reasonReadAllowed(user, codeSystem);

		assertFalse(reason.isPresent());
	}

	@Test
	public void testReasonReadAllowedForRemoteUserByRoleTag() throws Exception
	{
		String organizationIdentifier = "test.org";
		String consortiumIdentifier = "consortium.org";
		String roleSystem = "http://server.base/fhir/CodeSystem/test";
		String roleCode = "foo";

		Organization organization = new Organization();
		organization.setActive(true);
		organization.setName("Remote Org");
		organization.addIdentifier().setSystem(AbstractAuthorizationRule.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue(organizationIdentifier);
		User user = new User(organization, UserRole.REMOTE, "remote");

		OrganizationAffiliation affiliation = new OrganizationAffiliation();
		affiliation.setActive(true);
		affiliation.addCode().addCoding().setSystem(roleSystem).setCode(roleCode);
		affiliation.getOrganization().setType("Organization").getIdentifier()
				.setSystem(AbstractAuthorizationRule.ORGANIZATION_IDENTIFIER_SYSTEM).setValue(consortiumIdentifier);
		affiliation.getParticipatingOrganization().setType("Organization").getIdentifier()
				.setSystem(AbstractAuthorizationRule.ORGANIZATION_IDENTIFIER_SYSTEM).setValue(organizationIdentifier);

		CodeSystem codeSystem = new CodeSystem();
		readAccessHelper.addRole(codeSystem, consortiumIdentifier, roleSystem, roleCode);

		when(daoProvider.newReadOnlyAutoCommitTransaction()).thenReturn(mock(Connection.class));
		OrganizationAffiliationDao dao = mock(OrganizationAffiliationDao.class);
		when(daoProvider.getOrganizationAffiliationDao()).thenReturn(dao);
		when(dao.readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
				isNotNull(), eq(organizationIdentifier))).thenReturn(Collections.singletonList(affiliation));

		Optional<String> reason = rule.reasonReadAllowed(user, codeSystem);

		assertTrue(reason.isPresent());
		assertFalse(reason.get().isBlank());

		verify(dao).readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
				isNotNull(), eq(organizationIdentifier));
		verify(daoProvider).getOrganizationAffiliationDao();
	}

	@Test
	public void testReasonReadAllowedForLocalUserByRoleTag() throws Exception
	{
		String organizationIdentifier = "test.org";
		String consortiumIdentifier = "consortium.org";
		String roleSystem = "http://server.base/fhir/CodeSystem/test";
		String roleCode = "foo";

		Organization organization = new Organization();
		organization.setActive(true);
		organization.setName("Remote Org");
		organization.addIdentifier().setSystem(AbstractAuthorizationRule.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue(organizationIdentifier);
		User user = new User(organization, UserRole.LOCAL, "local");

		OrganizationAffiliation affiliation = new OrganizationAffiliation();
		affiliation.setActive(true);
		affiliation.addCode().addCoding().setSystem(roleSystem).setCode(roleCode);
		affiliation.getOrganization().setType("Organization").getIdentifier()
				.setSystem(AbstractAuthorizationRule.ORGANIZATION_IDENTIFIER_SYSTEM).setValue(consortiumIdentifier);
		affiliation.getParticipatingOrganization().setType("Organization").getIdentifier()
				.setSystem(AbstractAuthorizationRule.ORGANIZATION_IDENTIFIER_SYSTEM).setValue(organizationIdentifier);

		CodeSystem codeSystem = new CodeSystem();
		readAccessHelper.addRole(codeSystem, consortiumIdentifier, roleSystem, roleCode);

		when(daoProvider.newReadOnlyAutoCommitTransaction()).thenReturn(mock(Connection.class));
		OrganizationAffiliationDao dao = mock(OrganizationAffiliationDao.class);
		when(daoProvider.getOrganizationAffiliationDao()).thenReturn(dao);
		when(dao.readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
				isNotNull(), eq(organizationIdentifier))).thenReturn(Collections.singletonList(affiliation));

		Optional<String> reason = rule.reasonReadAllowed(user, codeSystem);

		assertTrue(reason.isPresent());
		assertFalse(reason.get().isBlank());

		verify(dao).readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
				isNotNull(), eq(organizationIdentifier));
		verify(daoProvider).getOrganizationAffiliationDao();
	}

	@Test
	public void testReasonReadNotAllowedForRemoteUserByRoleTag() throws Exception
	{
		String organizationIdentifier = "test.org";
		String consortiumIdentifier = "consortium.org";
		String roleSystem = "http://server.base/fhir/CodeSystem/test";
		String roleCode = "foo";

		Organization organization = new Organization();
		organization.setActive(true);
		organization.setName("Remote Org");
		organization.addIdentifier().setSystem(AbstractAuthorizationRule.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue(organizationIdentifier);
		User user = new User(organization, UserRole.REMOTE, "remote");

		CodeSystem codeSystem = new CodeSystem();
		readAccessHelper.addRole(codeSystem, consortiumIdentifier, roleSystem, roleCode);

		when(daoProvider.newReadOnlyAutoCommitTransaction()).thenReturn(mock(Connection.class));
		OrganizationAffiliationDao dao = mock(OrganizationAffiliationDao.class);
		when(daoProvider.getOrganizationAffiliationDao()).thenReturn(dao);
		when(dao.readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
				isNotNull(), eq(organizationIdentifier))).thenReturn(Collections.emptyList());

		Optional<String> reason = rule.reasonReadAllowed(user, codeSystem);

		assertFalse(reason.isPresent());

		verify(dao).readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
				isNotNull(), eq(organizationIdentifier));
		verify(daoProvider).getOrganizationAffiliationDao();
	}

	@Test
	public void testReasonReadNotAllowedForLocalUserByRoleTag() throws Exception
	{
		String organizationIdentifier = "test.org";
		String consortiumIdentifier = "consortium.org";
		String roleSystem = "http://server.base/fhir/CodeSystem/test";
		String roleCode = "foo";

		Organization organization = new Organization();
		organization.setActive(true);
		organization.setName("Remote Org");
		organization.addIdentifier().setSystem(AbstractAuthorizationRule.ORGANIZATION_IDENTIFIER_SYSTEM)
				.setValue(organizationIdentifier);
		User user = new User(organization, UserRole.LOCAL, "local");

		CodeSystem codeSystem = new CodeSystem();
		readAccessHelper.addRole(codeSystem, consortiumIdentifier, roleSystem, roleCode);

		when(daoProvider.newReadOnlyAutoCommitTransaction()).thenReturn(mock(Connection.class));
		OrganizationAffiliationDao dao = mock(OrganizationAffiliationDao.class);
		when(daoProvider.getOrganizationAffiliationDao()).thenReturn(dao);
		when(dao.readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
				isNotNull(), eq(organizationIdentifier))).thenReturn(Collections.emptyList());

		Optional<String> reason = rule.reasonReadAllowed(user, codeSystem);

		assertFalse(reason.isPresent());

		verify(dao).readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
				isNotNull(), eq(organizationIdentifier));
		verify(daoProvider).getOrganizationAffiliationDao();
	}
}
