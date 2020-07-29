package org.highmed.mpi.client.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomSslFactory
{
	private static final Logger logger = LoggerFactory.getLogger(CustomSslFactory.class);

	/**
	 * @param path     the path to the keystore file with ending .p12, not <code>null</code>
	 * @param password the keystore file password, not <code>null</code>
	 * @return the {@link KeyStore} loaded from the specified file using the specified password
	 */
	public KeyStore getKeystoreFromP12File(String path, String password)
			throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException
	{
		return getKeyStore(path, password, "PKCS12");
	}

	/**
	 * @param path     the path to the keystore file with ending .jks, not <code>null</code>
	 * @param password the keystore file password, not <code>null</code>
	 * @return the {@link KeyStore} loaded from the specified file using the specified password
	 */
	public KeyStore getKeystoreFromJksFile(String path, String password)
			throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException
	{
		return getKeyStore(path, password, "JKS");
	}

	private KeyStore getKeyStore(String path, String password, String type)
			throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException
	{
		KeyStore keystore = KeyStore.getInstance(type);
		keystore.load(new FileInputStream(path), password.toCharArray());

		return keystore;
	}

	/**
	 * @param keyStore from which the truststore (e.g. the ca-certificates) should be extracted
	 * @return the {@link KeyStore} containing all trusted certificates
	 */
	public KeyStore extractTruststore(KeyStore keyStore)
			throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
	{
		KeyStore truststore = KeyStore.getInstance("PKCS12");
		truststore.load(null, null);

		for (X509Certificate caCert : getCaCertificates(keyStore))
			truststore.setCertificateEntry(UUID.randomUUID().toString(), caCert);

		return truststore;
	}

	private List<X509Certificate> getCaCertificates(KeyStore keyStore) throws KeyStoreException
	{
		List<X509Certificate> caCertificates = new ArrayList<>();
		for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); )
		{
			String alias = aliases.nextElement();
			Certificate[] chain = keyStore.getCertificateChain(alias);
			if (chain == null)
				chain = new Certificate[] { keyStore.getCertificate(alias) };

			for (Certificate certificate : chain)
			{
				if (certificate instanceof X509Certificate)
				{
					X509Certificate x = (X509Certificate) certificate;
					if (x.getBasicConstraints() >= 0)
						caCertificates.add(x);
				}
			}
		}

		logger.trace("Extracted {} {} from keystore", caCertificates.size(),
				caCertificates.size() == 1 ? "certificate" : "certificates");

		return caCertificates;
	}

	/**
	 * @param keystore the {@link KeyStore}, containing the client certificates, which are loaded into the new
	 *                 key manager factory, may be <code>null</code>
	 * @param password the password for the new key manager factory, not <code>null</code>
	 * @return the new {@link KeyManagerFactory} based on the provided {@link KeyStore}, if the provided keystore
	 * is <code>null</code>, an empty {@link KeyManagerFactory} is created with the provided password.
	 */
	public KeyManagerFactory getKeyManagerFactory(KeyStore keystore, String password)
			throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException
	{
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keystore, password.toCharArray());

		return keyManagerFactory;
	}

	/**
	 * @param truststore the truststore as {@link KeyStore}, containing the trusted server certificates, which are
	 *                   loaded into the new trust manager factory, may be <code>null</code>
	 * @return the new {@link TrustManagerFactory} based on the provided truststore, returns the default trust manager
	 * if the provided truststore is <code>null</code>.
	 */
	public TrustManagerFactory getTrustManagerFactory(KeyStore truststore)
			throws NoSuchAlgorithmException, KeyStoreException
	{
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(truststore);

		return trustManagerFactory;
	}

	/**
	 * @param trustManagerFactory the {@link TrustManagerFactory} from which the X509 trust manager should be
	 *                            extracted, not <code>null</code>
	 * @return the {@link X509TrustManager} contained in the provided trust manager factory
	 */
	public X509TrustManager getTrustManager(TrustManagerFactory trustManagerFactory)
	{
		X509TrustManager defaultTrustManager = null;
		for (TrustManager tm : trustManagerFactory.getTrustManagers())
		{
			if (tm instanceof X509TrustManager)
			{
				defaultTrustManager = (X509TrustManager) tm;
				break;
			}
		}

		return defaultTrustManager;
	}

	/**
	 * @param keyManagerFactory   the {@link KeyManagerFactory} containing the client certificates, not <code>null</code>
	 * @param trustManagerFactory the {@link TrustManagerFactory} containing the trusted server certificates,
	 *                            not <code>null</code>
	 * @return the {@link SSLContext} of type TLS based on the provided key manager factory and trust manager factory
	 */
	public SSLContext getTLSContext(KeyManagerFactory keyManagerFactory, TrustManagerFactory trustManagerFactory)
			throws Exception
	{
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

		return sslContext;
	}
}