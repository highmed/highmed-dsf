package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.dao.jdbc.BinaryDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.HistroyDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationDaoJdbc;
import org.highmed.dsf.fhir.history.AtParameter;
import org.highmed.dsf.fhir.history.History;
import org.highmed.dsf.fhir.history.SinceParameter;
import org.highmed.dsf.fhir.history.user.HistoryUserFilterFactory;
import org.highmed.dsf.fhir.history.user.HistoryUserFilterFactoryImpl;
import org.highmed.dsf.fhir.search.PageAndCount;
import org.highmed.dsf.fhir.test.FhirEmbeddedPostgresWithLiquibase;
import org.highmed.dsf.fhir.test.TestSuiteDbTests;
import org.hl7.fhir.r4.model.Organization;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import de.rwh.utils.test.Database;

public class HistoryDaoTest
{
	@ClassRule
	public static final FhirEmbeddedPostgresWithLiquibase template = new FhirEmbeddedPostgresWithLiquibase(
			TestSuiteDbTests.template);

	@Rule
	public final Database database = new Database(template);

	private final FhirContext fhirContext = FhirContext.forR4();
	private final OrganizationDao orgDao = new OrganizationDaoJdbc(database.getDataSource(), fhirContext);
	private final HistoryDao dao = new HistroyDaoJdbc(database.getDataSource(), fhirContext,
			new BinaryDaoJdbc(database.getDataSource(), fhirContext));
	private final HistoryUserFilterFactory filterFactory = new HistoryUserFilterFactoryImpl();

	@Test
	public void testReadHistory() throws Exception
	{
		Organization organization = new Organization();
		organization.getMeta().addTag("http://highmed.org/fhir/CodeSystem/authorization-role", "REMOTE", null);
		organization.setName("Test Organization");
		Organization createdOrganization = orgDao.create(organization);

		History history = dao.readHistory(filterFactory.getUserFilters(new User(createdOrganization, UserRole.LOCAL)),
				new PageAndCount(1, 1000), new AtParameter(), new SinceParameter());
		assertNotNull(history);
		assertEquals(1, history.getTotal());
		assertNotNull(history.getEntries());
		assertEquals(1, history.getEntries().size());
	}

	@Test
	public void testReadHistoryOrganization() throws Exception
	{
		Organization organization = new Organization();
		organization.getMeta().addTag("http://highmed.org/fhir/CodeSystem/authorization-role", "REMOTE", null);
		organization.setName("Test Organization");
		Organization createdOrganization = orgDao.create(organization);

		History history = dao.readHistory(
				filterFactory.getUserFilter(new User(createdOrganization, UserRole.LOCAL), Organization.class),
				new PageAndCount(1, 1000), new AtParameter(), new SinceParameter(), Organization.class);
		assertNotNull(history);
		assertEquals(1, history.getTotal());
		assertNotNull(history.getEntries());
		assertEquals(1, history.getEntries().size());
	}

	@Test
	public void testReadHistoryOrganizationWithId() throws Exception
	{
		Organization organization = new Organization();
		organization.getMeta().addTag("http://highmed.org/fhir/CodeSystem/authorization-role", "REMOTE", null);
		organization.setName("Test Organization");
		Organization createdOrganization = orgDao.create(organization);

		History history = dao.readHistory(
				filterFactory.getUserFilter(new User(createdOrganization, UserRole.LOCAL), Organization.class),
				new PageAndCount(1, 1000), new AtParameter(), new SinceParameter(), Organization.class,
				UUID.fromString(createdOrganization.getIdElement().getIdPart()));

		assertNotNull(history);
		assertEquals(1, history.getTotal());
		assertNotNull(history.getEntries());
		assertEquals(1, history.getEntries().size());
	}
}
