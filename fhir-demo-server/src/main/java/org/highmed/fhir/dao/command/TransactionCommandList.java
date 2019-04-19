package org.highmed.fhir.dao.command;

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

import org.highmed.fhir.help.ExceptionHandler;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionCommandList implements CommandList
{
	private static final Logger logger = LoggerFactory.getLogger(TransactionCommandList.class);

	private final DataSource dataSource;
	private final ExceptionHandler exceptionHandler;

	private final List<Command> commands = new ArrayList<>();

	public TransactionCommandList(DataSource dataSource, ExceptionHandler exceptionHandler, List<Command> commands)
	{
		this.dataSource = dataSource;
		this.exceptionHandler = exceptionHandler;

		if (commands != null)
			this.commands.addAll(commands);

		Collections.sort(this.commands,
				Comparator.comparing(Command::getTransactionPriority).thenComparing(Command::getIndex));
	}

	@Override
	public Bundle execute() throws WebApplicationException
	{
		try (Connection connection = dataSource.getConnection())
		{
			if (commands.stream().anyMatch(
					c -> c instanceof CreateCommand || c instanceof UpdateCommand || c instanceof DeleteCommand))
				connection.setReadOnly(false);

			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

			for (Command c : commands)
			{
				try
				{
					logger.debug("Running pre-execute of command {}, for entry at index {}", c.getClass().getName(),
							c.getIndex());
					c.preExecute(connection);
				}
				catch (Exception e)
				{
					logger.warn("Error while running pre-execute of command " + c.getClass().getSimpleName()
							+ ", for entry at index " + c.getIndex() + ", rolling back transaction", e);

					connection.rollback();
					throw e;
				}
			}

			for (Command c : commands)
			{
				try
				{
					logger.debug("Running execute of command {}, for entry at index {}", c.getClass().getName(),
							c.getIndex());
					c.execute(connection);
				}
				catch (Exception e)
				{
					logger.warn("Error while executing command " + c.getClass().getSimpleName()
							+ ", for entry at index " + c.getIndex() + ", rolling back transaction", e);

					connection.rollback();
					throw e;
				}
			}

			Map<Integer, BundleEntryComponent> results = new HashMap<>((int) ((commands.size() / 0.75) + 1));
			for (Command c : commands)
			{
				try
				{
					logger.debug("Running post-execute of command {}, for entry at index {}", c.getClass().getName(),
							c.getIndex());
					results.putIfAbsent(c.getIndex(), c.postExecute(connection));
				}
				catch (Exception e)
				{
					logger.warn("Error while running post-execute of command " + c.getClass().getSimpleName()
							+ ", for entry at index " + c.getIndex() + ", rolling back transaction", e);

					connection.rollback();
					throw e;
				}
			}

			Bundle result = new Bundle();
			result.setType(BundleType.TRANSACTIONRESPONSE);
			results.entrySet().stream().sorted(Comparator.comparing(Entry::getKey)).map(Entry::getValue)
					.forEach(result::addEntry);

			connection.commit();

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
