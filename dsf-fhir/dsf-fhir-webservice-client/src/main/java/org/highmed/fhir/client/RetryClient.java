package org.highmed.fhir.client;

public interface RetryClient<T>
{
	int RETRY_ONCE = 1;
	int RETRY_FOREVER = -1;
	long FIVE_SECONDS = 5_000L;

	/**
	 * retries once after a delay of {@value RetryClient#FIVE_SECONDS} ms
	 *
	 * @return T
	 */
	default T withRetry()
	{
		return withRetry(RETRY_ONCE, FIVE_SECONDS);
	}

	/**
	 * retries <b>nTimes</b> and waits {@value RetryClient#FIVE_SECONDS} ms between tries
	 *
	 * @param nTimes
	 *            {@code >= 0}
	 * @return T
	 *
	 * @throws IllegalArgumentException
	 *             if param <b>nTimes</b> is {@code <0}
	 */
	default T withRetry(int nTimes)
	{
		return withRetry(nTimes, FIVE_SECONDS);
	}

	/**
	 * retries once after a delay of <b>delayMillis</b> ms
	 *
	 * @param delayMillis
	 *            {@code >= 0}
	 * @return T
	 * @throws IllegalArgumentException
	 *             if param <b>delayMillis</b> is {@code <0}
	 */
	default T withRetry(long delayMillis)
	{
		return withRetry(RETRY_ONCE, delayMillis);
	}

	/**
	 * @param nTimes
	 *            {@code >= 0}
	 * @param delayMillis
	 *            {@code >= 0}
	 * @return T
	 *
	 * @throws IllegalArgumentException
	 *             if param <b>nTimes</b> or <b>delayMillis</b> is {@code <0}
	 */
	T withRetry(int nTimes, long delayMillis);

	/**
	 * @param delayMillis
	 *            {@code >= 0}
	 * @return T
	 * @throws IllegalArgumentException
	 *             if param <b>delayMillis</b> is {@code <0}
	 */
	T withRetryForever(long delayMillis);
}
