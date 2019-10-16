package org.highmed.dsf.fhir.dao.command;

import org.highmed.dsf.fhir.dao.exception.BadBundleException;
import org.hl7.fhir.r4.model.Bundle;

public interface CommandFactory
{
	CommandList createCommands(Bundle bundle) throws BadBundleException;
}