package org.highmed.dsf.fhir.dao.command;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;

public abstract class AbstractCommandWithResource<R extends Resource, D extends ResourceDao<R>> extends AbstractCommand
		implements Command
{
	protected final ValidationHelper validationHelper;
	protected final R resource;
	protected final D dao;
	protected final ExceptionHandler exceptionHandler;
	protected final ParameterConverter parameterConverter;
	protected final ReferencesHelperImpl<R> referencesHelper;

	public AbstractCommandWithResource(int transactionPriority, int index, User user, Bundle bundle,
			BundleEntryComponent entry, String serverBase, AuthorizationHelper authorizationHelper,
			ValidationHelper validationHelper, R resource, D dao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, ResponseGenerator responseGenerator,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver)
	{
		super(transactionPriority, index, user, bundle, entry, serverBase, authorizationHelper);

		this.validationHelper = validationHelper;
		this.resource = resource;
		this.dao = dao;
		this.exceptionHandler = exceptionHandler;
		this.parameterConverter = parameterConverter;

		referencesHelper = createReferencesHelper(index, user, serverBase, resource, responseGenerator,
				referenceExtractor, referenceResolver);
	}

	protected ReferencesHelperImpl<R> createReferencesHelper(int index, User user, String serverBase, R resource,
			ResponseGenerator responseGenerator, ReferenceExtractor referenceExtractor,
			ReferenceResolver referenceResolver)
	{
		return new ReferencesHelperImpl<R>(index, user, resource, serverBase, referenceExtractor, referenceResolver,
				responseGenerator);
	}
}
