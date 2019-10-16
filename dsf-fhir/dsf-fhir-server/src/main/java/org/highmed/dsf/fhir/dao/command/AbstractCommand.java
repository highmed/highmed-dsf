package org.highmed.dsf.fhir.dao.command;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

public abstract class AbstractCommand implements Command
{
	private final int transactionPriority;

	protected final int index;

	protected final Bundle bundle;
	protected final BundleEntryComponent entry;

	protected final String serverBase;

	public AbstractCommand(int transactionPriority, int index, Bundle bundle, BundleEntryComponent entry,
			String serverBase)
	{
		this.transactionPriority = transactionPriority;

		this.index = index;
		this.bundle = bundle;
		this.entry = entry;
		this.serverBase = serverBase;
	}

	@Override
	public final int getIndex()
	{
		return index;
	}

	@Override
	public final int getTransactionPriority()
	{
		return transactionPriority;
	}
}
