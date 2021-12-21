package org.highmed.dsf.fhir.authorization.read;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;

public class ReadAccessHelperTest
{
	private final ReadAccessHelper helper = new ReadAccessHelperImpl();

	@Test
	public void testHasLocal() throws Exception
	{
		var r = new CodeSystem();
		assertFalse(helper.hasLocal(r));
		assertFalse(helper.hasAll(r));

		helper.addLocal(r);
		assertTrue(helper.hasLocal(r));
		assertFalse(helper.hasAll(r));
	}

	@Test
	public void testSetLocal() throws Exception
	{
		var r = new CodeSystem();
		helper.addAll(r);
		assertFalse(helper.hasLocal(r));
		assertTrue(helper.hasAll(r));

		helper.addLocal(r);
		assertTrue(helper.hasLocal(r));
		assertFalse(helper.hasAll(r));
	}

	@Test
	public void testHasLocalViaFile() throws Exception
	{
		try (InputStream in = Files
				.newInputStream(Paths.get("src/test/resources/authorization/read-access/tag_local.xml")))
		{
			var r = FhirContext.forR4().newXmlParser().parseResource(CodeSystem.class, in);

			assertTrue(helper.isValid(r));
			assertTrue(helper.isValid(r, org -> false, role -> false));

			assertTrue(helper.hasLocal(r));
			assertFalse(helper.hasAll(r));
		}
	}

	@Test
	public void testHasOrganization() throws Exception
	{
		var r = new CodeSystem();
		assertFalse(helper.hasLocal(r));
		assertFalse(helper.hasOrganization(r, "organization.com"));
		assertFalse(helper.hasAll(r));

		helper.addOrganization(r, "organization.com");
		assertTrue(helper.hasLocal(r));
		assertTrue(helper.hasOrganization(r, "organization.com"));
		assertFalse(helper.hasAll(r));
	}

	@Test
	public void testHasOrganization2() throws Exception
	{
		var r = new CodeSystem();
		helper.addOrganization(r, "organization.com");
		assertTrue(helper.hasLocal(r));
		assertTrue(helper.hasOrganization(r, "organization.com"));
		assertFalse(helper.hasOrganization(r, "organization2.com"));
		assertFalse(helper.hasAll(r));

		helper.addOrganization(r, "organization2.com");

		assertTrue(helper.hasLocal(r));
		assertTrue(helper.hasOrganization(r, "organization.com"));
		assertTrue(helper.hasOrganization(r, "organization2.com"));
		assertFalse(helper.hasAll(r));
	}

	@Test
	public void testHasOrganizationViaFile() throws Exception
	{
		final String organizationIdentifier = "organization.com";

		try (InputStream in = Files
				.newInputStream(Paths.get("src/test/resources/authorization/read-access/tag_organization.xml")))
		{
			var r = FhirContext.forR4().newXmlParser().parseResource(CodeSystem.class, in);

			assertTrue(helper.isValid(r));
			assertTrue(helper.isValid(r, org -> organizationIdentifier.equals(org.getValue()), role -> false));

			assertTrue(helper.hasLocal(r));
			assertTrue(helper.hasOrganization(r, organizationIdentifier));
			assertFalse(helper.hasAll(r));
		}
	}

	@Test
	public void testHasOrganizationViaResource() throws Exception
	{
		Organization org = new Organization();
		org.addIdentifier().setSystem("http://highmed.org/sid/organization-identifier").setValue("organization.com");

		var r = new CodeSystem();
		assertFalse(helper.hasLocal(r));
		assertFalse(helper.hasOrganization(r, org));
		assertFalse(helper.hasAll(r));

		helper.addOrganization(r, org);
		assertTrue(helper.hasLocal(r));
		assertTrue(helper.hasOrganization(r, org));
		assertFalse(helper.hasAll(r));
	}

	@Test
	public void testHasRole() throws Exception
	{
		final String consortiumIdentifier = "consortium.com";
		final String roleSystem = "role-system";
		final String roleCode = "role-code";

		var r = new CodeSystem();
		assertFalse(helper.hasLocal(r));
		assertFalse(helper.hasRole(r, consortiumIdentifier, roleSystem, roleCode));
		assertFalse(helper.hasAll(r));

		helper.addRole(r, consortiumIdentifier, roleSystem, roleCode);
		assertTrue(helper.hasLocal(r));
		assertTrue(helper.hasRole(r, consortiumIdentifier, roleSystem, roleCode));
		assertFalse(helper.hasAll(r));
	}

	@Test
	public void testHasRoleViaResource() throws Exception
	{
		OrganizationAffiliation affiliation = new OrganizationAffiliation();
		affiliation.getOrganization().getIdentifier().setSystem("http://highmed.org/sid/organization-identifier")
				.setValue("consortium.com");
		affiliation.addCode().addCoding().setSystem("role-system").setCode("role-code");

		var r = new CodeSystem();
		assertFalse(helper.hasLocal(r));
		assertFalse(helper.hasRole(r, affiliation));
		assertFalse(helper.hasAll(r));

		helper.addRole(r, affiliation);
		assertTrue(helper.hasLocal(r));
		assertTrue(helper.hasRole(r, affiliation));
		assertFalse(helper.hasAll(r));
	}

	@Test
	public void testHasRoleViaFile() throws Exception
	{
		final String consortiumIdentifier = "consortium.com";
		final String roleSystem = "http://highmed.org/fhir/CodeSystem/organization-role";
		final String roleCode = "MeDIC";

		try (InputStream in = Files
				.newInputStream(Paths.get("src/test/resources/authorization/read-access/tag_role.xml")))
		{
			var r = FhirContext.forR4().newXmlParser().parseResource(CodeSystem.class, in);

			assertTrue(helper.isValid(r));
			assertTrue(helper.isValid(r, org -> consortiumIdentifier.equals(org.getValue()),
					role -> roleSystem.equals(role.getSystem()) && roleCode.equals(role.getCode())));
			assertTrue(helper.hasRole(r, consortiumIdentifier, roleSystem, roleCode));
		}
	}

	@Test
	public void testHasAll() throws Exception
	{
		var r = new CodeSystem();
		assertFalse(helper.hasAll(r));

		helper.addAll(r);
		assertTrue(helper.hasAll(r));
	}

	@Test
	public void testHasAllViaFile() throws Exception
	{
		try (InputStream in = Files
				.newInputStream(Paths.get("src/test/resources/authorization/read-access/tag_all.xml")))
		{
			var r = FhirContext.forR4().newXmlParser().parseResource(CodeSystem.class, in);

			assertTrue(helper.isValid(r));
			assertTrue(helper.isValid(r, org -> false, role -> false));
			assertTrue(helper.hasAll(r));
		}
	}

	@Test
	public void testLocalValid() throws Exception
	{
		var r = new CodeSystem();
		helper.addLocal(r);

		assertTrue(helper.isValid(r));
	}

	@Test
	public void testOrganizationValid() throws Exception
	{
		var r = new CodeSystem();
		helper.addLocal(r);
		helper.addOrganization(r, "organization.com");

		assertTrue(helper.isValid(r));
	}

	@Test
	public void testOrganizationValidWithTests() throws Exception
	{
		final String organizationIdentifier = "organization.com";

		var r = new CodeSystem();
		helper.addLocal(r);
		helper.addOrganization(r, organizationIdentifier);

		assertTrue(helper.isValid(r, org -> organizationIdentifier.equals(org.getValue()), role -> true));
	}

	@Test
	public void testRoleValid() throws Exception
	{
		var r = new CodeSystem();
		helper.addLocal(r);
		helper.addRole(r, "consortium.com", "role-system", "role-code");

		assertTrue(helper.isValid(r));
	}

	@Test
	public void testRoleValidWithTests() throws Exception
	{
		final String consortiumIdentifier = "consortium.com";
		final String roleSystem = "role-system";
		final String roleCode = "role-code";

		var r = new CodeSystem();
		helper.addLocal(r);
		helper.addRole(r, consortiumIdentifier, roleSystem, roleCode);

		assertTrue(helper.isValid(r, org -> consortiumIdentifier.equals(org.getValue()),
				role -> roleSystem.equals(role.getSystem()) && roleCode.equals(role.getCode())));
	}

	@Test
	public void testAllValid() throws Exception
	{
		var r = new CodeSystem();
		helper.addAll(r);

		assertTrue(helper.isValid(r));
	}

	@Test
	public void testNotValid() throws Exception
	{
		var r = new CodeSystem();
		assertFalse(helper.isValid(r));
	}
}
