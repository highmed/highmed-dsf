package org.highmed.dsf.fhir.service;

import java.util.Objects;

import org.highmed.dsf.fhir.dao.command.CommandFactory;
import org.highmed.dsf.fhir.dao.command.CommandList;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class InitialDataLoaderImpl implements InitialDataLoader, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(InitialDataLoaderImpl.class);

	private final CommandFactory commandFactory;
	private final FhirContext fhirContext;

	public InitialDataLoaderImpl(CommandFactory commandFactory, FhirContext fhirContext)
	{
		this.commandFactory = commandFactory;
		this.fhirContext = fhirContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(commandFactory, "commandFactory");
		Objects.requireNonNull(fhirContext, "fhirContext");
	}

	@Override
	public void load(Bundle bundle)
	{
		if (bundle == null)
		{
			logger.warn("Not loading 'null' bundle");
			return;
		}

		CommandList commands = commandFactory.createCommands(bundle);
		logger.debug("Executing command list for bundle with {} entries", bundle.getEntry().size());
		Bundle result = commands.execute();
		result.getEntry().forEach(this::logResult);
	}

	private void logResult(BundleEntryComponent entry)
	{
		logger.info("{} {}", entry.getResponse().getLocation(), entry.getResponse().getStatus());
	}
}
