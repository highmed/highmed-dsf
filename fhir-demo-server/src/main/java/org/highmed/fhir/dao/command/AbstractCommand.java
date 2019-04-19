package org.highmed.fhir.dao.command;

import org.highmed.fhir.dao.DomainResourceDao;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

public abstract class AbstractCommand<R extends DomainResource, D extends DomainResourceDao<R>> implements Command
{
	private final int transactionPriority;
	private final int index;

	protected final Bundle bundle;
	protected final BundleEntryComponent entry;
	protected final R resource;

	protected final String serverBase;
	protected final D dao;

	public AbstractCommand(int transactionPriority, int index, Bundle bundle, BundleEntryComponent entry, R resource,
			String serverBase, D dao)
	{
		this.transactionPriority = transactionPriority;

		this.index = index;
		this.bundle = bundle;
		this.entry = entry;
		this.resource = resource;
		this.serverBase = serverBase;
		this.dao = dao;
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
