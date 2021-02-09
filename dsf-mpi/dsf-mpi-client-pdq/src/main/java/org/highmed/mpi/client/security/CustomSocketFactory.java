package org.highmed.mpi.client.security;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.util.StandardSocketFactory;
import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.context.SSLContextFactory;
import de.rwh.utils.crypto.io.CertificateReader;

public class CustomSocketFactory extends StandardSocketFactory
{
	private static final Logger logger = LoggerFactory.getLogger(CustomSocketFactory.class);

	private final String keystorePath;
	private final String keystorePassword;

	/**
	 * @param keystorePath
	 *            the path to the .p12 file containing the client certificate, not <code>null</code>
	 * @param keystorePassword
	 *            the password of the .p12 file containing the client certificate, not <code>null</code>
	 */
	public CustomSocketFactory(String keystorePath, String keystorePassword)
	{
		this.keystorePath = keystorePath;
		this.keystorePassword = keystorePassword;
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
			logger.warn("Could not create custom socket, returning default socket");
			return super.createTlsSocket();
		}
	}

	private SSLContext getSslContext()
	{
		try
		{
			KeyStore keystore = CertificateReader.fromPkcs12(Path.of(keystorePath), keystorePassword.toCharArray());
			KeyStore truststore = CertificateHelper.extractTrust(keystore);
			return new SSLContextFactory().createSSLContext(truststore, keystore, keystorePassword.toCharArray());
		}
		catch (Exception exception)
		{
			logger.warn("Could not load client certificate, reason: {}", exception.getMessage());
			return null;
		}
	}
}
