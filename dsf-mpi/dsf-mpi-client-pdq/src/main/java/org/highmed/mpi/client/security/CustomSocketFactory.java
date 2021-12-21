package org.highmed.mpi.client.security;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.util.StandardSocketFactory;
import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.context.SSLContextFactory;
import de.rwh.utils.crypto.io.CertificateReader;
import de.rwh.utils.crypto.io.PemIo;

public class CustomSocketFactory extends StandardSocketFactory
{
	private static final Logger logger = LoggerFactory.getLogger(CustomSocketFactory.class);

	private static final BouncyCastleProvider provider = new BouncyCastleProvider();

	private final String trustCertificatesFile;

	private final String clientCertificateFile;
	private final String clientCertificatePrivateKeyFile;
	private final char[] clientCertificatePrivateKeyPassword;

	/**
	 * @param trustCertificatesFile
	 *            the path to the PEM encoded file containing the chain of trusted certificates, not <code>null</code>
	 * @param clientCertificateFile
	 *            the path to the PEM encoded file containing the client certificate, not <code>null</code>
	 * @param clientCertificatePrivateKeyFile
	 *            the path to the PEM encoded file containing the client certificate private-key, not <code>null</code>
	 * @param clientCertificatePrivateKeyPassword
	 *            if the PEM encoded client certificate private key is encrypted, will be ignored if <code>null</code>
	 */
	public CustomSocketFactory(String trustCertificatesFile, String clientCertificateFile,
			String clientCertificatePrivateKeyFile, char[] clientCertificatePrivateKeyPassword)
	{
		this.trustCertificatesFile = trustCertificatesFile;
		this.clientCertificateFile = clientCertificateFile;
		this.clientCertificatePrivateKeyFile = clientCertificatePrivateKeyFile;
		this.clientCertificatePrivateKeyPassword = clientCertificatePrivateKeyPassword;
	}

	@Override
	public ServerSocket createTlsServerSocket() throws IOException
	{
		logger.warn("Custom TLS server sockets are not supported, returning default TLS server socket");
		return super.createTlsServerSocket();
	}

	@Override
	public Socket createTlsSocket() throws IOException
	{
		SSLContext sslContext = getSslContext();

		if (sslContext != null)
		{
			Socket socket = sslContext.getSocketFactory().createSocket();
			socket.setKeepAlive(false);
			socket.setTcpNoDelay(true);
			return socket;
		}
		else
		{
			logger.warn("Could not create CustomSocketFactory, returning default socket");
			return super.createTlsSocket();
		}
	}

	private SSLContext getSslContext()
	{
		char[] keystorePassword = UUID.randomUUID().toString().toCharArray();

		KeyStore keystore = createKeystore(keystorePassword);
		KeyStore truststore = createTruststore();

		try
		{
			return new SSLContextFactory().createSSLContext(truststore, keystore, keystorePassword);
		}
		catch (Exception exception)
		{
			logger.warn("Could not create SSLContext, reason: {}", exception.getMessage());
			throw new RuntimeException(exception);
		}
	}

	private KeyStore createKeystore(char[] keystorePassword)
	{
		try
		{
			Path certificatePath = Paths.get(clientCertificateFile);
			Path privateKeyPath = Paths.get(clientCertificatePrivateKeyFile);

			if (!Files.isReadable(certificatePath))
				throw new IOException("Certificate file '" + certificatePath.toString() + "' not readable");
			if (!Files.isReadable(certificatePath))
				throw new IOException("Private key file '" + privateKeyPath.toString() + "' not readable");

			X509Certificate certificate = PemIo.readX509CertificateFromPem(certificatePath);
			PrivateKey privateKey = PemIo.readPrivateKeyFromPem(provider, privateKeyPath,
					clientCertificatePrivateKeyPassword);

			String subjectCommonName = CertificateHelper.getSubjectCommonName(certificate);
			return CertificateHelper.toJksKeyStore(privateKey, new Certificate[] { certificate }, subjectCommonName,
					keystorePassword);

		}
		catch (Exception exception)
		{
			logger.warn("Could not create keystore, reason: {}", exception.getMessage());
			throw new RuntimeException(exception);
		}
	}

	private KeyStore createTruststore()
	{
		try
		{
			Path truststorePath = Paths.get(trustCertificatesFile);

			if (!Files.isReadable(truststorePath))
				throw new IOException("Truststore file '" + truststorePath.toString() + "' not readable");
			return CertificateReader.allFromCer(truststorePath);
		}
		catch (Exception exception)
		{
			logger.warn("Could not create truststore, reason: {}", exception.getMessage());
			throw new RuntimeException(exception);
		}
	}
}
