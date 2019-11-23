package org.highmed.dsf.fhir.integration;

import static de.rwh.utils.jetty.JettyServer.httpConfiguration;
import static de.rwh.utils.jetty.JettyServer.httpsConnector;
import static de.rwh.utils.jetty.JettyServer.secureRequestCustomizer;
import static de.rwh.utils.jetty.JettyServer.statusCodeOnlyErrorHandler;
import static de.rwh.utils.jetty.JettyServer.webInfClassesDirs;
import static de.rwh.utils.jetty.JettyServer.webInfJars;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.glassfish.jersey.servlet.init.JerseyServletContainerInitializer;
import org.highmed.dsf.fhir.FhirContextLoaderListener;
import org.highmed.dsf.fhir.authentication.AuthenticationFilter;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceExtractorImpl;
import org.highmed.dsf.fhir.spring.config.InitialDataLoaderConfig;
import org.highmed.dsf.fhir.test.FhirEmbeddedPostgresWithLiquibase;
import org.highmed.dsf.fhir.test.TestSuiteIntegrationTests;
import org.highmed.dsf.fhir.test.X509Certificates;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
import org.highmed.fhir.client.WebsocketClient;
import org.highmed.fhir.client.WebsocketClientTyrus;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Subscription;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.rwh.utils.jetty.JettyServer;
import de.rwh.utils.jetty.PropertiesReader;
import de.rwh.utils.test.Database;

public abstract class AbstractIntegrationTest
{
	@ClassRule
	public static final X509Certificates certificates = new X509Certificates(TestSuiteIntegrationTests.certificates);

	@ClassRule
	public static final FhirEmbeddedPostgresWithLiquibase template = new FhirEmbeddedPostgresWithLiquibase(
			TestSuiteIntegrationTests.template);

	@Rule
	public final Database database = new Database(template);

	private static final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);

	private static final String BASE_URL = "https://localhost:8001/fhir/";
	private static final String WEBSOCKET_URL = "wss://localhost:8001/fhir/ws";

	private static final Path FHIR_BUNDLE_FILE = Paths.get("target", UUID.randomUUID().toString() + ".xml");
	private static final List<Path> FILES_TO_DELETE = Arrays.asList(FHIR_BUNDLE_FILE);

	private static final FhirContext fhirContext = FhirContext.forR4();
	private static final ReferenceExtractor extractor = new ReferenceExtractorImpl();

	private static JettyServer fhirServer;
	private static FhirWebserviceClient webserviceClient;

	@BeforeClass
	public static void beforeClass() throws Exception
	{
		logger.info("Creating DB schema pre server startup ...");
		template.createSchema();

		logger.info("Creating Bundle ...");
		createTestBundle(certificates.getClientCertificate().getCertificate());

		logger.info("Creating webservice client ...");
		webserviceClient = createWebserviceClient(certificates.getClientCertificate().getTrustStore(),
				certificates.getClientCertificate().getKeyStore(),
				certificates.getClientCertificate().getKeyStorePassword(), fhirContext);

		logger.info("Starting FHIR Server ...");
		fhirServer = startFhirServer();
	}

	private static FhirWebserviceClient createWebserviceClient(KeyStore trustStore, KeyStore keyStore,
			String keyStorePassword, FhirContext fhirContext)
	{
		return new FhirWebserviceClientJersey(BASE_URL, trustStore, keyStore, keyStorePassword, null, null, null, 0, 0,
				null, fhirContext);
	}

	private static WebsocketClient createWebsocketClient(KeyStore trustStore, KeyStore keyStore,
			String keyStorePassword, FhirContext fhirContext, String subscriptionIdPart)
	{
		return new WebsocketClientTyrus(fhirContext, URI.create(WEBSOCKET_URL), trustStore, keyStore, keyStorePassword,
				subscriptionIdPart);
	}

	private static JettyServer startFhirServer() throws Exception
	{
		Properties properties = PropertiesReader.read(Paths.get("src/test/resources/integration/jetty.properties"),
				StandardCharsets.UTF_8);
		overrideJettyPropertiesForTesting(properties);

		HttpConfiguration httpConfiguration = httpConfiguration(secureRequestCustomizer());
		Function<Server, ServerConnector> connector = httpsConnector(httpConfiguration, properties);

		Properties initParameter = PropertiesReader.read(Paths.get("src/test/resources/integration/config.properties"),
				StandardCharsets.UTF_8);
		overrideConfigPropertiesForTesting(initParameter);

		Predicate<String> filter = s -> s.contains("fhir-server");
		Stream<String> webInfClassesDirs = webInfClassesDirs(filter);
		Stream<String> webInfJars = webInfJars(filter);

		List<Class<?>> initializers = Arrays.asList(SpringServletContainerInitializer.class,
				JerseyServletContainerInitializer.class);

		ErrorHandler errorHandler = statusCodeOnlyErrorHandler();

		JettyServer server = new JettyServer(connector, errorHandler, "/fhir", initializers, initParameter,
				webInfClassesDirs, webInfJars, AuthenticationFilter.class);

		WebSocketServerContainerInitializer.initialize(server.getWebAppContext());

		server.start();

		return server;
	}

	private static void overrideJettyPropertiesForTesting(Properties properties)
	{
		properties.put("jetty.truststore.pem", certificates.getCaCertificateFile().toString());
		properties.put("jetty.keystore.p12", certificates.getServerCertificateFile().toString());
	}

	private static void overrideConfigPropertiesForTesting(Properties properties)
	{
		properties.put("org.highmed.dsf.fhir.db.url", template.getJdbcUrl());
		properties.put("org.highmed.dsf.fhir.db.server_user", template.getDbUsername());
		properties.put("org.highmed.dsf.fhir.db.server_user_password", template.getDbPassword());

		String clientCertHashHex = calculateSha512CertificateThumbprintHex(
				certificates.getClientCertificate().getCertificate());
		properties.put("org.highmed.dsf.fhir.local-user.thumbprints", clientCertHashHex);
		properties.put("org.highmed.dsf.fhir.webservice.keystore.p12file",
				certificates.getClientCertificateFile().toString());

		properties.put("org.highmed.dsf.fhir.init.bundle.file", FHIR_BUNDLE_FILE.toString());
	}

	private static String calculateSha512CertificateThumbprintHex(X509Certificate certificate)
	{
		try
		{
			return Hex.encodeHexString(MessageDigest.getInstance("SHA-512").digest(certificate.getEncoded()));
		}
		catch (CertificateEncodingException | NoSuchAlgorithmException e)
		{
			logger.error("Error while calculating SHA-512 certificate thumbprint", e);
			throw new RuntimeException(e);
		}
	}

	protected static Bundle readBundle(Path bundleTemplateFile, IParser parser)
	{
		try (InputStream in = Files.newInputStream(bundleTemplateFile))
		{
			return parser.parseResource(Bundle.class, in);
		}
		catch (IOException e)
		{
			logger.error("Error while reading bundle from " + bundleTemplateFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	protected static void writeBundle(Path bundleFile, Bundle bundle)
	{
		try (OutputStream out = Files.newOutputStream(bundleFile);
				OutputStreamWriter writer = new OutputStreamWriter(out))
		{
			newXmlParser().encodeResourceToWriter(bundle, writer);
		}
		catch (IOException e)
		{
			logger.error("Error while writing bundle to " + bundleFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	protected static IParser newXmlParser()
	{
		IParser parser = fhirContext.newXmlParser();
		parser.setStripVersionsFromReferences(false);
		parser.setOverrideResourceIdWithBundleEntryFullUrl(false);
		parser.setPrettyPrint(true);
		return parser;
	}

	protected static IParser newJsonParser()
	{
		IParser parser = fhirContext.newJsonParser();
		parser.setStripVersionsFromReferences(false);
		parser.setOverrideResourceIdWithBundleEntryFullUrl(false);
		parser.setPrettyPrint(true);
		return parser;
	}

	private static void createTestBundle(X509Certificate certificate)
	{
		Path testBundleTemplateFile = Paths.get("src/test/resources/integration/test-bundle.xml");

		Bundle testBundle = readBundle(testBundleTemplateFile, newXmlParser());

		Organization organization = (Organization) testBundle.getEntry().get(0).getResource();
		Extension thumbprintExtension = organization
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint");

		String clientCertHashHex = calculateSha512CertificateThumbprintHex(certificate);
		thumbprintExtension.setValue(new StringType(clientCertHashHex));

		removeReferenceEmbeddedResources(testBundle);

		writeBundle(FHIR_BUNDLE_FILE, testBundle);
	}

	// FIXME hapi parser can't handle embedded resources and creates them while parsing bundles
	private static void removeReferenceEmbeddedResources(Bundle bundle)
	{
		bundle.getEntry().stream().map(e -> e.getResource()).forEach(res ->
		{
			logger.debug("Extracting references from {} resource", res.getResourceType());
			extractor.getReferences(res).forEach(ref ->
			{
				logger.debug("Setting reference embedded resource to null at {}", ref.getReferenceLocation());
				ref.getReference().setResource(null);
			});
		});
	}

	@AfterClass
	public static void afterClass() throws Exception
	{
		try
		{
			if (fhirServer != null)
			{
				logger.info("Stoping FHIR Server ...");
				fhirServer.stop();
			}
		}
		catch (Exception e)
		{
			logger.error("Error while stopping FHIR Server", e);
		}

		logger.info("Deleting files {} ...", FILES_TO_DELETE);
		FILES_TO_DELETE.forEach(AbstractIntegrationTest::deleteFile);
	}

	private static void deleteFile(Path file)
	{
		try
		{
			Files.delete(file);
		}
		catch (IOException e)
		{
			logger.error("Error while deleting test file {}, error: {}", file.toString(), e.toString());
		}
	}

	protected AnnotationConfigWebApplicationContext getSpringWebApplicationContext()
	{
		WebAppContext webAppContext = fhirServer.getWebAppContext();

		assertNotNull(webAppContext);
		assertNotNull(webAppContext.getEventListeners().length);
		assertTrue(webAppContext.getEventListeners().length >= 1);
		assertTrue(webAppContext.getEventListeners()[0] instanceof FhirContextLoaderListener);

		FhirContextLoaderListener listener = (FhirContextLoaderListener) webAppContext.getEventListeners()[0];
		AnnotationConfigWebApplicationContext contex = listener.getContex();

		assertNotNull(contex);

		return contex;
	}

	@Before
	public void before() throws Exception
	{
		logger.info("Loading initial FHIR data bundles ...");
		InitialDataLoaderConfig initialDataLoadConfig = getSpringWebApplicationContext()
				.getBean(InitialDataLoaderConfig.class);
		assertNotNull(initialDataLoadConfig);
		initialDataLoadConfig.onContextRefreshedEvent(null);
	}

	protected static FhirWebserviceClient getWebserviceClient()
	{
		return webserviceClient;
	}

	protected static WebsocketClient getWebsocketClient()
	{
		Bundle bundle = getWebserviceClient().search(Subscription.class,
				Map.of("criteria", Collections.singletonList("Task?status=requested"), "status",
						Collections.singletonList("active"), "type", Collections.singletonList("websocket"), "payload",
						Collections.singletonList("application/fhir+json")));

		assertNotNull(bundle);
		assertEquals(1, bundle.getTotal());
		assertNotNull(bundle.getEntryFirstRep());
		assertTrue(bundle.getEntryFirstRep().getResource() instanceof Subscription);

		Subscription subscription = (Subscription) bundle.getEntryFirstRep().getResource();
		assertNotNull(subscription.getIdElement());
		assertNotNull(subscription.getIdElement().getIdPart());

		return createWebsocketClient(certificates.getClientCertificate().getTrustStore(),
				certificates.getClientCertificate().getKeyStore(),
				certificates.getClientCertificate().getKeyStorePassword(), fhirContext,
				subscription.getIdElement().getIdPart());
	}
}
