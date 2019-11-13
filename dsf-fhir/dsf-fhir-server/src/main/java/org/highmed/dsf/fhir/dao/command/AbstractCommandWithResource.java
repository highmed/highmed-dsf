package org.highmed.dsf.fhir.dao.command;

import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;

public abstract class AbstractCommandWithResource<R extends Resource, D extends ResourceDao<R>> extends AbstractCommand
		implements Command
{
	protected final R resource;
	protected final D dao;
	protected final ExceptionHandler exceptionHandler;
	protected final ParameterConverter parameterConverter;

	public AbstractCommandWithResource(int transactionPriority, int index, Bundle bundle, BundleEntryComponent entry,
			String serverBase, R resource, D dao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter)
	{
		super(transactionPriority, index, bundle, entry, serverBase);

		this.resource = resource;
		this.dao = dao;
		this.exceptionHandler = exceptionHandler;
		this.parameterConverter = parameterConverter;
	}
}
