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
	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.db.url}")
	private String dbUrl;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.db.user.username:bpe_server_user}")
	private String dbUsername;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.db.user.password}")
	private char[] dbPassword;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.db.user.camunda.username:camunda_server_user}")
	private String dbCamundaUsername;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.db.user.camunda.password}")
	private char[] dbCamundaPassword;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.server.organization.identifier.value}")
	private String organizationIdentifierValue;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.trust.certificates}")
	private String webserviceClientCertificateTrustStoreFile;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.certificate}")
	private String webserviceClientCertificateFile;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.certificate.private.key}")
	private String webserviceClientCertificatePrivateKeyFile;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.certificate.private.key.password:#{null}}")
	private char[] webserviceClientCertificatePrivateKeyFilePassword;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.remote.timeout.read:60000}")
	private int webserviceClientRemoteReadTimeout;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.remote.timeout.connect:5000}")
	private int webserviceClientRemoteConnectTimeout;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.remote.proxy.url:#{null}}")
	private String webserviceClientRemoteProxySchemeHostPort;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.remote.proxy.username:#{null}}")
	private String webserviceClientRemoteProxyUsername;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.remote.proxy.password:#{null}}")
	private char[] webserviceClientRemoteProxyPassword;

	@Documentation(required = true, description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.server.base.url}")
	private String serverBaseUrl;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.timeout.read:60000}")
	private int webserviceClientLocalReadTimeout;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.timeout.connect:2000}")
	private int webserviceClientLocalConnectTimeout;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.proxy.url:#{null}}")
	private String webserviceClientLocalProxySchemeHostPort;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.proxy.username:#{null}}")
	private String webserviceClientLocalProxyUsername;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.proxy.password:#{null}}")
	private char[] webserviceClientLocalProxyPassword;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.websocket.proxy.url:#{null}}")
	private String websocketClientProxySchemeHostPort;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.websocket.proxy.username:#{null}}")
	private String websocketClientProxyUsername;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.websocket.proxy.password:#{null}}")
	private char[] websocketClientProxyPassword;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.search.parameter:?criteria=Task%3Fstatus%3Drequested&status=active&type=websocket&payload=application/fhir%2Bjson}")
	private String subscriptionSearchParameter;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.last.event.time:last_event/time.file}")
	private String lastEventTimeFile;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.retry.max:-1}")
	private int websocketMaxRetries;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.retry.sleep:5000}")
	private long websocketRetrySleepMillis;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.mpi.webservice.factory.class:org.highmed.mpi.client.stub.MasterPatientIndexClientStubFactory}")
	private String masterPatientIndexClientFactoryClass;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.consent.webservice.factory.class:org.highmed.consent.client.stub.ConsentClientStubFactory}")
	private String consentClientFactoryClass;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.openehr.webservice.factory.class:org.highmed.openehr.client.stub.OpenEhrClientStubFactory}")
	private String openEhrClientFactoryClass;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.pseudonymization.webservice.factory.class:org.highmed.pseudonymization.client.stub.PseudonymizationClientStubFactory}")
	private String pseudonymizationClientFactoryClass;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.process.plugin.directroy:process}")
	private String processPluginDirectory;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("#{'${org.highmed.dsf.bpe.process.excluded:}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> processExcluded;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("#{'${org.highmed.dsf.bpe.process.retired:}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> processRetired;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
	@Value("${org.highmed.dsf.bpe.process.fhir.server.retry.max:-1}")
	private int fhirServerRequestMaxRetries;

	@Documentation(description = "description", recommendation = "recommendation", example = "example")
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
