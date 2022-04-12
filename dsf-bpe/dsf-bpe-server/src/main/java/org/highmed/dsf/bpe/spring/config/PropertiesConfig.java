package org.highmed.dsf.bpe.spring.config;

import java.nio.file.Path;
import java.nio.file.Paths;
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
	@Documentation(required = true, description = "The address of the database used for the DSF BPE server", recommendation = "Change only if you don't use the provided docker-compose from the installation guide or made changes to the database settings/networking in the docker-compose", example = "jdbc:postgresql://db/bpe")
	@Value("${org.highmed.dsf.bpe.db.url}")
	private String dbUrl;

	@Documentation(description = "The user name to access the database from the DSF BPE server")
	@Value("${org.highmed.dsf.bpe.db.user.username:bpe_server_user}")
	private String dbUsername;

	@Documentation(required = true, filePropertySupported = true, description = "The password to access the database from the DSF BPE server", recommendation = "Use docker secret file to configure by using *${env_variable}_FILE*", example = "/run/secrets/db_user.password")
	@Value("${org.highmed.dsf.bpe.db.user.password}")
	private char[] dbPassword;

	@Documentation(description = "The user name to access the database from the DSF BPE server for camunda processes", recommendation = "Use a different user then in *ORG_HIGHMED_DSF_BPE_DB_USER_USERNAME*")
	@Value("${org.highmed.dsf.bpe.db.user.camunda.username:camunda_server_user}")
	private String dbCamundaUsername;

	@Documentation(required = true, filePropertySupported = true, description = "The password to access the database from the DSF BPE server for camunda processes", recommendation = "Use docker secret file to configure by using *${env_variable}_FILE*", example = "/run/secrets/db_user_camunda.password")
	@Value("${org.highmed.dsf.bpe.db.user.camunda.password}")
	private char[] dbCamundaPassword;

	@Documentation(required = true, description = "The local identifier value used in the Allow-List", recommendation = "The convention is to use the shortest possible FQDN of the organization", example = "hospital.com")
	@Value("${org.highmed.dsf.bpe.fhir.server.organization.identifier.value}")
	private String organizationIdentifierValue;

	@Documentation(required = true, description = "PEM encoded file with one or more trusted root certificates to validate DSF client certificates for https connections to local and remote DSF FHIR servers", recommendation = "Use docker secret file to configure", example = "/run/secrets/app_client_trust_certificates.pem")
	@Value("${org.highmed.dsf.bpe.fhir.client.trust.certificates}")
	private String webserviceClientCertificateTrustStoreFile;

	@Documentation(required = true, description = "PEM encoded file with local client certificate for https connections to local and remote DSF FHIR servers", recommendation = "Use docker secret file to configure", example = "/run/secrets/app_client_certificate.pem")
	@Value("${org.highmed.dsf.bpe.fhir.client.certificate}")
	private String webserviceClientCertificateFile;

	@Documentation(required = true, description = "Private key corresponding to the local client certificate as PEM encoded file. Use ${env_variable}_PASSWORD* or *${env_variable}_PASSWORD_FILE* if private key is encrypted", recommendation = "Use docker secret file to configure", example = "/run/secrets/app_client_certificate_private_key.pem")
	@Value("${org.highmed.dsf.bpe.fhir.client.certificate.private.key}")
	private String webserviceClientCertificatePrivateKeyFile;

	@Documentation(filePropertySupported = true, description = "Password to decrypt the local client certificate encrypted private key", recommendation = "Use docker secret file to configure by using *${env_variable}_FILE*", example = "/run/secrets/app_client_certificate_private_key.pem.password")
	@Value("${org.highmed.dsf.bpe.fhir.client.certificate.private.key.password:#{null}}")
	private char[] webserviceClientCertificatePrivateKeyFilePassword;

	@Documentation(description = "The timeout in milliseconds until a reading a resource from a remote DSF FHIR server is aborted", recommendation = "Change default value only if timeout exceptions occur")
	@Value("${org.highmed.dsf.bpe.fhir.client.remote.timeout.read:60000}")
	private int webserviceClientRemoteReadTimeout;

	@Documentation(description = "The timeout in milliseconds until a connection is established with a remote DSF FHIR server", recommendation = "Change default value only if timeout exceptions occur")
	@Value("${org.highmed.dsf.bpe.fhir.client.remote.timeout.connect:5000}")
	private int webserviceClientRemoteConnectTimeout;

	@Documentation(description = "Proxy location, set if the DSF BPE server can reach the internet only through a proxy", example = "http://proxy.foo:8080")
	@Value("${org.highmed.dsf.bpe.fhir.client.remote.proxy.url:#{null}}")
	private String webserviceClientRemoteProxySchemeHostPort;

	@Documentation(description = "Proxy username, set if the the DSF BPE server can reach the internet only through a proxy which requests authentication")
	@Value("${org.highmed.dsf.bpe.fhir.client.remote.proxy.username:#{null}}")
	private String webserviceClientRemoteProxyUsername;

	@Documentation(filePropertySupported = true, description = "Proxy password, set if the the DSF FHIR server can reach the internet only through a proxy which requests authentication", recommendation = "Use docker secret file to configure by using *${env_variable}_FILE*")
	@Value("${org.highmed.dsf.bpe.fhir.client.remote.proxy.password:#{null}}")
	private char[] webserviceClientRemoteProxyPassword;

	@Documentation(required = true, description = "The base address of the local DSF FHIR server to read/store fhir resources", example = "http://foo.bar/fhir")
	@Value("${org.highmed.dsf.bpe.fhir.server.base.url}")
	private String serverBaseUrl;

	@Documentation(description = "The timeout in milliseconds until a reading a resource from the local DSF FHIR server is aborted", recommendation = "Change default value only if timeout exceptions occur")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.timeout.read:60000}")
	private int webserviceClientLocalReadTimeout;

	@Documentation(description = "The timeout in milliseconds until a connection is established with the local DSF FHIR server", recommendation = "Change default value only if timeout exceptions occur")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.timeout.connect:2000}")
	private int webserviceClientLocalConnectTimeout;

	@Documentation(description = "Proxy location, set if the DSF BPE server can reach internal servers, like the DSF FHIR server, only through a proxy", example = "http://proxy.foo:8080")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.proxy.url:#{null}}")
	private String webserviceClientLocalProxySchemeHostPort;

	@Documentation(description = "Proxy username, set if the DSF BPE server can reach internal servers, like the DSF FHIR server, only through a proxy which requests authentication")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.proxy.username:#{null}}")
	private String webserviceClientLocalProxyUsername;

	@Documentation(filePropertySupported = true, description = "Proxy password, set if the DSF BPE server can reach internal servers, like the DSF FHIR server, only through a proxy which requests authentication", recommendation = "Use docker secret file to configure by using *${env_variable}_FILE*")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.proxy.password:#{null}}")
	private char[] webserviceClientLocalProxyPassword;

	@Documentation(description = "Proxy location, set if the DSF BPE server can reach internal servers via websocket, like the DSF FHIR server, only through a proxy", example = "http://proxy.foo:8080")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.websocket.proxy.url:#{null}}")
	private String websocketClientProxySchemeHostPort;

	@Documentation(description = "Proxy username, set if the DSF BPE server can reach internal servers via websocket, like the DSF FHIR server, only through a proxy which requests authentication")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.websocket.proxy.username:#{null}}")
	private String websocketClientProxyUsername;

	@Documentation(filePropertySupported = true, description = "Proxy password, set if the DSF BPE server can reach internal servers via websocket, like the getSub server, only through a proxy which requests authentication", recommendation = "Use docker secret file to configure by using *${env_variable}_FILE*")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.websocket.proxy.password:#{null}}")
	private char[] websocketClientProxyPassword;

	@Documentation(description = "Subscription to receive notifications about resources from the DSF FHIR server", recommendation = "Change only if you need other subscriptions then the default Task subscription of the DSF")
	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.search.parameter:?criteria=Task%3Fstatus%3Drequested&status=active&type=websocket&payload=application/fhir%2Bjson}")
	private String subscriptionSearchParameter;

	@Documentation(description = "File storing the last event received and processed on the DFS BPE server from the DSF FHIR server, used to load events that occurred on the DSF FHIR server while the DSF BPE server was turned off")
	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.last.event.time:last_event/time.file}")
	private String lastEventTimeFile;

	@Documentation(description = "Number of retries until a websocket connection can be established with the DSF FHIR server, -1 means infinite number of retries")
	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.retry.max:-1}")
	private int websocketMaxRetries;

	@Documentation(description = "Milliseconds between two retries to establish a websocket connection with the DSF FHIR server")
	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.retry.sleep:5000}")
	private long websocketRetrySleepMillis;

	@Documentation(description = "Factory for client implementations used to connect to a Master Patient Index (MPI) server in order to read patient demographic data", recommendation = "The default value is a factory for a stub implementation, change to a factory for client implementation that matches the API of your MPI")
	@Value("${org.highmed.dsf.bpe.mpi.webservice.factory.class:org.highmed.mpi.client.stub.MasterPatientIndexClientStubFactory}")
	private String masterPatientIndexClientFactoryClass;

	@Documentation(description = "Factory for client implementations used to connect to a consent server in order to check permissions to access patient medical data", recommendation = "The default value is a factory for a stub implementation, change to a factory for client implementation that matches the API of your Consent server")
	@Value("${org.highmed.dsf.bpe.consent.webservice.factory.class:org.highmed.consent.client.stub.ConsentClientStubFactory}")
	private String consentClientFactoryClass;

	@Documentation(description = "Factory for client implementations used to connect to an openEHR repository in order to read patient medical data", recommendation = "The default value is a factory for a stub implementation, change to a factory for client implementation that matches the API of your openEHR repository")
	@Value("${org.highmed.dsf.bpe.openehr.webservice.factory.class:org.highmed.openehr.client.stub.OpenEhrClientStubFactory}")
	private String openEhrClientFactoryClass;

	@Documentation(description = "Factory for client implementations used to connect to a pseudonymization service in order to pseudonymize patient demographic and medical data", recommendation = "The default value is a factory for a stub implementation, change to a factory for client implementation that matches the API of your pseudonymization service")
	@Value("${org.highmed.dsf.bpe.pseudonymization.webservice.factory.class:org.highmed.pseudonymization.client.stub.PseudonymizationClientStubFactory}")
	private String pseudonymizationClientFactoryClass;

	@Documentation(description = "Directory containing the DSF BPE process plugins for deployment on startup of the DSF BPE server", recommendation = "Change only if you don't use the provided directory structure from the installation guide or made changes to tit")
	@Value("${org.highmed.dsf.bpe.process.plugin.directroy:process}")
	private String processPluginDirectory;

	@Documentation(description = "List of process names that should be excluded from deployment during startup of the DSF BPE server", recommendation = "Only deploy processes that can be started depending on your organization's roles in the Allow-List")
	@Value("#{'${org.highmed.dsf.bpe.process.excluded:}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> processExcluded;

	@Documentation(description = "List of already deployed process names that should be retired during startup of the DSF BPE server", recommendation = "Retire processes that where deployed previously but are not anymore available")
	@Value("#{'${org.highmed.dsf.bpe.process.retired:}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> processRetired;

	@Documentation(description = "Number of retries until a connection can be established with the local DSF FHIR server during process deployment, -1 means infinite number of retries")
	@Value("${org.highmed.dsf.bpe.process.fhir.server.retry.max:-1}")
	private int fhirServerRequestMaxRetries;

	@Documentation(description = "Milliseconds between two retries to establish a connection with the local DSF FHIR server during process deployment")
	@Value("${org.highmed.dsf.bpe.process.fhir.server.retry.sleep:5000}")
	private long fhirServerRetryDelayMillis;

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

	public String getDbCamundaUsername()
	{
		return dbCamundaUsername;
	}

	public char[] getDbCamundaPassword()
	{
		return dbCamundaPassword;
	}

	public String getOrganizationIdentifierValue()
	{
		return organizationIdentifierValue;
	}

	public String getWebserviceClientCertificateTrustStoreFile()
	{
		return webserviceClientCertificateTrustStoreFile;
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

	public int getWebserviceClientRemoteReadTimeout()
	{
		return webserviceClientRemoteReadTimeout;
	}

	public int getWebserviceClientRemoteConnectTimeout()
	{
		return webserviceClientRemoteConnectTimeout;
	}

	public String getWebserviceClientRemoteProxySchemeHostPort()
	{
		return webserviceClientRemoteProxySchemeHostPort;
	}

	public String getWebserviceClientRemoteProxyUsername()
	{
		return webserviceClientRemoteProxyUsername;
	}

	public char[] getWebserviceClientRemoteProxyPassword()
	{
		return webserviceClientRemoteProxyPassword;
	}

	public String getServerBaseUrl()
	{
		return serverBaseUrl;
	}

	public int getWebserviceClientLocalReadTimeout()
	{
		return webserviceClientLocalReadTimeout;
	}

	public int getWebserviceClientLocalConnectTimeout()
	{
		return webserviceClientLocalConnectTimeout;
	}

	public String getWebserviceClientLocalProxySchemeHostPort()
	{
		return webserviceClientLocalProxySchemeHostPort;
	}

	public String getWebserviceClientLocalProxyUsername()
	{
		return webserviceClientLocalProxyUsername;
	}

	public char[] getWebserviceClientLocalProxyPassword()
	{
		return webserviceClientLocalProxyPassword;
	}

	public String getWebsocketClientProxySchemeHostPort()
	{
		return websocketClientProxySchemeHostPort;
	}

	public String getWebsocketClientProxyUsername()
	{
		return websocketClientProxyUsername;
	}

	public char[] getWebsocketClientProxyPassword()
	{
		return websocketClientProxyPassword;
	}

	public String getSubscriptionSearchParameter()
	{
		return subscriptionSearchParameter;
	}

	public Path getLastEventTimeFile()
	{
		return Paths.get(lastEventTimeFile);
	}

	public long getWebsocketRetrySleepMillis()
	{
		return websocketRetrySleepMillis;
	}

	public int getWebsocketMaxRetries()
	{
		return websocketMaxRetries;
	}

	public String getMasterPatientIndexClientFactoryClass()
	{
		return masterPatientIndexClientFactoryClass;
	}

	public String getConsentClientFactoryClass()
	{
		return consentClientFactoryClass;
	}

	public String getOpenEhrClientFactoryClass()
	{
		return openEhrClientFactoryClass;
	}

	public String getPseudonymizationClientFactoryClass()
	{
		return pseudonymizationClientFactoryClass;
	}

	public Path getProcessPluginDirectory()
	{
		return Paths.get(processPluginDirectory);
	}

	public List<String> getProcessExcluded()
	{
		return Collections.unmodifiableList(processExcluded);
	}

	public List<String> getProcessRetired()
	{
		return Collections.unmodifiableList(processRetired);
	}

	public int getFhirServerRequestMaxRetries()
	{
		return fhirServerRequestMaxRetries;
	}

	public long getFhirServerRetryDelayMillis()
	{
		return fhirServerRetryDelayMillis;
	}
}
