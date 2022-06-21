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
import static org.junit.Assert.fail;

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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.glassfish.jersey.servlet.init.JerseyServletContainerInitializer;
import org.highmed.dsf.fhir.FhirContextLoaderListener;
import org.highmed.dsf.fhir.authentication.AuthenticationFilter;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelperImpl;
import org.highmed.dsf.fhir.dao.AbstractDbTest;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceCleanerImpl;
import org.highmed.dsf.fhir.service.ReferenceExtractorImpl;
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
import de.rwh.utils.test.LiquibaseTemplateTestClassRule;
import de.rwh.utils.test.LiquibaseTemplateTestRule;

public abstract class AbstractIntegrationTest extends AbstractDbTest
{
	@ClassRule
	public static final X509Certificates certificates = new X509Certificates();

	public static final String INTEGRATION_TEST_DB_TEMPLATE_NAME = "integration_test_template";

	protected static final BasicDataSource adminDataSource = createAdminBasicDataSource();
	protected static final BasicDataSource liquibaseDataSource = createLiquibaseDataSource();
	protected static final BasicDataSource defaultDataSource = createDefaultDataSource();

	@ClassRule
	public static final LiquibaseTemplateTestClassRule liquibaseRule = new LiquibaseTemplateTestClassRule(
			adminDataSource, LiquibaseTemplateTestClassRule.DEFAULT_TEST_DB_NAME, INTEGRATION_TEST_DB_TEMPLATE_NAME,
			liquibaseDataSource, CHANGE_LOG_FILE, CHANGE_LOG_PARAMETERS, false);

	@Rule
	public final LiquibaseTemplateTestRule templateRule = new LiquibaseTemplateTestRule(adminDataSource,
			LiquibaseTemplateTestClassRule.DEFAULT_TEST_DB_NAME, INTEGRATION_TEST_DB_TEMPLATE_NAME);

	private static final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);

	protected static final String[] ALL_TABLES = { "activity_definitions", "binaries", "bundles", "code_systems",
			"endpoints", "groups", "healthcare_services", "locations", "naming_systems", "organizations", "patients",
			"practitioner_roles", "practitioners", "provenances", "research_studies", "structure_definition_snapshots",
			"structure_definitions", "subscriptions", "tasks", "value_sets" };

	protected static final String BASE_URL = "https://localhost:8001/fhir";
	protected static final String WEBSOCKET_URL = "wss://localhost:8001/fhir/ws";

	private static final Path FHIR_BUNDLE_FILE = Paths.get("target", UUID.randomUUID().toString() + ".xml");
	private static final List<Path> FILES_TO_DELETE = Arrays.asList(FHIR_BUNDLE_FILE);

	protected static final FhirContext fhirContext = FhirContext.forR4();
	protected static final ReadAccessHelperImpl readAccessHelper = new ReadAccessHelperImpl();

	private static final ReferenceCleaner referenceCleaner = new ReferenceCleanerImpl(new ReferenceExtractorImpl());

	private static JettyServer fhirServer;
	private static FhirWebserviceClient webserviceClient;
	private static FhirWebserviceClient externalWebserviceClient;

	@BeforeClass
	public static void beforeClass() throws Exception
	{
		defaultDataSource.start();
		liquibaseDataSource.start();
		adminDataSource.start();

		logger.info("Creating Bundle ...");
		createTestBundle(certificates.getClientCertificate().getCertificate(),
				certificates.getExternalClientCertificate().getCertificate());

		logger.info("Creating webservice client ...");
		webserviceClient = createWebserviceClient(certificates.getClientCertificate().getTrustStore(),
				certificates.getClientCertificate().getKeyStore(),
				certificates.getClientCertificate().getKeyStorePassword(), fhirContext, referenceCleaner);

		logger.info("Creating external webservice client ...");
		externalWebserviceClient = createWebserviceClient(certificates.getExternalClientCertificate().getTrustStore(),
				certificates.getExternalClientCertificate().getKeyStore(),
				certificates.getExternalClientCertificate().getKeyStorePassword(), fhirContext, referenceCleaner);

		logger.info("Starting FHIR Server ...");
		fhirServer = startFhirServer();

		logger.info("Creating template database ...");
		liquibaseRule.createTemplateDatabase();
	}

	private static FhirWebserviceClient createWebserviceClient(KeyStore trustStore, KeyStore keyStore,
			char[] keyStorePassword, FhirContext fhirContext, ReferenceCleaner referenceCleaner)
	{
		return new FhirWebserviceClientJersey(BASE_URL, trustStore, keyStore, keyStorePassword, null, null, null, 0, 0,
				false, null, fhirContext, referenceCleaner);
	}

	private static WebsocketClient createWebsocketClient(KeyStore trustStore, KeyStore keyStore,
			char[] keyStorePassword, String subscriptionIdPart)
	{
		return new WebsocketClientTyrus(() ->
		{}, URI.create(WEBSOCKET_URL), trustStore, keyStore, keyStorePassword, null, null, null, subscriptionIdPart);
	}

	private static JettyServer startFhirServer() throws Exception
	{
		Properties properties = PropertiesReader.read(Paths.get("src/test/resources/integration/jetty.properties"),
				StandardCharsets.UTF_8);
		overrideJettyPropertiesForTesting(properties);

		HttpConfiguration httpConfiguration = httpConfiguration(secureRequestCustomizer(properties));
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
		properties.put("org.highmed.dsf.fhir.db.url", DATABASE_URL);
		properties.put("org.highmed.dsf.fhir.db.user.username", DATABASE_USER);
		properties.put("org.highmed.dsf.fhir.db.user.password", DATABASE_USER_PASSWORD);
		properties.put("org.highmed.dsf.fhir.db.user.permanent.delete.username", DATABASE_DELETE_USER);
		properties.put("org.highmed.dsf.fhir.db.user.permanent.delete.password", DATABASE_DELETE_USER_PASSWORD);

		String clientCertHashHex = calculateSha512CertificateThumbprintHex(
				certificates.getClientCertificate().getCertificate());
		properties.put("org.highmed.dsf.fhir.server.user.thumbprints", clientCertHashHex);
		properties.put("org.highmed.dsf.fhir.server.user.thumbprints.permanent.delete", clientCertHashHex);

		properties.put("org.highmed.dsf.fhir.server.init.bundle", FHIR_BUNDLE_FILE.toString());

		properties.put("org.highmed.dsf.fhir.client.trust.certificates",
				certificates.getCaCertificateFile().toString());
		properties.put("org.highmed.dsf.fhir.client.certificate", certificates.getClientCertificateFile().toString());
		properties.put("org.highmed.dsf.fhir.client.certificate.private.key",
				certificates.getClientCertificatePrivateKeyFile().toString());
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
			Bundle bundle = parser.parseResource(Bundle.class, in);
			return referenceCleaner.cleanReferenceResourcesIfBundle(bundle);
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

	private static void createTestBundle(X509Certificate certificate, X509Certificate externalCertificate)
	{
		Path testBundleTemplateFile = Paths.get("src/test/resources/integration/test-bundle.xml");

		Bundle testBundle = readBundle(testBundleTemplateFile, newXmlParser());

		Organization organization = (Organization) testBundle.getEntry().get(0).getResource();
		Extension thumbprintExtension = organization
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");

		String clientCertHashHex = calculateSha512CertificateThumbprintHex(certificate);
		thumbprintExtension.setValue(new StringType(clientCertHashHex));

		Organization externalOrganization = (Organization) testBundle.getEntry().get(2).getResource();
		Extension externalThumbprintExtension = externalOrganization
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");

		String externalClientCertHashHex = calculateSha512CertificateThumbprintHex(externalCertificate);
		externalThumbprintExtension.setValue(new StringType(externalClientCertHashHex));

		// FIXME hapi parser can't handle embedded resources and creates them while parsing bundles
		new ReferenceCleanerImpl(new ReferenceExtractorImpl()).cleanReferenceResourcesIfBundle(testBundle);

		writeBundle(FHIR_BUNDLE_FILE, testBundle);
	}

	@AfterClass
	public static void afterClass() throws Exception
	{
		defaultDataSource.close();
		liquibaseDataSource.close();
		adminDataSource.close();

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

	protected static FhirWebserviceClient getWebserviceClient()
	{
		return webserviceClient;
	}

	protected static FhirWebserviceClient getExternalWebserviceClient()
	{
		return externalWebserviceClient;
	}

	protected static WebsocketClient getWebsocketClient()
	{
		Bundle bundle = getWebserviceClient().searchWithStrictHandling(Subscription.class,
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
				certificates.getClientCertificate().getKeyStorePassword(), subscription.getIdElement().getIdPart());
	}

	protected static final ReadAccessHelper getReadAccessHelper()
	{
		return readAccessHelper;
	}

	protected static void expectBadRequest(Runnable operation) throws Exception
	{
		expectWebApplicationException(operation, Status.BAD_REQUEST);
	}

	protected static void expectForbidden(Runnable operation) throws Exception
	{
		expectWebApplicationException(operation, Status.FORBIDDEN);
	}

	protected static void expectNotFound(Runnable operation) throws Exception
	{
		expectWebApplicationException(operation, Status.NOT_FOUND);
	}

	protected static void expectNotAcceptable(Runnable operation) throws Exception
	{
		expectWebApplicationException(operation, Status.NOT_ACCEPTABLE);
	}

	protected static void expectWebApplicationException(Runnable operation, Status status) throws Exception
	{
		try
		{
			operation.run();
			fail("WebApplicationException expected");
		}
		catch (WebApplicationException e)
		{
			assertEquals(status.getStatusCode(), e.getResponse().getStatus());
		}
	}
}
