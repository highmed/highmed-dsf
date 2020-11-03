package org.highmed.fhir.client;

import java.util.function.Supplier;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFhirWebserviceClientJerseyWithRetry
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractFhirWebserviceClientJerseyWithRetry.class);

	protected final FhirWebserviceClientJersey delegate;
	protected final int nTimes;
	protected final long delayMillis;

	protected AbstractFhirWebserviceClientJerseyWithRetry(FhirWebserviceClientJersey delegate, int nTimes,
			long delayMillis)
	{
		this.delegate = delegate;
		this.nTimes = nTimes;
		this.delayMillis = delayMillis;
	}

	protected final <R> R retry(int nTimes, long delayMillis, Supplier<R> supplier)
	{
		if (nTimes < 0)
			throw new IllegalArgumentException("nTimes < 0");
		if (delayMillis < 0)
			throw new IllegalArgumentException("delayMillis < 0");

		RuntimeException caughtException = null;
		for (int tryNumber = 0; tryNumber <= nTimes; tryNumber++)
		{
			try
			{
				if (tryNumber == 0)
					logger.debug("First try ...");
				else
					logger.debug("Retry {} of {}", tryNumber, nTimes);

				return supplier.get();
			}
			catch (ProcessingException | WebApplicationException e)
			{
				if (shouldRetry(e))
				{
					if ((tryNumber) < nTimes)
					{
						logger.warn("Caught {}: {}; trying again in {} ms", e.getClass(), e.getMessage(), delayMillis);

						try
						{
							Thread.sleep(delayMillis);
						}
						catch (InterruptedException e1)
						{
						}
					}
					else
					{
						logger.warn("Caught {}: {}; not trying again", e.getClass(), e.getMessage(), delayMillis);
					}

					if (caughtException != null)
						e.addSuppressed(caughtException);
					caughtException = e;
				}
				else
					throw e;
			}
		}

		throw caughtException;
	}

	private boolean shouldRetry(RuntimeException e)
	{
		if (e instanceof WebApplicationException)
		{
			return isRetryStatusCode((WebApplicationException) e);
		}
		else if (e instanceof ProcessingException)
		{
			Throwable cause = e;
			if (isRetryCause(cause))
				return true;

			while (cause.getCause() != null)
			{
				cause = cause.getCause();
				if (isRetryCause(cause))
					return true;
			}
		}

		return false;
	}

	private boolean isRetryStatusCode(WebApplicationException e)
	{
		return Status.Family.SERVER_ERROR.equals(e.getResponse().getStatusInfo().getFamily());
	}

	private boolean isRetryCause(Throwable cause)
	{
		return cause instanceof ConnectTimeoutException || cause instanceof HttpHostConnectException;
	}
}
