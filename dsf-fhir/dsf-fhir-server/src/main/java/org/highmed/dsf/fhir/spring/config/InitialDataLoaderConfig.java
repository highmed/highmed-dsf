package org.highmed.dsf.fhir.spring.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.highmed.dsf.fhir.service.InitialDataLoader;
import org.highmed.dsf.fhir.service.InitialDataLoaderImpl;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import ca.uhn.fhir.parser.IParser;

@Configuration
public class InitialDataLoaderConfig
{
	private static final Logger logger = LoggerFactory.getLogger(InitialDataLoaderConfig.class);

	@Value("${org.highmed.dsf.fhir.init.bundle.file}")
	private String initBundleFile;

	@Autowired
	private CommandConfig commandConfig;

	@Autowired
	private FhirConfig fhirConfig;

	@Bean
	public InitialDataLoader initialDataLoader()
	{
		return new InitialDataLoaderImpl(commandConfig.commandFactory(), fhirConfig.fhirContext());
	}

	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event) throws IOException
	{
		try (InputStream fileIn = ClassLoader.getSystemResourceAsStream("fhir/bundle.xml"))
		{
			logger.info("Loading data from JAR bundle ...");
			Bundle bundle = parseXmlBundle(fileIn);
			initialDataLoader().load(bundle);
		}

		Path file = Paths.get(initBundleFile);
		if (!Files.isReadable(file))
			throw new IOException("Init bundle file at " + file.toString() + " not readable");

		try (InputStream fileIn = Files.newInputStream(file))
		{
			logger.info("Loading data from external bundle ...");
			Bundle bundle = parseXmlBundle(fileIn);
			initialDataLoader().load(bundle);
		}
	}

	private Bundle parseXmlBundle(InputStream in)
	{
		IParser p = fhirConfig.fhirContext().newXmlParser();
		p.setStripVersionsFromReferences(false);
		p.setOverrideResourceIdWithBundleEntryFullUrl(false);

		return p.parseResource(Bundle.class, in);
	}
}
