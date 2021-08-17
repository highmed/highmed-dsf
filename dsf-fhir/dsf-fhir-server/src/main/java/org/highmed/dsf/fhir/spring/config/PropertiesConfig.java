package org.highmed.dsf.fhir.spring.config;

import java.util.Collections;
import java.util.List;

import org.highmed.dsf.tools.docker.secrets.DockerSecretsPropertySourceFactory;
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
	@Value("${org.highmed.dsf.fhir.db.url}")
	private String dbUrl;

	@Value("${org.highmed.dsf.fhir.db.user.username:fhir_server_user}")
	private String dbUsername;

	@Value("${org.highmed.dsf.fhir.db.user.password}")
	private char[] dbPassword;

	@Value("${org.highmed.dsf.fhir.db.user.permanent.delete.username:fhir_server_permanent_delete_user}")
	private String dbPermanentDeleteUsername;

	@Value("${org.highmed.dsf.fhir.db.user.permanent.delete.password}")
	private char[] dbPermanentDeletePassword;

	@Value("${org.highmed.dsf.fhir.server.base.url}")
	private String serverBaseUrl;

	@Value("${org.highmed.dsf.fhir.server.page.count:20}")
	private int defaultPageCount;

	@Value("#{'${org.highmed.dsf.fhir.server.user.thumbprints}'.split(',')}")
	private List<String> userThumbprints;

	@Value("#{'${org.highmed.dsf.fhir.server.user.thumbprints.permanent.delete}'.split(',')}")
	private List<String> userPermanentDeleteThumbprints;

	@Value("${org.highmed.dsf.fhir.server.organization.identifier.value}")
	private String organizationIdentifierValue;

	@Value("${org.highmed.dsf.fhir.server.init.bundle:conf/bundle.xml}")
	private String initBundleFile;

	@Value("${org.highmed.dsf.fhir.client.trust.certificates}")
	private String webserviceClientCertificateTrustCertificatesFile;

	@Value("${org.highmed.dsf.fhir.client.certificate}")
	private String webserviceClientCertificateFile;

	@Value("${org.highmed.dsf.fhir.client.certificate.private.key}")
	private String webserviceClientCertificatePrivateKeyFile;

	@Value("${org.highmed.dsf.fhir.client.certificate.private.key.password:#{null}}")
	private char[] webserviceClientCertificatePrivateKeyFilePassword;

	@Value("${org.highmed.dsf.fhir.client.timeout.read:10000}")
	private int webserviceClientReadTimeout;

	@Value("${org.highmed.dsf.fhir.client.timeout.connect:2000}")
	private int webserviceClientConnectTimeout;

	@Value("${org.highmed.dsf.fhir.client.proxy.url:#{null}}")
	private String webserviceClientProxyUrl;

	@Value("${org.highmed.dsf.fhir.client.proxy.username:#{null}}")
	private String webserviceClientProxyUsername;

	@Value("${org.highmed.dsf.fhir.client.proxy.password:#{null}}")
	private char[] webserviceClientProxyPassword;

	@Value("#{'${org.highmed.dsf.fhir.server.cors.origins:}'.split(',')}")
	private List<String> allowedOrigins;

	@Bean // static in order to initialize before @Configuration classes
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
			ConfigurableEnvironment environment)
	{
		new DockerSecretsPropertySourceFactory(environment, "org.highmed.dsf.fhir.db.user.password",
				"org.highmed.dsf.fhir.db.user.permanent.delete.password",
				"org.highmed.dsf.fhir.client.certificate.private.key.password",
				"org.highmed.dsf.fhir.client.proxy.password").readDockerSecretsAndAddPropertiesToEnvironment();

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
