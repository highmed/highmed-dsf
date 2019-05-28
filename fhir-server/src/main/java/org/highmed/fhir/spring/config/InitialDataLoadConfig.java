package org.highmed.fhir.spring.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.highmed.fhir.init.InitialDataLoader;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class InitialDataLoadConfig
{
	private static final Logger logger = LoggerFactory.getLogger(InitialDataLoadConfig.class);

	@Value("${org.highmed.fhir.init.bundle.file}")
	private String initBundleFile;

	@Autowired
	private CommandConfig commandConfig;

	@Autowired
	private FhirConfig fhirConfig;

	@Bean
	public InitialDataLoader initialDataLoader()
	{
		return new InitialDataLoader(commandConfig.commandFactory(), fhirConfig.fhirContext());
	}

	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event) throws IOException
	{
		try (InputStream fileIn = ClassLoader.getSystemResourceAsStream("fhir/bundle.xml"))
		{
			logger.debug("Loading data from JAR bundle ...");
			Bundle bundle = fhirConfig.fhirContext().newXmlParser().parseResource(Bundle.class, fileIn);
			initialDataLoader().load(bundle);
		}

		Path file = Paths.get(initBundleFile);
		if (!Files.isReadable(file))
			throw new IOException("Init bundle file at " + file.toString() + " not readable");

		try (InputStream fileIn = Files.newInputStream(file))
		{
			logger.debug("Loading data from external bundle ...");
			Bundle bundle = fhirConfig.fhirContext().newXmlParser().parseResource(Bundle.class, fileIn);
			initialDataLoader().load(bundle);
		}
	}
}
