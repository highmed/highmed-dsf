package org.highmed.dsf.fhir.test;

import java.util.Map;

import de.rwh.utils.test.EmbeddedPostgresWithLiquibase;

public class FhirEmbeddedPostgresWithLiquibase extends EmbeddedPostgresWithLiquibase
{
	private static final String CHANGE_LOG_FILE = "db/db.changelog.xml";
	private static final String DATABASE_USER_GROUP = "server_user_group";

	private static final String DATABASE_NAME = "test_fhir_demo";
	private static final String DATABASE_USER = "server_user";
	private static final String DATABASE_PASSWORD = "server_user_password";

	private static final Map<String, String> CHANGE_LOG_PARAMETERS = Map.of("db.liquibase_user", LIQUIBASE_USER,
			"db.server_users_group", DATABASE_USER_GROUP, "db.server_user", DATABASE_USER, "db.server_user_password",
			DATABASE_PASSWORD);

	public FhirEmbeddedPostgresWithLiquibase()
	{
		super(CHANGE_LOG_FILE, CHANGE_LOG_PARAMETERS, DATABASE_NAME, DATABASE_USER, DATABASE_PASSWORD);
	}

	public FhirEmbeddedPostgresWithLiquibase(FhirEmbeddedPostgresWithLiquibase parent)
	{
		super(CHANGE_LOG_FILE, CHANGE_LOG_PARAMETERS, DATABASE_NAME, DATABASE_USER, DATABASE_PASSWORD, parent);
	}
}
