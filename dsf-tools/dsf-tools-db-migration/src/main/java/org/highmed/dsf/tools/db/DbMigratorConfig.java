package org.highmed.dsf.tools.db;

import java.util.Map;

public interface DbMigratorConfig
{
	String getDbUrl();

	String getDbLiquibaseUsername();

	char[] getDbLiquibasePassword();

	Map<String, String> getChangeLogParameters();
}
