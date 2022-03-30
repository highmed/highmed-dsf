package org.highmed.dsf.fhir.spring.config;

import java.util.Collections;
import java.util.List;

import org.highmed.dsf.tools.docker.secrets.DockerSecretsPropertySourceFactory;
import org.highmed.dsf.tools.generator.Documentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
@PropertySource(value = "file:conf/config.properties", encoding = "UTF-8", ignoreResourceNotFound = true)
public class PropertiesConfig
{
	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.db.url}")
	private String dbUrl;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.db.user.username:fhir_server_user}")
	private String dbUsername;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.db.user.password}")
	private char[] dbPassword;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.db.user.permanent.delete.username:fhir_server_permanent_delete_user}")
	private String dbPermanentDeleteUsername;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.db.user.permanent.delete.password}")
	private char[] dbPermanentDeletePassword;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.server.base.url}")
	private String serverBaseUrl;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.server.page.count:20}")
	private int defaultPageCount;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("#{'${org.highmed.dsf.fhir.server.user.thumbprints}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> userThumbprints;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("#{'${org.highmed.dsf.fhir.server.user.thumbprints.permanent.delete}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> userPermanentDeleteThumbprints;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.server.organization.identifier.value}")
	private String organizationIdentifierValue;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.server.init.bundle:conf/bundle.xml}")
	private String initBundleFile;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.client.trust.certificates}")
	private String webserviceClientCertificateTrustCertificatesFile;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.client.certificate}")
	private String webserviceClientCertificateFile;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.client.certificate.private.key}")
	private String webserviceClientCertificatePrivateKeyFile;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.client.certificate.private.key.password:#{null}}")
	private char[] webserviceClientCertificatePrivateKeyFilePassword;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.client.timeout.read:10000}")
	private int webserviceClientReadTimeout;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.client.timeout.connect:2000}")
	private int webserviceClientConnectTimeout;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.client.proxy.url:#{null}}")
	private String webserviceClientProxyUrl;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.client.proxy.username:#{null}}")
	private String webserviceClientProxyUsername;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.fhir.client.proxy.password:#{null}}")
	private char[] webserviceClientProxyPassword;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("#{'${org.highmed.dsf.fhir.server.cors.origins:}'.split(',')}")
	private List<String> allowedOrigins;

	@Bean // static in order to initialize before @Configuration classes
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
			ConfigurableEnvironment environment)
	{
		new DockerSecretsPropertySourceFactory(environment).readDockerSecretsAndAddPropertiesToEnvironment();

		return new PropertySourcesPlaceholderConfigurer();
	}

	public String getDbUrl()
	{
		return dbUrl;
	}

	public String getDbUsername()
	{
		return dbUsername;
	}

	public char[] getDbPassword()
	{
		return dbPassword;
	}

	public String getDbPermanentDeleteUsername()
	{
		return dbPermanentDeleteUsername;
	}

	public char[] getDbPermanentDeletePassword()
	{
		return dbPermanentDeletePassword;
	}

	public String getServerBaseUrl()
	{
		return serverBaseUrl;
	}

	public int getDefaultPageCount()
	{
		return defaultPageCount;
	}

	public List<String> getUserThumbprints()
	{
		return Collections.unmodifiableList(userThumbprints);
	}

	public List<String> getUserPermanentDeleteThumbprints()
	{
		return Collections.unmodifiableList(userPermanentDeleteThumbprints);
	}

	public String getOrganizationIdentifierValue()
	{
		return organizationIdentifierValue;
	}

	public String getInitBundleFile()
	{
		return initBundleFile;
	}

	public String getWebserviceClientCertificateTrustCertificatesFile()
	{
		return webserviceClientCertificateTrustCertificatesFile;
	}

	public String getWebserviceClientCertificateFile()
	{
		return webserviceClientCertificateFile;
	}

	public String getWebserviceClientCertificatePrivateKeyFile()
	{
		return webserviceClientCertificatePrivateKeyFile;
	}

	public char[] getWebserviceClientCertificatePrivateKeyFilePassword()
	{
		return webserviceClientCertificatePrivateKeyFilePassword;
	}

	public int getWebserviceClientReadTimeout()
	{
		return webserviceClientReadTimeout;
	}

	public int getWebserviceClientConnectTimeout()
	{
		return webserviceClientConnectTimeout;
	}

	public String getWebserviceClientProxyUrl()
	{
		return webserviceClientProxyUrl;
	}

	public String getWebserviceClientProxyUsername()
	{
		return webserviceClientProxyUsername;
	}

	public char[] getWebserviceClientProxyPassword()
	{
		return webserviceClientProxyPassword;
	}

	public List<String> getAllowedOrigins()
	{
		return Collections.unmodifiableList(allowedOrigins);
	}
}
