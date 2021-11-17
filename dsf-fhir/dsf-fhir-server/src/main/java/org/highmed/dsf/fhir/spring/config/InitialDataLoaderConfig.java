package org.highmed.dsf.fhir.spring.config;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.highmed.dsf.fhir.service.InitialDataLoader;
import org.highmed.dsf.fhir.service.InitialDataLoaderImpl;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import ca.uhn.fhir.parser.IParser;

@Configuration
public class InitialDataLoaderConfig
{
	private static final Logger logger = LoggerFactory.getLogger(InitialDataLoaderConfig.class);

	@Autowired
	private PropertiesConfig propertiesConfig;

	@Autowired
	private CommandConfig commandConfig;

	@Autowired
	private FhirConfig fhirConfig;

	@Autowired
	private ReferenceConfig referenceConfig;

	@Bean
	public InitialDataLoader initialDataLoader()
	{
		return new InitialDataLoaderImpl(commandConfig.commandFactory(), fhirConfig.fhirContext());
	}

	@Order(HIGHEST_PRECEDENCE)
	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event) throws IOException
	{
		try (InputStream fileIn = ClassLoader.getSystemResourceAsStream("fhir/bundle.xml"))
		{
			logger.info("Loading data from JAR bundle ...");
			Bundle bundle = parseXmlBundle(p -> p.parseResource(Bundle.class, fileIn));
			initialDataLoader().load(bundle);
		}
		catch (Exception e)
		{
			logger.warn("Error while loading data from JAR bundle: {}", e.getMessage());
			throw e;
		}

		Path file = Paths.get(propertiesConfig.getInitBundleFile());
		if (!Files.isReadable(file))
			throw new IOException("Init bundle file at " + file.toString() + " not readable");

		try (InputStream fileIn = Files.newInputStream(file))
		{
			String read = IOUtils.toString(fileIn, StandardCharsets.UTF_8);
			String resolved = event.getApplicationContext().getEnvironment().resolveRequiredPlaceholders(read);

			logger.info("Loading data from external bundle ...");
			Bundle bundle = parseXmlBundle(p -> p.parseResource(Bundle.class, resolved));
			initialDataLoader().load(bundle);
		}
		catch (Exception e)
		{
			logger.warn("Error while loading data from external bundle: {}", e.getMessage());
			throw e;
		}
	}

	private Bundle parseXmlBundle(Function<IParser, Bundle> parse)
	{
		IParser p = fhirConfig.fhirContext().newXmlParser();
		p.setStripVersionsFromReferences(false);
		p.setOverrideResourceIdWithBundleEntryFullUrl(false);

		Bundle bundle = parse.apply(p);
		return referenceConfig.referenceCleaner().cleanReferenceResourcesIfBundle(bundle);
	}
}
