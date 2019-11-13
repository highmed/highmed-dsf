package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionCommandList implements CommandList
{
	private static final Logger logger = LoggerFactory.getLogger(TransactionCommandList.class);

	private final DataSource dataSource;
	private final ExceptionHandler exceptionHandler;
	private final TransactionEventManager eventManager;

	private final List<Command> commands = new ArrayList<>();
	private final boolean hasModifyingCommand;

	public TransactionCommandList(DataSource dataSource, ExceptionHandler exceptionHandler, List<Command> commands,
			TransactionEventManager eventManager)
	{
		this.dataSource = dataSource;
		this.exceptionHandler = exceptionHandler;
		this.eventManager = eventManager;

		if (commands != null)
			this.commands.addAll(commands);

		Collections.sort(this.commands,
				Comparator.comparing(Command::getTransactionPriority).thenComparing(Command::getIndex));

		hasModifyingCommand = commands.stream()
				.anyMatch(c -> c instanceof CreateCommand || c instanceof UpdateCommand || c instanceof DeleteCommand);
	}

	@Override
	public Bundle execute() throws WebApplicationException
	{
		Map<Integer, BundleEntryComponent> results = new HashMap<>((int) ((commands.size() / 0.75) + 1));
		try
		{
			Map<String, IdType> idTranslationTable = new HashMap<>();
			for (Command c : commands)
			{
				try
				{
					logger.debug("Running pre-execute of command {} for entry at index {}", c.getClass().getName(),
							c.getIndex());
					c.preExecute(idTranslationTable);
				}
				catch (Exception e)
				{
					logger.warn("Error while running pre-execute of command " + c.getClass().getSimpleName()
							+ " for entry at index " + c.getIndex() + ", abborting transaction", e);

					throw e;
				}
			}

			try (Connection connection = dataSource.getConnection())
			{
				if (hasModifyingCommand)
				{
					connection.setReadOnly(false);
					connection.setAutoCommit(false);
					connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
				}

				for (Command c : commands)
				{
					try
					{
						logger.debug("Running execute of command {} for entry at index {}", c.getClass().getName(),
								c.getIndex());
						c.execute(idTranslationTable, connection);
					}
					catch (Exception e)
					{
						logger.warn("Error while executing command " + c.getClass().getSimpleName()
								+ " for entry at index " + c.getIndex() + ", rolling back transaction", e);

						if (hasModifyingCommand)
						{
							logger.debug("Rolling back DB transaction");
							connection.rollback();
						}

						throw e;
					}
				}

				for (Command c : commands)
				{
					try
					{
						logger.debug("Running post-execute of command {} for entry at index {}", c.getClass().getName(),
								c.getIndex());
						results.putIfAbsent(c.getIndex(), c.postExecute(connection));
					}
					catch (Exception e)
					{
						logger.warn("Error while running post-execute of command " + c.getClass().getSimpleName()
								+ " for entry at index " + c.getIndex() + ", rolling back transaction", e);

						if (hasModifyingCommand)
						{
							logger.debug("Rolling back DB transaction");
							connection.rollback();
						}

						throw e;
					}
				}

				if (hasModifyingCommand)
				{
					logger.debug("Commiting DB transaction");
					connection.commit();
				}
			}

			try
			{
				logger.debug("Commiting events");
				eventManager.commitEvents();
			}
			catch (Exception e)
			{
				logger.warn("Error while handling events", e);
			}

			Bundle result = new Bundle();
			result.setType(BundleType.TRANSACTIONRESPONSE);
			results.entrySet().stream().sorted(Comparator.comparing(Entry::getKey)).map(Entry::getValue)
					.forEach(result::addEntry);

			return result;
		}
		catch (WebApplicationException e)
		{
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity(e.getResponse().getEntity()).build());
		}
		catch (Exception e)
		{
			throw exceptionHandler.internalServerErrorBundleTransaction(e);
		}
	}
}
