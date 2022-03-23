package org.highmed.dsf.fhir.dao.command;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.exception.BadBundleException;
import org.highmed.dsf.fhir.prefer.PreferHandlingType;
import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.hl7.fhir.r4.model.Bundle;

public interface CommandFactory
{
	/**
	 * @param bundle
	 *            not <code>null</code>
	 * @param user
	 *            not <code>null</code>
	 * @param returnType
	 *            not <code>null</code>
	 * @param handlingType
	 *            not <code>null</code>
	 *
	 * @return {@link CommandList} with individual commands from each entry contained in the bundle
	 * @throws BadBundleException
	 *             if the bundle could not be processed because of wrong bundle type or other errors
	 */
	CommandList createCommands(Bundle bundle, User user, PreferReturnType returnType, PreferHandlingType handlingType)
			throws BadBundleException;
}