package org.highmed.dsf.fhir;

import static de.rwh.utils.jetty.JettyServer.httpConfiguration;
import static de.rwh.utils.jetty.JettyServer.start;
import static de.rwh.utils.jetty.JettyServer.statusCodeOnlyErrorHandler;
import static de.rwh.utils.jetty.JettyServer.webInfClassesDirs;
import static de.rwh.utils.jetty.JettyServer.webInfJars;
import static de.rwh.utils.jetty.PropertiesReader.read;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.servlet.Filter;
import javax.servlet.ServletException;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConfiguration.Customizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.glassfish.jersey.servlet.init.JerseyServletContainerInitializer;
import org.highmed.dsf.fhir.authentication.AuthenticationFilter;
import org.highmed.dsf.fhir.cors.CorsFilter;
import org.highmed.dsf.tools.db.DbMigrator;
import org.springframework.web.SpringServletContainerInitializer;

import de.rwh.utils.jetty.JettyServer;
import de.rwh.utils.jetty.Log4jInitializer;

public final class FhirServer
{
	public static void startHttpServer()
	{
		startServer(JettyServer::forwardedSecureRequestCustomizer, JettyServer::httpConnector);
	}

	public static void startHttpsServer()
	{
		startServer(JettyServer::secureRequestCustomizer, JettyServer::httpsConnector);
	}

	private static void startServer(Supplier<Customizer> customizerBuilder,
			BiFunction<HttpConfiguration, Properties, Function<Server, ServerConnector>> connectorBuilder)
	{
		Properties properties = read(Paths.get("conf/jetty.properties"), StandardCharsets.UTF_8);

		Log4jInitializer.initializeLog4j(properties);

		Properties configProperties = read(Paths.get("conf/config.properties"), StandardCharsets.UTF_8);

		DbMigrator dbMigrator = new DbMigrator("org.highmed.dsf.fhir.", configProperties);
		DbMigrator.retryOnConnectException(3, dbMigrator::migrate);

		HttpConfiguration httpConfiguration = httpConfiguration(customizerBuilder.get());
		Function<Server, ServerConnector> connector = connectorBuilder.apply(httpConfiguration, properties);

		Predicate<String> filter = s -> s.contains("fhir-server");
		Stream<String> webInfClassesDirs = webInfClassesDirs(filter);
		Stream<String> webInfJars = webInfJars(filter);

		List<Class<?>> initializers = Arrays.asList(SpringServletContainerInitializer.class,
				JerseyServletContainerInitializer.class);

		ErrorHandler errorHandler = statusCodeOnlyErrorHandler();

		List<Class<? extends Filter>> filters = new ArrayList<>();
		filters.add(AuthenticationFilter.class);
		if (Boolean.parseBoolean(properties.getProperty("jetty.cors.enable")))
			filters.add(CorsFilter.class);

		@SuppressWarnings("unchecked")
		JettyServer server = new JettyServer(connector, errorHandler, "/fhir", initializers, configProperties,
				webInfClassesDirs, webInfJars, filters.toArray(new Class[filters.size()]));

		initializeWebSocketServerContainer(server);

		start(server);
	}

	private static void initializeWebSocketServerContainer(JettyServer server)
	{
		try
		{
			WebSocketServerContainerInitializer.initialize(server.getWebAppContext());
		}
		catch (ServletException e)
		{
			throw new RuntimeException(e);
		}
	}

	private FhirServer()
	{
	}
}
