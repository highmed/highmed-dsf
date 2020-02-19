package org.highmed.dsf.fhir.dao.command;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.exception.BadBundleException;
import org.hl7.fhir.r4.model.Bundle;

public interface CommandFactory
{
	/**
	 * @param bundle
	 *            not <code>null</code>
	 * @param user
	 *            not <code>null</code>
	 * @return
	 * @throws BadBundleException
	 */
	CommandList createCommands(Bundle bundle, User user) throws BadBundleException;
}