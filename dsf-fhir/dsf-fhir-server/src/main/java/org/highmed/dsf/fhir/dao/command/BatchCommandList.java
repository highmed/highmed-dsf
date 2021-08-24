package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;

import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.highmed.dsf.fhir.event.EventHandler;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.validation.SnapshotGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchCommandList implements CommandList
{
	private static final Logger logger = LoggerFactory.getLogger(BatchCommandList.class);

	private final DataSource dataSource;
	private final ExceptionHandler exceptionHandler;
	private final ValidationHelper validationHelper;
	private final SnapshotGenerator snapshotGenerator;
	private final EventHandler eventHandler;

	private final List<Command> commands = new ArrayList<>();

	public BatchCommandList(DataSource dataSource, ExceptionHandler exceptionHandler, ValidationHelper validationHelper,
			SnapshotGenerator snapshotGenerator, EventHandler eventHandler, List<Command> commands)
	{
		this.dataSource = dataSource;
		this.exceptionHandler = exceptionHandler;
		this.validationHelper = validationHelper;
		this.snapshotGenerator = snapshotGenerator;
		this.eventHandler = eventHandler;

		if (commands != null)
			this.commands.addAll(commands);
	}

	private boolean hasModifyingCommands()
	{
		return commands.stream()
				.anyMatch(c -> c instanceof CreateCommand || c instanceof UpdateCommand || c instanceof DeleteCommand);
	}

	@Override
	public Bundle execute() throws WebApplicationException
	{
		try (Connection connection = dataSource.getConnection())
		{
			boolean initialReadOnly = connection.isReadOnly();
			boolean initialAutoCommit = connection.getAutoCommit();
			int initialTransactionIsolationLevel = connection.getTransactionIsolation();
			logger.debug(
					"Running batch with DB connection setting: read-only {}, auto-commit {}, transaction-isolation-level {}",
					initialReadOnly, initialAutoCommit,
					getTransactionIsolationLevelString(initialTransactionIsolationLevel));

			Map<Integer, Exception> caughtExceptions = new HashMap<Integer, Exception>(
					(int) (commands.size() / 0.75) + 1);
			Map<String, IdType> idTranslationTable = new HashMap<>();

			if (hasModifyingCommands())
			{
				logger.debug(
						"Elevating DB connection setting to: read-only {}, auto-commit {}, transaction-isolation-level {}",
						false, false, getTransactionIsolationLevelString(Connection.TRANSACTION_REPEATABLE_READ));

				connection.setReadOnly(false);
				connection.setAutoCommit(false);
				connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			}

			commands.forEach(preExecute(idTranslationTable, connection, caughtExceptions));

			commands.forEach(execute(idTranslationTable, connection, caughtExceptions));

			if (hasModifyingCommands())
			{
				logger.debug(
						"Reseting DB connection setting to: read-only {}, auto-commit {}, transaction-isolation-level {}",
						initialReadOnly, initialAutoCommit,
						getTransactionIsolationLevelString(initialTransactionIsolationLevel));

				connection.setReadOnly(initialReadOnly);
				connection.setAutoCommit(initialAutoCommit);
				connection.setTransactionIsolation(initialTransactionIsolationLevel);
			}

			Map<Integer, BundleEntryComponent> results = new HashMap<>((int) ((commands.size() / 0.75) + 1));

			commands.forEach(postExecute(connection, caughtExceptions, results));

			Bundle result = new Bundle();
			result.setType(BundleType.BATCHRESPONSE);

			caughtExceptions.forEach((k, v) -> results.put(k, toEntry(v)));
			results.entrySet().stream().sorted(Comparator.comparing(Entry::getKey)).map(Entry::getValue)
					.forEach(result::addEntry);

			return result;
		}
		catch (Exception e)
		{
			throw exceptionHandler.internalServerErrorBundleTransaction(e);
		}
	}

	private String getTransactionIsolationLevelString(int level)
	{
		switch (level)
		{
			case Connection.TRANSACTION_NONE:
				return "NONE";
			case Connection.TRANSACTION_READ_UNCOMMITTED:
				return "READ_UNCOMMITTED";
			case Connection.TRANSACTION_READ_COMMITTED:
				return "READ_COMMITTED";
			case Connection.TRANSACTION_REPEATABLE_READ:
				return "REPEATABLE_READ";
			case Connection.TRANSACTION_SERIALIZABLE:
				return "SERIALIZABLE";

			default:
				return "?";
		}
	}

	private BundleEntryComponent toEntry(Exception exception)
	{
		var entry = new BundleEntryComponent();
		var response = entry.getResponse();

		if (!(exception instanceof WebApplicationException)
				|| !(((WebApplicationException) exception).getResponse().getEntity() instanceof OperationOutcome))
		{
			exception = exceptionHandler.internalServerErrorBundleBatch(exception);
		}

		Response httpResponse = ((WebApplicationException) exception).getResponse();
		response.setStatus(
				httpResponse.getStatusInfo().getStatusCode() + " " + httpResponse.getStatusInfo().getReasonPhrase());
		response.setOutcome((OperationOutcome) httpResponse.getEntity());

		return entry;
	}

	private Consumer<Command> preExecute(Map<String, IdType> idTranslationTable, Connection connection,
			Map<Integer, Exception> caughtExceptions)
	{
		return command ->
		{
			try
			{
				if (!caughtExceptions.containsKey(command.getIndex()))
				{
					logger.debug("Running pre-execute of command {} for entry at index {}",
							command.getClass().getName(), command.getIndex());
					command.preExecute(idTranslationTable, connection, validationHelper, snapshotGenerator);
				}
				else
				{
					logger.info("Skipping pre-execute of command {} for entry at index {}, caught exception {}",
							command.getClass().getName(), command.getIndex(),
							caughtExceptions.get(command.getIndex()).getClass().getName() + ": "
									+ caughtExceptions.get(command.getIndex()).getMessage());
				}
			}
			catch (Exception e)
			{
				logger.warn("Error while running pre-execute of command {} for entry at index {}: {}",
						command.getClass().getName(), command.getIndex(), e.getMessage());
				caughtExceptions.put(command.getIndex(), e);
			}
		};
	}

	private Consumer<Command> execute(Map<String, IdType> idTranslationTable, Connection connection,
			Map<Integer, Exception> caughtExceptions)
	{
		return command ->
		{
			try
			{
				if (!caughtExceptions.containsKey(command.getIndex()))
				{
					logger.debug("Running execute of command {} for entry at index {}", command.getClass().getName(),
							command.getIndex());
					command.execute(idTranslationTable, connection, validationHelper, snapshotGenerator);
				}
				else
				{
					logger.info("Skipping execute of command {} for entry at index {}, caught exception {}",
							command.getClass().getName(), command.getIndex(),
							caughtExceptions.get(command.getIndex()).getClass().getName() + ": "
									+ caughtExceptions.get(command.getIndex()).getMessage());
				}

				if (!connection.getAutoCommit())
					connection.commit();
			}
			catch (Exception e)
			{
				logger.warn("Error while executing command {}, rolling back transaction for entry at index {}: {}",
						command.getClass().getName(), command.getIndex(), e.getMessage());
				caughtExceptions.put(command.getIndex(), e);

				try
				{
					if (!connection.getAutoCommit())
						connection.rollback();
				}
				catch (SQLException e1)
				{
					logger.warn(
							"Error while executing command {}, error while rolling back transaction for entry at index {}: {}",
							command.getClass().getName(), command.getIndex(), e1.getMessage());
					caughtExceptions.put(command.getIndex(), e1);
				}
			}
		};
	}

	private Consumer<Command> postExecute(Connection connection, Map<Integer, Exception> caughtExceptions,
			Map<Integer, BundleEntryComponent> results)
	{
		return command ->
		{
			try
			{
				if (!caughtExceptions.containsKey(command.getIndex()))
				{
					logger.debug("Running post-execute of command {} for entry at index {}",
							command.getClass().getName(), command.getIndex());

					Optional<BundleEntryComponent> optResult = command.postExecute(connection, eventHandler);
					optResult.ifPresent(result -> results.put(command.getIndex(), result));
				}
				else
				{
					logger.info("Skipping post-execute of command {} for entry at index {}, caught exception {}",
							command.getClass().getName(), command.getIndex(),
							caughtExceptions.get(command.getIndex()).getClass().getName() + ": "
									+ caughtExceptions.get(command.getIndex()).getMessage());
				}
			}
			catch (Exception e)
			{
				logger.warn("Error while running post-execute of command {} for entry at index {}: {}",
						command.getClass().getName(), command.getIndex(), e.getMessage());
				caughtExceptions.put(command.getIndex(), e);
			}
		};
	}
}
