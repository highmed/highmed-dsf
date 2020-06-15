package org.highmed.dsf.fhir.event;

public interface EventManager extends EventHandler
{
	/**
	 * @param eventHandler
	 *            not <code>null</code>
	 * @return handler remover, calls {@link EventManager#removeHandler(EventHandler)}
	 */
	Runnable addHandler(EventHandler eventHandler);

	/**
	 * @param eventHandler
	 *            not <code>null</code>
	 */
	void removeHandler(EventHandler eventHandler);
}
