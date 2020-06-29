package org.highmed.mpi.client.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class CustomSslFactory
{
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
	 * @param keystore the {@link KeyStore}, containing the client certificates, which are loaded into the new
	 *                 key manager factory, may be <code>null</code>
	 * @param password the password for the new key manager factory, not <code>null</code>
	 * @return the new {@link KeyManagerFactory} based on the provided {@link KeyStore}, if the provided keystore
	 * 		   is <code>null</code>, an empty {@link KeyManagerFactory} is created with the provided password.
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
	 * 		   if the provided truststore is <code>null</code>.
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
	 * 							  not <code>null</code>
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