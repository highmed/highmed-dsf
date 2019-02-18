package org.highmed.fhir;

import java.util.Arrays;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@ApplicationPath("/")
public final class FhirJerseyApplication extends ResourceConfig
{
	private static final Logger logger = LoggerFactory.getLogger(FhirJerseyApplication.class);

	@Inject
	public FhirJerseyApplication(ServletContext servletContext)
	{
		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);

		context.getBeansWithAnnotation(Path.class).forEach((n, b) ->
		{
			Optional<Path> path = Arrays.stream(b.getClass().getInterfaces()).map(i -> i.getAnnotation(Path.class))
					.filter(p -> p != null).findFirst();

			logger.debug("Registering bean '{}' as singleton resource with path '{}'", n,
					servletContext.getContextPath() + "/" + path.map(Path::value).orElse("?unknown?"));

			register(b);
		});

		context.getBeansWithAnnotation(Provider.class).forEach((n, b) ->
		{
			logger.debug("Registering bean '{}' as singleton provider", n);

			register(b);
		});
	}
}
