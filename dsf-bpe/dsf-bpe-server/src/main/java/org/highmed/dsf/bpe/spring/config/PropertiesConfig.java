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

	@Documentation(required = true, description = "The password to access the database from the DSF BPE server", recommendation = "Use docker secret file to configure using *${env_variable}_FILE*", example = "/run/secrets/db_user.password")
	@Value("${org.highmed.dsf.bpe.db.user.password}")
	private char[] dbPassword;

	@Documentation(description = "The user name to access the database from the DSF BPE server for camunda processes", recommendation = "Use a different user then in *ORG_HIGHMED_DSF_BPE_DB_USER_USERNAME*")
	@Value("${org.highmed.dsf.bpe.db.user.camunda.username:camunda_server_user}")
	private String dbCamundaUsername;

	@Documentation(required = true, description = "The password to access the database from the DSF BPE server for camunda processes", recommendation = "Use docker secret file to configure using *${env_variable}_FILE*", example = "/run/secrets/db_user_camunda.password")
	@Value("${org.highmed.dsf.bpe.db.user.camunda.password}")
	private char[] dbCamundaPassword;

	@Documentation(required = true, description = "The local identifier value used in the Allow-List", recommendation = "By convention: The shortest possible FQDN that resolve the homepage of the organization", example = "hospital.com")
	@Value("${org.highmed.dsf.bpe.fhir.server.organization.identifier.value}")
	private String organizationIdentifierValue;

	@Documentation(required = true, description = "PEM encoded file with one or more trusted root certificates to validate server certificates for https connections to local and remote DSF FHIR servers", recommendation = "Use docker secret file to configure", example = "/run/secrets/app_client_trust_certificates.pem")
	@Value("${org.highmed.dsf.bpe.fhir.client.trust.certificates}")
	private String clientCertificateTrustStoreFile;

	@Documentation(required = true, description = "PEM encoded file with local client certificate for https connections to local and remote DSF FHIR servers", recommendation = "Use docker secret file to configure", example = "/run/secrets/app_client_certificate.pem")
	@Value("${org.highmed.dsf.bpe.fhir.client.certificate}")
	private String clientCertificateFile;

	@Documentation(required = true, description = "Private key corresponding to the local client certificate as PEM encoded file. Use ${env_variable}_PASSWORD* or *${env_variable}_PASSWORD_FILE* if private key is encrypted", recommendation = "Use docker secret file to configure", example = "/run/secrets/app_client_certificate_private_key.pem")
	@Value("${org.highmed.dsf.bpe.fhir.client.certificate.private.key}")
	private String clientCertificatePrivateKeyFile;

	@Documentation(description = "Password to decrypt the local client certificate encrypted private key", recommendation = "Use docker secret file to configure using *${env_variable}_FILE*", example = "/run/secrets/app_client_certificate_private_key.pem.password")
	@Value("${org.highmed.dsf.bpe.fhir.client.certificate.private.key.password:#{null}}")
	private char[] clientCertificatePrivateKeyFilePassword;

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

	@Documentation(description = "Proxy password, set if the the DSF FHIR server can reach the internet only through a proxy which requests authentication", recommendation = "Use docker secret file to configure using *${env_variable}_FILE*")
	@Value("${org.highmed.dsf.bpe.fhir.client.remote.proxy.password:#{null}}")
	private char[] webserviceClientRemoteProxyPassword;

	@Documentation(description = "To enable verbose logging of requests to and replies from remote DSF FHIR servers, set to `true`")
	@Value("${org.highmed.dsf.bpe.fhir.client.remote.verbose:false}")
	private boolean webserviceClientRemoteVerbose;

	@Documentation(required = true, description = "The base address of the local DSF FHIR server to read/store fhir resources", example = "https://foo.bar/fhir")
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

	@Documentation(description = "Proxy password, set if the DSF BPE server can reach internal servers, like the DSF FHIR server, only through a proxy which requests authentication", recommendation = "Use docker secret file to configure using *${env_variable}_FILE*")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.proxy.password:#{null}}")
	private char[] webserviceClientLocalProxyPassword;

	@Documentation(description = "To enable verbose logging of requests to and replies from the local DSF FHIR server, set to `true`")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.verbose:false}")
	private boolean webserviceClientLocalVerbose;

	@Documentation(description = "Proxy location, set if the DSF BPE server can reach internal servers via websocket, like the DSF FHIR server, only through a proxy", example = "http://proxy.foo:8080")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.websocket.proxy.url:#{null}}")
	private String websocketClientProxySchemeHostPort;

	@Documentation(description = "Proxy username, set if the DSF BPE server can reach internal servers via websocket, like the DSF FHIR server, only through a proxy which requests authentication")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.websocket.proxy.username:#{null}}")
	private String websocketClientProxyUsername;

	@Documentation(description = "Proxy password, set if the DSF BPE server can reach internal servers via websocket, like the getSub server, only through a proxy which requests authentication", recommendation = "Use docker secret file to configure using *${env_variable}_FILE*")
	@Value("${org.highmed.dsf.bpe.fhir.client.local.websocket.proxy.password:#{null}}")
	private char[] websocketClientProxyPassword;

	@Documentation(description = "Subscription to receive notifications about task resources from the DSF FHIR server")
	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.search.parameter:?criteria=Task%3Fstatus%3Drequested&status=active&type=websocket&payload=application/fhir%2Bjson}")
	private String taskSubscriptionSearchParameter;

	@Documentation(description = "Subscription to receive notifications about questionnaire response resources from the DSF FHIR server")
	@Value("${org.highmed.dsf.bpe.fhir.questionnaire.response.subscription.search.parameter:?criteria=QuestionnaireResponse%3Fstatus%3Dcompleted&status=active&type=websocket&payload=application/fhir%2Bjson}")
	private String questionnaireResponseSubscriptionSearchParameter;

	@Documentation(description = "Number of retries until a websocket connection can be established with the DSF FHIR server, `-1` means infinite number of retries")
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

	@Documentation(description = "List of process names that should be excluded from deployment during startup of the DSF BPE server; comma or space separated list, YAML block scalars supported", recommendation = "Only deploy processes that can be started depending on your organization's roles in the Allow-List")
	@Value("#{'${org.highmed.dsf.bpe.process.excluded:}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> processExcluded;

	@Documentation(description = "List of already deployed process names that should be retired during startup of the DSF BPE server; comma or space separated list, YAML block scalars supported", recommendation = "Retire processes that where deployed previously but are not anymore available")
	@Value("#{'${org.highmed.dsf.bpe.process.retired:}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> processRetired;

	@Documentation(description = "Number of retries until a connection can be established with the local DSF FHIR server during process deployment, `-1` means infinite number of retries")
	@Value("${org.highmed.dsf.bpe.process.fhir.server.retry.max:-1}")
	private int fhirServerRequestMaxRetries;

	@Documentation(description = "Milliseconds between two retries to establish a connection with the local DSF FHIR server during process deployment")
	@Value("${org.highmed.dsf.bpe.process.fhir.server.retry.sleep:5000}")
	private long fhirServerRetryDelayMillis;

	@Documentation(description = "Mail service sender address", example = "sender@localhost")
	@Value("${org.highmed.dsf.bpe.mail.fromAddress:}")
	private String mailFromAddress;

	@Documentation(description = "Mail service recipient addresses, configure at least one; comma or space separated list, YAML block scalars supported", example = "recipient@localhost")
	@Value("#{'${org.highmed.dsf.bpe.mail.toAddresses:}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> mailToAddresses;

	@Documentation(description = "Mail service CC recipient addresses; comma or space separated list, YAML block scalars supported", example = "cc.recipient@localhost")
	@Value("#{'${org.highmed.dsf.bpe.mail.toAddressesCc:}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> mailToAddressesCc;

	@Documentation(description = "Mail service reply to addresses; comma or space separated list, YAML block scalars supported", example = "reply.to@localhost")
	@Value("#{'${org.highmed.dsf.bpe.mail.replyToAddresses:}'.trim().split('(,[ ]?)|(\\n)')}")
	private List<String> mailReplyToAddresses;

	@Documentation(description = "To enable SMTP over TLS (smtps), set to `true`")
	@Value("${org.highmed.dsf.bpe.mail.useSmtps:false}")
	private boolean mailUseSmtps;

	@Documentation(description = "SMTP server hostname", example = "smtp.server.de")
	@Value("${org.highmed.dsf.bpe.mail.host:#{null}}")
	private String mailServerHostname;

	@Documentation(description = "SMTP server port", example = "465")
	@Value("${org.highmed.dsf.bpe.mail.port:0}")
	private int mailServerPort;

	@Documentation(description = "SMTP server authentication username", recommendation = "Configure if the SMTP server reqiures username/password authentication; enable SMTP over TLS via *ORG_HIGHMED_DSF_BPE_MAIL_USESMTPS*")
	@Value("${org.highmed.dsf.bpe.mail.username:#{null}}")
	private String mailServerUsername;

	@Documentation(description = "SMTP server authentication password", recommendation = "Configure if the SMTP server reqiures username/password authentication; use docker secret file to configure using *${env_variable}_FILE*; enable SMTP over TLS via *ORG_HIGHMED_DSF_BPE_MAIL_USESMTPS*")
	@Value("${org.highmed.dsf.bpe.mail.password:#{null}}")
	private char[] mailServerPassword;

	@Documentation(description = "PEM encoded file with one or more trusted root certificates to validate the server certificate of the SMTP server. Requires SMTP over TLS to be enabled via *ORG_HIGHMED_DSF_BPE_MAIL_USESMTPS*", recommendation = "Use docker secret file to configure", example = "/run/secrets/smtp_server_trust_certificates.pem")
	@Value("${org.highmed.dsf.bpe.mail.trust.certificates:#{null}}")
	private String mailServerTrustStoreFile;

	@Documentation(description = "PEM encoded file with client certificate used to authenticate against the SMTP server. Requires SMTP over TLS to be enabled via *ORG_HIGHMED_DSF_BPE_MAIL_USESMTPS*", recommendation = "Use docker secret file to configure", example = "/run/secrets/smtp_server_client_certificate.pem")
	@Value("${org.highmed.dsf.bpe.mail.client.certificate:#{null}}")
	private String mailServerClientCertificateFile;

	@Documentation(description = "Private key corresponging to the SMTP server client certificate as PEM encoded file. Use ${env_variable}_PASSWORD* or *${env_variable}_PASSWORD_FILE* if private key is encrypted. Requires SMTP over TLS to be enabled via *ORG_HIGHMED_DSF_BPE_MAIL_USESMTPS*", recommendation = "Use docker secret file to configure", example = "/run/secrets/smtp_server_client_certificate_private_key.pem")
	@Value("${org.highmed.dsf.bpe.mail.client.certificate.private.key:#{null}}")
	private String mailServerClientCertificatePrivateKeyFile;

	@Documentation(description = "Password to decrypt the local client certificate encrypted private key", recommendation = "Use docker secret file to configure using *${env_variable}_FILE*", example = "/run/secrets/smtp_server_client_certificate_private_key.pem.password")
	@Value("${org.highmed.dsf.bpe.mail.client.certificate.private.key.password:#{null}}")
	private char[] mailServerClientCertificatePrivateKeyFilePassword;

	@Documentation(description = "PKCS12 encoded file with S/MIME certificate, private key and certificate chain to enable send mails to be S/MIME signed", recommendation = "Use docker secret file to configure", example = "/run/secrets/smime_certificate.p12")
	@Value("${org.highmed.dsf.bpe.mail.smime.p12Keystore:#{null}}")
	private String mailSmimeSigingKeyStoreFile;

	@Documentation(description = "Password to decrypt the PKCS12 encoded S/MIMIE certificate file", recommendation = "Use docker secret file to configure using *${env_variable}_FILE*", example = "/run/secrets/smime_certificate.p12.password")
	@Value("${org.highmed.dsf.bpe.mail.smime.p12Keystore.password:#{null}}")
	private char[] mailSmimeSigingKeyStorePassword;

	@Documentation(description = "To enable a test mail being send on startup of the BPE, set to `true`. Requires SMTP server to be configured.")
	@Value("${org.highmed.dsf.bpe.mail.sendTestMailOnStartup:false}")
	private boolean sendTestMailOnStartup;

	@Documentation(description = "To enable mails being send for every ERROR logged, set to `true`. Requires SMTP server to be configured.")
	@Value("${org.highmed.dsf.bpe.mail.sendMailOnErrorLogEvent:false}")
	private boolean sendMailOnErrorLogEvent;

	@Documentation(description = "Number of previous INFO, WARN log messages to include in ERROR log event mails (>=0). Requires send mail on ERROR log event option to be enabled to have an effect.")
	@Value("${org.highmed.dsf.bpe.mail.mailOnErrorLogEventBufferSize:4}")
	private int mailOnErrorLogEventBufferSize;

	@Documentation(description = "Location of the BPE debug log as displayed in the footer of ERROR log event mails, does not modify the actual location of the debug log file. Requires send mail on ERROR log event option to be enabled to have an effect.")
	@Value("${org.highmed.dsf.bpe.mail.mailOnErrorLogEventDebugLogLocation:/opt/bpe/log/bpe.log}")
	private String mailOnErrorLogEventDebugLogLocation;

	@Documentation(description = "To enable debug log messages for every bpmn activity start, set to `true`.", recommendation = "This debug function should only be activated during process plugin development.")
	@Value("${org.highmed.dsf.bpe.debug.log.message.onActivityStart:false}")
	private boolean debugLogMessageOnActivityStart;

	@Documentation(description = "To enable debug log messages for every bpmn activity end, set to `true`.", recommendation = "This debug function should only be activated during process plugin development.")
	@Value("${org.highmed.dsf.bpe.debug.log.message.onActivityEnd:false}")
	private boolean debugLogMessageOnActivityEnd;

	@Documentation(description = "To enable loging bpmn variables for every bpmn activity start or end, when logging of these events is enabled, set to `true`.", recommendation = "This debug function should only be activated during process plugin development. WARNNING: Confidential information may be leaked via the debug log!")
	@Value("${org.highmed.dsf.bpe.debug.log.message.variables:false}")
	private boolean debugLogMessageVariables;

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

	public String getClientCertificateTrustStoreFile()
	{
		return clientCertificateTrustStoreFile;
	}

	public String getClientCertificateFile()
	{
		return clientCertificateFile;
	}

	public String getClientCertificatePrivateKeyFile()
	{
		return clientCertificatePrivateKeyFile;
	}

	public char[] getClientCertificatePrivateKeyFilePassword()
	{
		return clientCertificatePrivateKeyFilePassword;
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

	public boolean getWebserviceClientRemoteVerbose()
	{
		return webserviceClientRemoteVerbose;
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

	public boolean getWebserviceClientLocalVerbose()
	{
		return webserviceClientLocalVerbose;
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

	public String getTaskSubscriptionSearchParameter()
	{
		return taskSubscriptionSearchParameter;
	}

	public String getQuestionnaireResponseSubscriptionSearchParameter()
	{
		return questionnaireResponseSubscriptionSearchParameter;
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

	public String getMailFromAddress()
	{
		return mailFromAddress;
	}

	public List<String> getMailToAddresses()
	{
		return mailToAddresses;
	}

	public List<String> getMailToAddressesCc()
	{
		return mailToAddressesCc;
	}

	public List<String> getMailReplyToAddresses()
	{
		return mailReplyToAddresses;
	}

	public boolean getMailUseSmtps()
	{
		return mailUseSmtps;
	}

	public String getMailServerHostname()
	{
		return mailServerHostname;
	}

	public int getMailServerPort()
	{
		return mailServerPort;
	}

	public String getMailServerUsername()
	{
		return mailServerUsername;
	}

	public char[] getMailServerPassword()
	{
		return mailServerPassword;
	}

	public String getMailServerTrustStoreFile()
	{
		return mailServerTrustStoreFile;
	}

	public String getMailServerClientCertificateFile()
	{
		return mailServerClientCertificateFile;
	}

	public String getMailServerClientCertificatePrivateKeyFile()
	{
		return mailServerClientCertificatePrivateKeyFile;
	}

	public char[] getMailServerClientCertificatePrivateKeyFilePassword()
	{
		return mailServerClientCertificatePrivateKeyFilePassword;
	}

	public String getMailSmimeSigingKeyStoreFile()
	{
		return mailSmimeSigingKeyStoreFile;
	}

	public char[] getMailSmimeSigingKeyStorePassword()
	{
		return mailSmimeSigingKeyStorePassword;
	}

	public boolean getSendTestMailOnStartup()
	{
		return sendTestMailOnStartup;
	}

	public boolean getSendMailOnErrorLogEvent()
	{
		return sendMailOnErrorLogEvent;
	}

	public int getMailOnErrorLogEventBufferSize()
	{
		return mailOnErrorLogEventBufferSize;
	}

	public String getMailOnErrorLogEventDebugLogLocation()
	{
		return mailOnErrorLogEventDebugLogLocation;
	}

	public boolean getDebugLogMessageOnActivityStart()
	{
		return debugLogMessageOnActivityStart;
	}

	public boolean getDebugLogMessageOnActivityEnd()
	{
		return debugLogMessageOnActivityEnd;
	}

	public boolean getDebugLogMessageVariables()
	{
		return debugLogMessageVariables;
	}
}
