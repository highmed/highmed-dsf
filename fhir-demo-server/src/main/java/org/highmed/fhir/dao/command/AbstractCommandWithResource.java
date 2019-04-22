package org.highmed.fhir.dao.command;

import org.highmed.fhir.dao.DomainResourceDao;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractCommandWithResource<R extends DomainResource, D extends DomainResourceDao<R>>
		extends AbstractCommand implements Command
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
