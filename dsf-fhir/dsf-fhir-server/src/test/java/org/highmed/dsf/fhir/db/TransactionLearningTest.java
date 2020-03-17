package org.highmed.dsf.fhir.db;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.highmed.dsf.fhir.test.FhirEmbeddedPostgresWithLiquibase;
import org.highmed.dsf.fhir.test.TestSuiteIntegrationTests;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.postgresql.util.PGobject;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import de.rwh.utils.test.Database;

public class TransactionLearningTest
{
	@ClassRule
	public static final FhirEmbeddedPostgresWithLiquibase template = new FhirEmbeddedPostgresWithLiquibase(
			TestSuiteIntegrationTests.template);

	@Rule
	public final Database database = new Database(template);

	private final FhirContext context = FhirContext.forR4();

	@Test
	public void testTransactionsCommitTwice() throws Exception
	{
		try (Connection connection = database.getDataSource().getConnection())
		{
			connection.setAutoCommit(false);
			connection.setReadOnly(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

			for (int i = 1; i <= 2; i++)
				try (PreparedStatement statement = connection
						.prepareStatement("INSERT INTO patients (patient_id, patient) VALUES (?, ?)"))
				{
					UUID id = UUID.randomUUID();
					Patient patient = new Patient();
					patient.setIdElement(new IdType("Patient", id.toString()));

					statement.setObject(1, uuidToPgObject(id));
					statement.setObject(2, resourceToPgObject(patient));

					statement.execute();
				}

			connection.commit();

			try (PreparedStatement statement = connection
					.prepareStatement("INSERT INTO patients (patient_id, patient) VALUES (?, ?)"))
			{
				UUID id = UUID.randomUUID();
				Patient patient = new Patient();
				patient.setIdElement(new IdType("Patient", id.toString()));

				statement.setObject(1, uuidToPgObject(id));
				statement.setObject(2, resourceToPgObject(patient));

				statement.execute();
			}

			connection.commit();
		}

		try (Connection connection = database.getDataSource().getConnection())
		{
			try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM patients");
					ResultSet result = statement.executeQuery())
			{
				assertTrue(result.next());
				assertEquals(3, result.getInt(1));
			}
		}
	}

	@Test
	public void testTransactionsCommitRollback() throws Exception
	{
		try (Connection connection = database.getDataSource().getConnection())
		{
			connection.setAutoCommit(false);
			connection.setReadOnly(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

			for (int i = 1; i <= 2; i++)
				try (PreparedStatement statement = connection
						.prepareStatement("INSERT INTO patients (patient_id, patient) VALUES (?, ?)"))
				{
					UUID id = UUID.randomUUID();
					Patient patient = new Patient();
					patient.setIdElement(new IdType("Patient", id.toString()));

					statement.setObject(1, uuidToPgObject(id));
					statement.setObject(2, resourceToPgObject(patient));

					statement.execute();
				}

			connection.commit();

			try (PreparedStatement statement = connection
					.prepareStatement("INSERT INTO patients (patient_id, patient) VALUES (?, ?)"))
			{
				UUID id = UUID.randomUUID();
				Patient patient = new Patient();
				patient.setIdElement(new IdType("Patient", id.toString()));

				statement.setObject(1, uuidToPgObject(id));
				statement.setObject(2, resourceToPgObject(patient));

				statement.execute();
			}

			// checking if consecutive roll backs work without commit in between
			connection.rollback();
			connection.rollback();
		}

		try (Connection connection = database.getDataSource().getConnection())
		{
			try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM patients");
					ResultSet result = statement.executeQuery())
			{
				assertTrue(result.next());
				assertEquals(2, result.getInt(1));
			}
		}
	}

	private final PGobject resourceToPgObject(Resource resource)
	{
		if (resource == null)
			return null;

		try
		{
			PGobject o = new PGobject();
			o.setType("JSONB");
			o.setValue(context.newJsonParser().encodeResourceToString(resource));
			return o;
		}
		catch (DataFormatException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	private final PGobject uuidToPgObject(UUID uuid)
	{
		if (uuid == null)
			return null;

		try
		{
			PGobject o = new PGobject();
			o.setType("UUID");
			o.setValue(uuid.toString());
			return o;
		}
		catch (DataFormatException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
