package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.jdbc.BinaryDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.HistroyDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationDaoJdbc;
import org.highmed.dsf.fhir.history.AtParameter;
import org.highmed.dsf.fhir.history.History;
import org.highmed.dsf.fhir.history.SinceParameter;
import org.highmed.dsf.fhir.history.user.HistoryUserFilterFactory;
import org.highmed.dsf.fhir.history.user.HistoryUserFilterFactoryImpl;
import org.highmed.dsf.fhir.search.PageAndCount;
import org.hl7.fhir.r4.model.Organization;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import de.rwh.utils.test.LiquibaseTemplateTestClassRule;
import de.rwh.utils.test.LiquibaseTemplateTestRule;

public class HistoryDaoTest extends AbstractDbTest
{
	private static final BasicDataSource adminDataSource = createAdminBasicDataSource();
	private static final BasicDataSource liquibaseDataSource = createLiquibaseDataSource();
	private static final BasicDataSource defaultDataSource = createDefaultDataSource();
	private static final BasicDataSource permanentDeleteDataSource = createPermanentDeleteDataSource();

	@ClassRule
	public static final LiquibaseTemplateTestClassRule liquibaseRule = new LiquibaseTemplateTestClassRule(
			adminDataSource, LiquibaseTemplateTestClassRule.DEFAULT_TEST_DB_NAME,
			AbstractResourceDaoTest.DAO_DB_TEMPLATE_NAME, liquibaseDataSource, CHANGE_LOG_FILE, CHANGE_LOG_PARAMETERS,
			true);

	@AfterClass
	public static void afterClass() throws Exception
	{
		defaultDataSource.close();
		liquibaseDataSource.close();
		adminDataSource.close();
		permanentDeleteDataSource.close();
	}

	@Rule
	public final LiquibaseTemplateTestRule templateRule = new LiquibaseTemplateTestRule(adminDataSource,
			LiquibaseTemplateTestClassRule.DEFAULT_TEST_DB_NAME, AbstractResourceDaoTest.DAO_DB_TEMPLATE_NAME);

	private final FhirContext fhirContext = FhirContext.forR4();
	private final OrganizationDao orgDao = new OrganizationDaoJdbc(defaultDataSource, permanentDeleteDataSource,
			fhirContext);
	private final HistoryDao dao = new HistroyDaoJdbc(defaultDataSource, fhirContext,
			new BinaryDaoJdbc(defaultDataSource, permanentDeleteDataSource, fhirContext));
	private final HistoryUserFilterFactory filterFactory = new HistoryUserFilterFactoryImpl();

	@Test
	public void testReadHistory() throws Exception
	{
		Organization organization = new Organization();
		organization.getMeta().addTag("http://highmed.org/fhir/CodeSystem/read-access-tag", "ALL", null);
		organization.setName("Test Organization");
		organization.addIdentifier().setSystem("http://highmed.org/sid/organization-identifier").setValue("test.org");
		Organization createdOrganization = orgDao.create(organization);

		History history = dao.readHistory(filterFactory.getUserFilters(User.local(createdOrganization)),
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
		organization.getMeta().addTag("http://highmed.org/fhir/CodeSystem/read-access-tag", "ALL", null);
		organization.setName("Test Organization");
		organization.addIdentifier().setSystem("http://highmed.org/sid/organization-identifier").setValue("test.org");
		Organization createdOrganization = orgDao.create(organization);

		History history = dao.readHistory(
				filterFactory.getUserFilter(User.local(createdOrganization), Organization.class),
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
		organization.getMeta().addTag("http://highmed.org/fhir/CodeSystem/read-access-tag", "ALL", null);
		organization.setName("Test Organization");
		organization.addIdentifier().setSystem("http://highmed.org/sid/organization-identifier").setValue("test.org");
		Organization createdOrganization = orgDao.create(organization);

		History history = dao.readHistory(
				filterFactory.getUserFilter(User.local(createdOrganization), Organization.class),
				new PageAndCount(1, 1000), new AtParameter(), new SinceParameter(), Organization.class,
				UUID.fromString(createdOrganization.getIdElement().getIdPart()));

		assertNotNull(history);
		assertEquals(1, history.getTotal());
		assertNotNull(history.getEntries());
		assertEquals(1, history.getEntries().size());
	}
}
