package org.highmed.dsf.bpe.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;

import de.rwh.utils.test.LiquibaseTemplateTestClassRule;
import de.rwh.utils.test.LiquibaseTemplateTestRule;

public class AbstractDaoTest extends AbstractDbTest
{
	public static final String DAO_DB_TEMPLATE_NAME = "dao_template";

	protected static final BasicDataSource adminDataSource = createAdminBasicDataSource();
	protected static final BasicDataSource liquibaseDataSource = createLiquibaseDataSource();
	protected static final BasicDataSource defaultDataSource = createDefaultDataSource();
	protected static final BasicDataSource camundaDataSource = createCamundaDataSource();

	@ClassRule
	public static final LiquibaseTemplateTestClassRule liquibaseRule = new LiquibaseTemplateTestClassRule(
			adminDataSource, LiquibaseTemplateTestClassRule.DEFAULT_TEST_DB_NAME, DAO_DB_TEMPLATE_NAME,
			liquibaseDataSource, CHANGE_LOG_FILE, CHANGE_LOG_PARAMETERS, true);

	@BeforeClass
	public static void beforeClass() throws Exception
	{
		defaultDataSource.start();
		liquibaseDataSource.start();
		adminDataSource.start();
		camundaDataSource.start();
	}

	@AfterClass
	public static void afterClass() throws Exception
	{
		defaultDataSource.close();
		liquibaseDataSource.close();
		adminDataSource.close();
		camundaDataSource.close();
	}

	@Rule
	public final LiquibaseTemplateTestRule templateRule = new LiquibaseTemplateTestRule(adminDataSource,
			LiquibaseTemplateTestClassRule.DEFAULT_TEST_DB_NAME, DAO_DB_TEMPLATE_NAME);
}
