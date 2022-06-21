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
	@Documentation(required = true, description = "The address of the database used for the DSF FHIR server", recommendation = "Change only if you don't use the provided docker-compose from the installation guide or made changes to the database settings/networking in the docker-compose", example = "jdbc:postgresql://db/fhir")
	@Value("${org.highmed.dsf.fhir.db.url}")
	private String dbUrl;

	@Documentation(description = "The user name to access the database from the DSF FHIR server")
	@Value("${org.highmed.dsf.fhir.db.user.username:fhir_server_user}")
	private String dbUsername;

	@Documentation(required = true, description = "The password to access the database from the DSF FHIR server", recommendation = "Use docker secret file to configure using *${env_variable}_FILE*", example = "/run/secrets/db_user.password")
	@Value("${org.highmed.dsf.fhir.db.user.password}")
	private char[] dbPassword;

	@Documentation(description = "The user name to access the database from the DSF FHIR server for permanent deletes", recommendation = "Use a different user then *ORG_HIGHMED_DSF_FHIR_DB_USER_USERNAME*")
	@Value("${org.highmed.dsf.fhir.db.user.permanent.delete.username:fhir_server_permanent_delete_user}")
	private String dbPermanentDeleteUsername;

	@Documentation(required = true, description = "The password to access the database from the DSF FHIR server for permanent deletes", recommendation = "Use docker secret file to configure using *${env_variable}_FILE*", example = "/run/secrets/db_user_permanent_delete.password")
	@Value("${org.highmed.dsf.fhir.db.user.permanent.delete.password}")
	private char[] dbPermanentDeletePassword;

	@Documentation(required = true, description = "The base address of this DSF FHIR server to read/store fhir resources", example = "https://foo.bar/fhir")
	@Value("${org.highmed.dsf.fhir.server.base.url}")
	private String serverBaseUrl;

	@Documentation(description = "The page size returned by the DSF FHIR server when reading/searching fhir resources")
	@Value("${org.highmed.dsf.fhir.server.page.count:20}")
	private int defaultPageCount;

	@Documentation(required = true, description = "List of SHA512 thumbprints as hex from local client certificates that can be used to access the DSF FHIR server; comma or space separated list, YAML block scalars supported", recommendation = "Besides the DSF BPE client certificate thumbprint, add a second thumbprint of a personal client certificate for administration purposes")
	@Value("#{'${org.highmed.dsf.fhir.server.user.thumbprints}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> userThumbprints;

	@Documentation(required = true, description = "List of SHA512 thumbprints as hex from local client certificates that can be used to access the DSF FHIR server for permanent deletes; comma or space separated list, YAML block scalars supported", recommendation = "Besides the DSF BPE client certificate thumbprint, add a second thumbprint of a personal client certificate for administration purposes")
	@Value("#{'${org.highmed.dsf.fhir.server.user.thumbprints.permanent.delete}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> userPermanentDeleteThumbprints;

	@Documentation(required = true, description = "The local identifier value used in the Allow-List", recommendation = "By convention: The shortest possible FQDN that resolve the homepage of the organization", example = "hospital.com")
	@Value("${org.highmed.dsf.fhir.server.organization.identifier.value}")
	private String organizationIdentifierValue;

	@Documentation(description = "The fhir bundle containing the initial Allow-List, loaded on startup of the DSF FHIR server", recommendation = "Change only if you don't use the provided files from the installation guide, have local changes in the Allow-List or received an Allow-List from another source")
	@Value("${org.highmed.dsf.fhir.server.init.bundle:conf/bundle.xml}")
	private String initBundleFile;

	@Documentation(required = true, description = "PEM encoded file with one or more trusted root certificates to validate server certificates for https connections to remote DSF FHIR servers", recommendation = "Use docker secret file to configure", example = "/run/secrets/app_client_trust_certificates.pem")
	@Value("${org.highmed.dsf.fhir.client.trust.certificates}")
	private String webserviceClientCertificateTrustCertificatesFile;

	@Documentation(required = true, description = "PEM encoded file with local client certificate for https connections to remote DSF FHIR servers", recommendation = "Use docker secret file to configure", example = "/run/secrets/app_client_certificate.pem")
	@Value("${org.highmed.dsf.fhir.client.certificate}")
	private String webserviceClientCertificateFile;

	@Documentation(required = true, description = "Private key corresponding to the local client certificate as PEM encoded file. Use *${env_variable}_PASSWORD* or *${env_variable}_PASSWORD_FILE* if private key is encrypted", recommendation = "Use docker secret file to configure", example = "/run/secrets/app_client_certificate_private_key.pem")
	@Value("${org.highmed.dsf.fhir.client.certificate.private.key}")
	private String webserviceClientCertificatePrivateKeyFile;

	@Documentation(description = "Password to decrypt the local client certificate encrypted private key", recommendation = "Use docker secret file to configure using *${env_variable}_FILE*", example = "/run/secrets/app_client_certificate_private_key.pem.password")
	@Value("${org.highmed.dsf.fhir.client.certificate.private.key.password:#{null}}")
	private char[] webserviceClientCertificatePrivateKeyFilePassword;

	@Documentation(description = "The timeout in milliseconds until a reading a resource from a remote DSF FHIR server is aborted", recommendation = "Change default value only if timeout exceptions occur")
	@Value("${org.highmed.dsf.fhir.client.timeout.read:10000}")
	private int webserviceClientReadTimeout;

	@Documentation(description = "The timeout in milliseconds until a connection is established between this DSF FHIR server and a remote DSF FHIR server", recommendation = "Change default value only if timeout exceptions occur")
	@Value("${org.highmed.dsf.fhir.client.timeout.connect:2000}")
	private int webserviceClientConnectTimeout;

	@Documentation(description = "Proxy location, set if the DSF FHIR server can reach the internet only through a proxy", example = "http://proxy.foo:8080")
	@Value("${org.highmed.dsf.fhir.client.proxy.url:#{null}}")
	private String webserviceClientProxyUrl;

	@Documentation(description = "Proxy username, set if the the DSF FHIR server can reach the internet only through a proxy which requests authentication")
	@Value("${org.highmed.dsf.fhir.client.proxy.username:#{null}}")
	private String webserviceClientProxyUsername;

	@Documentation(description = "Proxy password, set if the the DSF FHIR server can reach the internet only through a proxy which requests authentication", recommendation = "Use docker secret file to configure using *${env_variable}_FILE*")
	@Value("${org.highmed.dsf.fhir.client.proxy.password:#{null}}")
	private char[] webserviceClientProxyPassword;

	@Documentation(description = "To enable verbose logging of requests to and replies from remote DSF FHIR servers, set to `true`")
	@Value("${org.highmed.dsf.fhir.client.verbose:false}")
	private boolean webserviceClientVerbose;

	@Documentation(description = "List of allowed CORS origins, used to set the *Access-Control-Allow-Origin* HTTP response header, which indicates whether the response can be shared with requesting code from the given origin; comma or space separated list, YAML block scalars supported")
	@Value("#{'${org.highmed.dsf.fhir.server.cors.origins:}'.trim().split('(,[ ]?)|(\\n)')}")
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

	public boolean getWebserviceClientVerbose()
	{
		return webserviceClientVerbose;
	}

	public List<String> getAllowedOrigins()
	{
		return Collections.unmodifiableList(allowedOrigins);
	}
}
