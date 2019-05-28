package org.highmed.fhir.init;

import java.util.Objects;

import org.highmed.fhir.dao.command.CommandFactory;
import org.highmed.fhir.dao.command.CommandList;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class InitialDataLoader implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(InitialDataLoader.class);

	private final CommandFactory commandFactory;
	private final FhirContext fhirContext;

	public InitialDataLoader(CommandFactory commandFactory, FhirContext fhirContext)
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

	public void load(Bundle bundle)
	{
		if (bundle == null)
		{
			logger.warn("Not loading 'null' bundle");
			return;
		}

		CommandList commands = commandFactory.createCommands(bundle);
		logger.info("Executing command list for bundle with {} entries", bundle.getEntry().size());
		Bundle result = commands.execute();
		logger.info("Initial data load result: {}", fhirContext.newXmlParser().encodeResourceToString(result));
	}
}
