package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.highmed.dsf.fhir.help.ExceptionHandler;
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

	private final List<Command> commands = new ArrayList<>();

	public BatchCommandList(DataSource dataSource, ExceptionHandler exceptionHandler, List<Command> commands)
	{
		this.dataSource = dataSource;
		this.exceptionHandler = exceptionHandler;

		if (commands != null)
			this.commands.addAll(commands);
	}

	@Override
	public Bundle execute() throws WebApplicationException
	{
		try (Connection connection = dataSource.getConnection())
		{
			if (commands.stream().anyMatch(
					c -> c instanceof CreateCommand || c instanceof UpdateCommand || c instanceof DeleteCommand))
				connection.setReadOnly(false);

			Map<Integer, Exception> caughtExceptions = new HashMap<Integer, Exception>(
					(int) (commands.size() / 0.75) + 1);
			Map<String, IdType> idTranslationTable = new HashMap<>();

			commands.forEach(preExecute(idTranslationTable, caughtExceptions));

			IntStream.range(0, commands.size()).filter(index -> !caughtExceptions.containsKey(index))
					.mapToObj(index -> commands.get(index))
					.forEach(execute(idTranslationTable, connection, caughtExceptions));

			Map<Integer, BundleEntryComponent> results = new HashMap<>((int) ((commands.size() / 0.75) + 1));

			IntStream.range(0, commands.size()).filter(index -> !caughtExceptions.containsKey(index))
					.mapToObj(index -> commands.get(index)).forEach(postExecute(connection, caughtExceptions, results));

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

	private Consumer<Command> preExecute(Map<String, IdType> idTranslationTable,
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
					command.preExecute(idTranslationTable);
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
				logger.warn("Error while running pre-execute of command " + command.getClass().getName()
						+ " for entry at index " + command.getIndex(), e);
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
					command.execute(Collections.unmodifiableMap(idTranslationTable), connection);
				}
				else
				{
					logger.info("Skipping execute of command {} for entry at index {}, caught exception {}",
							command.getClass().getName(), command.getIndex(),
							caughtExceptions.get(command.getIndex()).getClass().getName() + ": "
									+ caughtExceptions.get(command.getIndex()).getMessage());
				}
			}
			catch (Exception e)
			{
				logger.warn("Error while executing command " + command.getClass().getName() + " for entry at index "
						+ command.getIndex(), e);
				caughtExceptions.put(command.getIndex(), e);
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
					results.put(command.getIndex(), command.postExecute(connection));
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
				logger.warn("Error while running post-execute of command " + command.getClass().getName()
						+ " for entry at index " + command.getIndex(), e);
				caughtExceptions.put(command.getIndex(), e);
			}
		};
	}
}
