package org.highmed.fhir;

import static de.rwh.utils.jetty.JettyServer.forwardedSecureRequestCustomizer;
import static de.rwh.utils.jetty.JettyServer.httpConfiguration;
import static de.rwh.utils.jetty.JettyServer.httpConnector;
import static de.rwh.utils.jetty.JettyServer.start;
import static de.rwh.utils.jetty.JettyServer.statusCodeOnlyErrorHandler;
import static de.rwh.utils.jetty.JettyServer.webInfClassesDirs;
import static de.rwh.utils.jetty.JettyServer.webInfJars;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.servlet.ServletException;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.glassfish.jersey.servlet.init.JerseyServletContainerInitializer;
import org.highmed.fhir.authentication.AuthenticationFilter;
import org.springframework.web.SpringServletContainerInitializer;

import de.rwh.utils.jetty.JettyServer;
import de.rwh.utils.jetty.Log4jInitializer;
import de.rwh.utils.jetty.PropertiesReader;

public class FhirJettyServer
{
	public static void main(String[] args)
	{
		Properties properties = PropertiesReader.read(Paths.get("conf/jetty.properties"), StandardCharsets.UTF_8);

		Log4jInitializer.initializeLog4j(properties);

		HttpConfiguration httpConfiguration = httpConfiguration(forwardedSecureRequestCustomizer());
		Function<Server, ServerConnector> connector = httpConnector(httpConfiguration, properties);

		Properties initParameter = PropertiesReader.read(Paths.get("conf/config.properties"), StandardCharsets.UTF_8);

		Predicate<String> filter = s -> s.contains("fhir-demo-server");
		Stream<String> webInfClassesDirs = webInfClassesDirs(filter);
		Stream<String> webInfJars = webInfJars(filter);

		List<Class<?>> initializers = Arrays.asList(SpringServletContainerInitializer.class,
				JerseyServletContainerInitializer.class);

		ErrorHandler errorHandler = statusCodeOnlyErrorHandler();

		JettyServer server = new JettyServer(connector, errorHandler, "/fhir", initializers, initParameter,
				webInfClassesDirs, webInfJars, AuthenticationFilter.class);

		initializeWebSocketServerContainer(server);

		start(server);
	}

	private static void initializeWebSocketServerContainer(JettyServer server)
	{
		try
		{
			WebSocketServerContainerInitializer.configureContext(server.getWebAppContext());
		}
		catch (ServletException e)
		{
			throw new RuntimeException(e);
		}
	}
}
