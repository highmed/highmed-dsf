package org.highmed.dsf.fhir.dao.command;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.prefer.PreferHandlingType;
import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;

public class HeadCommand extends ReadCommand
{
	public HeadCommand(int index, User user, PreferReturnType returnType, Bundle bundle, BundleEntryComponent entry,
			String serverBase, AuthorizationHelper authorizationHelper, int defaultPageCount, DaoProvider daoProvider,
			ParameterConverter parameterConverter, ResponseGenerator responseGenerator,
			ExceptionHandler exceptionHandler, ReferenceCleaner referenceCleaner, PreferHandlingType handlingType)
	{
		super(index, user, returnType, bundle, entry, serverBase, authorizationHelper, defaultPageCount, daoProvider,
				parameterConverter, responseGenerator, exceptionHandler, referenceCleaner, handlingType);
	}

	@Override
	protected void setSingleResult(BundleEntryComponent resultEntry, Resource singleResult)
	{
		// do nothing for HEAD
	}

	@Override
	protected void setMultipleResult(BundleEntryComponent resultEntry, Bundle multipleResult)
	{
		// do nothing for HEAD
	}
}
