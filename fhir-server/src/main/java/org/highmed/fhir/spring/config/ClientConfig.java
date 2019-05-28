package org.highmed.fhir.spring.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.highmed.fhir.client.ClientProvider;
import org.highmed.fhir.client.ClientProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

@Configuration
public class ClientConfig
{
	@Value("${org.highmed.fhir.webservice.keystore.p12file}")
	private String webserviceKeyStoreFile;

	@Value("${org.highmed.fhir.webservice.keystore.password}")
	private String webserviceKeyStorePassword;

	@Value("${org.highmed.fhir.remote.webservice.readTimeout}")
	private int remoteReadTimeout;

	@Value("${org.highmed.fhir.remote.webservice.connectTimeout}")
	private int remoteConnectTimeout;

	@Value("${org.highmed.fhir.remote.webservice.proxy.password:#{null}}")
	private String remoteProxyPassword;

	@Value("${org.highmed.fhir.remote.webservice.proxy.username:#{null}}")
	private String remoteProxyUsername;

	@Value("${org.highmed.fhir.remote.webservice.proxy.schemeHostPort:#{null}}")
	private String remoteProxySchemeHostPort;

	@Autowired
	private FhirConfig fhirConfig;

	@Autowired
	private DaoConfig daoConfig;

	@Autowired
	private HelperConfig helperConfig;

	@Bean
	public ClientProvider clientProvider()
	{
		try
		{
			Path webserviceKsFile = Paths.get(webserviceKeyStoreFile);

			if (!Files.isReadable(webserviceKsFile))
				throw new IOException("Webservice keystore file '" + webserviceKsFile.toString() + "' not readable");

			KeyStore webserviceKeyStore = CertificateReader.fromPkcs12(webserviceKsFile, webserviceKeyStorePassword);
			KeyStore webserviceTrustStore = CertificateHelper.extractTrust(webserviceKeyStore);

			return new ClientProviderImpl(webserviceTrustStore, webserviceKeyStore, webserviceKeyStorePassword,
					remoteReadTimeout, remoteConnectTimeout, remoteProxyPassword, remoteProxyUsername,
					remoteProxySchemeHostPort, fhirConfig.fhirContext(), daoConfig.endpointDao(),
					helperConfig.exceptionHandler());
		}
		catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
