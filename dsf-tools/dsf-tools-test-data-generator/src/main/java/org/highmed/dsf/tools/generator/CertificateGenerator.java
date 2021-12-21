package org.highmed.dsf.tools.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Streams;

import de.rwh.utils.crypto.CertificateAuthority;
import de.rwh.utils.crypto.CertificateAuthority.CertificateAuthorityBuilder;
import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.CertificationRequestBuilder;
import de.rwh.utils.crypto.io.CertificateWriter;
import de.rwh.utils.crypto.io.CsrIo;
import de.rwh.utils.crypto.io.PemIo;

public class CertificateGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(CertificateGenerator.class);

	private static final char[] CERT_PASSWORD = "password".toCharArray();

	private static final String[] SERVER_COMMON_NAMES = { "ttp", "medic1", "medic2", "medic3", "localhost" };
	private static final String[] CLIENT_COMMON_NAMES = { "ttp-client", "medic1-client", "medic2-client",
			"medic3-client", "test-client", "Webbrowser Test User" };

	private static final Map<String, List<String>> DNS_NAMES = Map.of("localhost",
			Arrays.asList("localhost", "fhir", "ttp-docker", "medic1-docker", "medic2-docker", "medic3-docker"));

	private static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();

	private static enum CertificateType
	{
		CLIENT, SERVER
	}

	public static final class CertificateFiles
	{
		private final String commonName;

		private final KeyPair keyPair;
		private final X509Certificate certificate;

		private final byte[] certificateSha512Thumbprint;

		CertificateFiles(String commonName, KeyPair keyPair, Path keyPairPrivateKeyFile, X509Certificate certificate,
				byte[] certificateSha512Thumbprint)
		{
			this.commonName = commonName;
			this.keyPair = keyPair;
			this.certificate = certificate;
			this.certificateSha512Thumbprint = certificateSha512Thumbprint;
		}

		public String getCommonName()
		{
			return commonName;
		}

		public X509Certificate getCertificate()
		{
			return certificate;
		}

		public String getCertificateSha512ThumbprintHex()
		{
			return Hex.encodeHexString(certificateSha512Thumbprint);
		}
	}

	private CertificateAuthority ca;
	private Map<String, CertificateFiles> serverCertificateFilesByCommonName;
	private Map<String, CertificateFiles> clientCertificateFilesByCommonName;

	public void generateCertificates()
	{
		ca = initCA();

		serverCertificateFilesByCommonName = Arrays.stream(SERVER_COMMON_NAMES)
				.map(commonName -> createCert(CertificateType.SERVER, commonName,
						DNS_NAMES.getOrDefault(commonName, Collections.singletonList(commonName))))
				.collect(Collectors.toMap(CertificateFiles::getCommonName, Function.identity()));
		clientCertificateFilesByCommonName = Arrays.stream(CLIENT_COMMON_NAMES)
				.map(commonName -> createCert(CertificateType.CLIENT, commonName, Collections.emptyList()))
				.collect(Collectors.toMap(CertificateFiles::getCommonName, Function.identity()));

		writeThumbprints();
	}

	public Map<String, CertificateFiles> getServerCertificateFilesByCommonName()
	{
		return serverCertificateFilesByCommonName != null
				? Collections.unmodifiableMap(serverCertificateFilesByCommonName)
				: Collections.emptyMap();
	}

	public Map<String, CertificateFiles> getClientCertificateFilesByCommonName()
	{
		return clientCertificateFilesByCommonName != null
				? Collections.unmodifiableMap(clientCertificateFilesByCommonName)
				: Collections.emptyMap();
	}

	public CertificateAuthority initCA()
	{
		Path caCertFile = createFolderIfNotExists(Paths.get("cert/ca/testca_certificate.pem"));
		Path caPrivateKeyFile = createFolderIfNotExists(Paths.get("cert/ca/testca_private-key.pem"));

		if (Files.isReadable(caCertFile) && Files.isReadable(caPrivateKeyFile))
		{
			logger.info("Initializing CA from cert file: {}, private key {}", caCertFile.toString(),
					caPrivateKeyFile.toString());

			X509Certificate caCertificate = readCertificate(caCertFile);
			PrivateKey caPrivateKey = readPrivatekey(caPrivateKeyFile);

			return CertificateAuthorityBuilder.create(caCertificate, caPrivateKey).initialize();
		}
		else
		{
			logger.info("Initializing CA with new cert file: {}, private key {}", caCertFile.toString(),
					caPrivateKeyFile.toString());

			CertificateAuthority ca = CertificateAuthorityBuilder.create("DE", null, null, null, null, "Test")
					.initialize();

			writeCertificate(caCertFile, ca.getCertificate());
			writePrivateKeyEncrypted(caPrivateKeyFile, ca.getCaKeyPair().getPrivate());

			return ca;
		}
	}

	private void writePrivateKeyEncrypted(Path privateKeyFile, PrivateKey privateKey)
	{
		try
		{
			PemIo.writeAes128EncryptedPrivateKeyToPkcs8(PROVIDER, privateKeyFile, privateKey, CERT_PASSWORD);
		}
		catch (IOException | OperatorCreationException e)
		{
			logger.error("Error while writing encrypted private-key to " + privateKeyFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private void writePrivateKeyNotEncrypted(Path privateKeyFile, PrivateKey privateKey)
	{
		try
		{
			PemIo.writeNotEncryptedPrivateKeyToPkcs8(PROVIDER, privateKeyFile, privateKey);
		}
		catch (IOException | OperatorCreationException e)
		{
			logger.error("Error while writing not-encrypted private-key to " + privateKeyFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private void writeCertificate(Path certificateFile, X509Certificate certificate)
	{
		try
		{
			PemIo.writeX509CertificateToPem(certificate, certificateFile);
		}
		catch (CertificateEncodingException | IllegalStateException | IOException e)
		{
			logger.error("Error while writing certificate to " + certificateFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private PrivateKey readPrivatekey(Path privateKeyFile)
	{
		try
		{
			return PemIo.readPrivateKeyFromPem(PROVIDER, privateKeyFile, CERT_PASSWORD);
		}
		catch (IOException | PKCSException e)
		{
			logger.error("Error while reading private-key from " + privateKeyFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private X509Certificate readCertificate(Path certFile)
	{
		try
		{
			return PemIo.readX509CertificateFromPem(certFile);
		}
		catch (CertificateException | IOException e)
		{
			logger.error("Error while reading certificate from " + certFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	public void writeThumbprints()
	{
		Path thumbprintsFile = Paths.get("cert", "thumbprints.txt");

		Stream<String> certificates = Streams
				.concat(serverCertificateFilesByCommonName.values().stream(),
						clientCertificateFilesByCommonName.values().stream())
				.sorted(Comparator.comparing(CertificateFiles::getCommonName))
				.map(c -> c.commonName + "\n\t" + c.getCertificateSha512ThumbprintHex() + " (SHA-512)\n");

		try
		{
			logger.info("Writing certificate thumbprints file to {}", thumbprintsFile.toString());
			Files.write(thumbprintsFile, (Iterable<String>) () -> certificates.iterator(), StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			logger.error("Error while writing certificate thumbprints file to " + thumbprintsFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	public CertificateFiles createCert(CertificateType certificateType, String commonName, List<String> dnsNames)
	{
		Path privateKeyFile = createFolderIfNotExists(getPrivateKeyPath(commonName));
		KeyPair keyPair = createOrReadKeyPair(privateKeyFile, commonName);

		Path certificateRequestFile = createFolderIfNotExists(getCertReqPath(commonName));
		JcaPKCS10CertificationRequest certificateRequest = createOrReadCertificateRequest(certificateRequestFile,
				certificateType, keyPair, commonName, dnsNames);

		Path certificatePemFile = createFolderIfNotExists(getCertPemPath(commonName));
		X509Certificate certificate = signOrReadCertificate(certificatePemFile, certificateRequest,
				keyPair.getPrivate(), commonName, certificateType);

		return new CertificateFiles(commonName, keyPair, privateKeyFile, certificate,
				calculateSha512CertificateThumbprint(certificate));
	}

	private X509Certificate signOrReadCertificate(Path certificateFile,
			JcaPKCS10CertificationRequest certificateRequest, PrivateKey privateKey, String commonName,
			CertificateType certificateType)
	{
		if (Files.isReadable(certificateFile))
		{
			logger.info("Reading certificate (pem) from {} [{}]", certificateFile.toString(), commonName);
			return readCertificate(certificateFile);
		}
		else
		{
			logger.info("Signing {} certificate [{}]", certificateType.toString().toLowerCase(), commonName);
			X509Certificate certificate = signCertificateRequest(certificateRequest, certificateType);

			logger.info("Saving certificate (pem) to {} [{}]", certificateFile.toString(), commonName);
			writeCertificate(certificateFile, certificate);

			return certificate;
		}
	}

	private X509Certificate signCertificateRequest(JcaPKCS10CertificationRequest certificateRequest,
			CertificateType certificateType)
	{
		try
		{
			switch (certificateType)
			{
				case CLIENT:
					return ca.signWebClientCertificate(certificateRequest);
				case SERVER:
					return ca.signWebServerCertificate(certificateRequest);
				default:
					throw new RuntimeException("Unknown certificate type " + certificateType);
			}
		}
		catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | OperatorCreationException
				| CertificateException | IllegalStateException | IOException e)
		{
			logger.error("Error while signing " + certificateType.toString().toLowerCase() + " certificate", e);
			throw new RuntimeException(e);
		}
	}

	private JcaPKCS10CertificationRequest createOrReadCertificateRequest(Path certificateRequestFile,
			CertificateType certificateType, KeyPair keyPair, String commonName, List<String> dnsNames)
	{
		if (!dnsNames.contains(commonName) && CertificateType.SERVER.equals(certificateType))
			throw new IllegalArgumentException("dnsNames must contain commonName if certificateType is SERVER");

		if (Files.isReadable(certificateRequestFile))
		{
			logger.info("Reading certificate request (csr) from {} [{}]", certificateRequestFile.toString(),
					commonName);
			return readCertificateRequest(certificateRequestFile);
		}
		else
		{
			X500Name subject = CertificationRequestBuilder.createSubject("DE", null, null, null, null, commonName);
			JcaPKCS10CertificationRequest certificateRequest = createCertificateRequest(certificateType, subject,
					keyPair, dnsNames);

			logger.info("Saving certificate request (csr) to {} [{}]", certificateRequestFile.toString(), commonName);
			writeCertificateRequest(certificateRequestFile, certificateRequest);

			return certificateRequest;
		}
	}

	private JcaPKCS10CertificationRequest createCertificateRequest(CertificateType certificateType, X500Name subject,
			KeyPair keyPair, List<String> dnsNames)
	{
		try
		{
			switch (certificateType)
			{
				case CLIENT:
					return CertificationRequestBuilder.createClientCertificationRequest(subject, keyPair);
				case SERVER:
					return CertificationRequestBuilder.createServerCertificationRequest(subject, keyPair, null,
							dnsNames);
				default:
					throw new RuntimeException("Unknown certificate type " + certificateType);
			}
		}
		catch (NoSuchAlgorithmException | OperatorCreationException | IllegalStateException | IOException e)
		{
			logger.error("Error while creating certificate-request", e);
			throw new RuntimeException(e);
		}
	}

	private void writeCertificateRequest(Path certificateRequestFile, JcaPKCS10CertificationRequest certificateRequest)
	{
		try
		{
			CsrIo.writeJcaPKCS10CertificationRequestToCsr(certificateRequest, certificateRequestFile);
		}
		catch (IOException e)
		{
			logger.error("Error while reading certificate-request from " + certificateRequestFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private JcaPKCS10CertificationRequest readCertificateRequest(Path certificateRequestFile)
	{
		try
		{
			return CsrIo.readJcaPKCS10CertificationRequestFromCsr(certificateRequestFile);
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e)
		{
			logger.error("Error while reading certificate-request from " + certificateRequestFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private KeyPair createOrReadKeyPair(Path privateKeyFile, String commonName)
	{
		if (Files.isReadable(privateKeyFile))
		{
			logger.info("Reading private-key from {} [{}]", privateKeyFile.toString(), commonName);
			PrivateKey privateKey = readPrivatekey(privateKeyFile);
			PublicKey publicKey = createPublicKey(privateKey, privateKeyFile, commonName);

			return new KeyPair(publicKey, privateKey);
		}
		else
		{
			logger.info("Generating 4096 bit key pair [{}]", commonName);
			KeyPair keyPair = createKeyPair();

			logger.info("Saving private-key to {} [{}]", privateKeyFile.toString(), commonName);
			writePrivateKeyEncrypted(privateKeyFile, keyPair.getPrivate());

			return keyPair;
		}
	}

	private PublicKey createPublicKey(PrivateKey privateKey, Path privateKeyFile, String commonName)
	{
		logger.debug("Generating public-key from private-key [{}]", commonName);

		if ("RSA".equals(privateKey.getAlgorithm()) && privateKey instanceof RSAPrivateCrtKey)
		{
			RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey) privateKey;
			RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(rsaPrivateKey.getModulus(),
					rsaPrivateKey.getPublicExponent());

			try
			{
				KeyFactory factory = KeyFactory.getInstance("RSA");
				return factory.generatePublic(publicKeySpec);
			}
			catch (NoSuchAlgorithmException | InvalidKeySpecException e)
			{
				throw new RuntimeException(
						"Error while generating public key from private key modules and public exponent", e);
			}
		}
		else
			throw new RuntimeException("Error while generating public key: private key for " + commonName + " at "
					+ privateKeyFile + " not a RSA private crt key");
	}

	private KeyPair createKeyPair()
	{
		try
		{
			return CertificationRequestBuilder.createRsaKeyPair4096Bit();
		}
		catch (NoSuchAlgorithmException e)
		{
			logger.error("Error while creating RSA key pair", e);
			throw new RuntimeException(e);
		}
	}

	private Path createFolderIfNotExists(Path file)
	{
		try
		{
			Files.createDirectories(file.getParent());
		}
		catch (IOException e)
		{
			logger.error("Error while creating directories " + file.getParent().toString(), e);
			throw new RuntimeException(e);
		}

		return file;
	}

	private Path getCertReqPath(String commonName)
	{
		commonName = commonName.replaceAll("\\s+", "_");
		return Paths.get("cert", commonName, commonName + "_" + "certificate.csr");
	}

	private Path getCertP12Path(String commonName)
	{
		commonName = commonName.replaceAll("\\s+", "_");
		return Paths.get("cert", commonName, commonName + "_" + "certificate.p12");
	}

	private Path getCertPemPath(String commonName)
	{
		commonName = commonName.replaceAll("\\s+", "_");
		return Paths.get("cert", commonName, commonName + "_" + "certificate.pem");
	}

	private Path getPrivateKeyPath(String commonName)
	{
		commonName = commonName.replaceAll("\\s+", "_");
		return Paths.get("cert", commonName, commonName + "_" + "private-key.pem");
	}

	private byte[] calculateSha512CertificateThumbprint(X509Certificate certificate)
	{
		try
		{
			return MessageDigest.getInstance("SHA-512").digest(certificate.getEncoded());
		}
		catch (CertificateEncodingException | NoSuchAlgorithmException e)
		{
			logger.error("Error while calculating SHA-512 certificate thumbprint", e);
			throw new RuntimeException(e);
		}
	}

	public void copyJavaTestCertificates()
	{
		X509Certificate testCaCertificate = ca.getCertificate();

		Path bpeCaCertFile = Paths.get("../../dsf-bpe/dsf-bpe-server-jetty/target/testca_certificate.pem");
		logger.info("Copying Test CA certificate file to {}", bpeCaCertFile.toString());
		writeCertificate(bpeCaCertFile, testCaCertificate);

		Path fhirCacertFile = Paths.get("../../dsf-fhir/dsf-fhir-server-jetty/target/testca_certificate.pem");
		logger.info("Copying Test CA certificate file to {}", fhirCacertFile.toString());
		writeCertificate(fhirCacertFile, testCaCertificate);

		CertificateFiles localhost = serverCertificateFilesByCommonName.get("localhost");
		KeyStore p12KeyStore = createP12KeyStore(localhost.keyPair.getPrivate(), localhost.commonName,
				localhost.certificate);

		Path bpeP12File = Paths.get("../../dsf-bpe/dsf-bpe-server-jetty/target/localhost_certificate.p12");
		logger.info("Saving localhost certificate p12 file to {}, password '{}' [{}]", bpeP12File.toString(),
				String.valueOf(CERT_PASSWORD), localhost.commonName);
		writeP12File(bpeP12File, p12KeyStore);

		Path fhirP12File = Paths.get("../../dsf-fhir/dsf-fhir-server-jetty/target/localhost_certificate.p12");
		logger.info("Saving localhost certificate p12 file to {}, password '{}' [{}]", fhirP12File.toString(),
				String.valueOf(CERT_PASSWORD), localhost.commonName);
		writeP12File(fhirP12File, p12KeyStore);

		CertificateFiles testClient = clientCertificateFilesByCommonName.get("test-client");

		Path bpeClientCertificateFile = Paths
				.get("../../dsf-bpe/dsf-bpe-server-jetty/target/test-client_certificate.pem");
		logger.info("Copying test-client certificate file to {}", bpeClientCertificateFile);
		writeCertificate(bpeClientCertificateFile, testClient.certificate);

		Path bpeClientPrivateKeyFile = Paths
				.get("../../dsf-bpe/dsf-bpe-server-jetty/target/test-client_private-key.pem");
		logger.info("Copying test-client certificate file to {}", bpeClientPrivateKeyFile);
		writePrivateKeyEncrypted(bpeClientPrivateKeyFile, testClient.keyPair.getPrivate());

		Path fhirClientCertificateFile = Paths
				.get("../../dsf-fhir/dsf-fhir-server-jetty/target/test-client_certificate.pem");
		logger.info("Copying test-client certificate file to {}", fhirClientCertificateFile);
		writeCertificate(fhirClientCertificateFile, testClient.certificate);

		Path fhirClientPrivateKeyFile = Paths
				.get("../../dsf-fhir/dsf-fhir-server-jetty/target/test-client_private-key.pem");
		logger.info("Copying test-client certificate file to {}", fhirClientPrivateKeyFile);
		writePrivateKeyEncrypted(fhirClientPrivateKeyFile, testClient.keyPair.getPrivate());
	}

	public void copyDockerTestCertificates()
	{
		copyProxyFiles("dsf-docker-test-setup", "localhost");
		copyClientCertFiles("../../dsf-docker-test-setup/bpe/secrets/", "../../dsf-docker-test-setup/fhir/secrets/",
				"test-client");
	}

	public void copyDockerTest3MedicTtpCertificates()
	{
		List<String> commonNames = Arrays.asList("medic1", "medic2", "medic3", "ttp");
		commonNames.forEach(cn -> copyProxyFiles("dsf-docker-test-setup-3medic-ttp/" + cn, cn));
		commonNames.forEach(cn -> copyClientCertFiles("../../dsf-docker-test-setup-3medic-ttp/" + cn + "/bpe/secrets/",
				"../../dsf-docker-test-setup-3medic-ttp/" + cn + "/fhir/secrets/", cn + "-client"));
	}

	private void copyProxyFiles(String dockerTestFolder, String commonName)
	{
		X509Certificate testCaCertificate = ca.getCertificate();
		CertificateFiles serverCertFiles = serverCertificateFilesByCommonName.get(commonName);

		Path baseFolder = Paths.get("../../", dockerTestFolder);

		Path bpeTestCaCertificate = baseFolder.resolve("bpe/secrets/testca_certificate.pem");
		logger.info("Copying Test CA certificate file to {}", bpeTestCaCertificate.toString());
		writeCertificate(bpeTestCaCertificate, testCaCertificate);

		Path fhirCertificateFile = baseFolder.resolve("fhir/secrets/server_certificate.pem");
		logger.info("Copying {} certificate pem file to {}", commonName, fhirCertificateFile);
		writeCertificate(fhirCertificateFile, serverCertFiles.getCertificate());

		Path fhirPrivateKeyFile = baseFolder.resolve("fhir/secrets/server_certificate_private_key.pem");
		logger.info("Copying {} private-key file to {}", commonName, fhirPrivateKeyFile);
		writePrivateKeyNotEncrypted(fhirPrivateKeyFile, serverCertFiles.keyPair.getPrivate());

		Path fhirTestCaCertificate = baseFolder.resolve("fhir/secrets/testca_certificate.pem");
		logger.info("Copying Test CA certificate file to {}", fhirTestCaCertificate.toString());
		writeCertificate(fhirTestCaCertificate, testCaCertificate);
	}

	private void copyClientCertFiles(String bpeConfFolder, String fhirConfFolder, String commonName)
	{
		final CertificateFiles clientCertFiles = clientCertificateFilesByCommonName.get(commonName);

		Path bpeClientCertificateFile = Paths.get(bpeConfFolder, "client_certificate.pem");
		logger.info("Copying {} certificate certificate file to {}", commonName, bpeClientCertificateFile);
		writeCertificate(bpeClientCertificateFile, clientCertFiles.certificate);

		Path bpeClientPrivateKeyFile = Paths.get(bpeConfFolder, "client_certificate_private_key.pem");
		logger.info("Copying {} certificate private-key file to {}", commonName, bpeClientPrivateKeyFile);
		writePrivateKeyEncrypted(bpeClientPrivateKeyFile, clientCertFiles.keyPair.getPrivate());

		Path fhirClientCertificateFile = Paths.get(fhirConfFolder, "client_certificate.pem");
		logger.info("Copying {} certificate certificate file to {}", commonName, fhirClientCertificateFile);
		writeCertificate(fhirClientCertificateFile, clientCertFiles.certificate);

		Path fhirClientPrivateKeyFile = Paths.get(fhirConfFolder, "client_certificate_private_key.pem");
		logger.info("Copying {} certificate private-key file to {}", commonName, fhirClientPrivateKeyFile);
		writePrivateKeyEncrypted(fhirClientPrivateKeyFile, clientCertFiles.keyPair.getPrivate());
	}

	public void copyDockerTest3MedicTtpDockerCertificates()
	{
		Path baseFolder = Paths.get("../../dsf-docker-test-setup-3medic-ttp-docker");

		final X509Certificate testCaCertificate = ca.getCertificate();

		Path testCaCertificateFile = baseFolder.resolve("secrets/proxy_trusted_client_cas.pem");
		logger.info("Copying Test CA certificate file to {}", testCaCertificateFile.toString());
		writeCertificate(testCaCertificateFile, testCaCertificate);

		CertificateFiles localhost = serverCertificateFilesByCommonName.get("localhost");

		Path localhostCertificateAndCa = baseFolder.resolve("secrets/proxy_certificate_and_int_cas.pem");
		logger.info("Writing localhost certificate and CA certificate to {}", testCaCertificateFile.toString());
		writeCertificates(localhostCertificateAndCa, localhost.getCertificate(), testCaCertificate);

		Path localhostCertificatePrivateKey = baseFolder.resolve("secrets/proxy_certificate_private_key.pem");
		logger.info("Copying localhost private-key file to {}", localhostCertificatePrivateKey);
		writePrivateKeyNotEncrypted(localhostCertificatePrivateKey, localhost.keyPair.getPrivate());

		List<String> commonNames = Arrays.asList("medic1", "medic2", "medic3", "ttp");
		commonNames.forEach(cn -> copyDockerTest3MedicTtpDockerClientCertFiles(
				"../../dsf-docker-test-setup-3medic-ttp-docker/secrets/", cn + "-client"));

		Path fhirCacertFile = Paths
				.get("../../dsf-docker-test-setup-3medic-ttp-docker/secrets/app_testca_certificate.pem");
		logger.info("Copying Test CA certificate file to {}", fhirCacertFile.toString());
		writeCertificate(fhirCacertFile, testCaCertificate);
	}

	private void copyDockerTest3MedicTtpDockerClientCertFiles(String folder, String commonName)
	{
		final CertificateFiles clientCertFiles = clientCertificateFilesByCommonName.get(commonName);

		Path bpeClientCertificateFile = Paths.get(folder, "app_" + commonName + "_certificate.pem");
		logger.info("Copying {} certificate certificate file to {}", commonName, bpeClientCertificateFile);
		writeCertificate(bpeClientCertificateFile, clientCertFiles.certificate);

		Path bpeClientPrivateKeyFile = Paths.get(folder, "app_" + commonName + "_private-key.pem");
		logger.info("Copying {} certificate private-key file to {}", commonName, bpeClientPrivateKeyFile);
		writePrivateKeyEncrypted(bpeClientPrivateKeyFile, clientCertFiles.keyPair.getPrivate());
	}

	private void writeCertificates(Path certificateFile, X509Certificate... certificates)
	{
		try
		{
			StringBuilder b = new StringBuilder();

			for (X509Certificate cert : certificates)
			{
				b.append("subject= ");
				b.append(cert.getSubjectX500Principal().getName());
				b.append("\n");
				b.append(PemIo.writeX509Certificate(cert));
			}

			Files.writeString(certificateFile, b.toString());
		}
		catch (CertificateEncodingException | IllegalStateException | IOException e)
		{
			logger.error("Error while writing certificate to " + certificateFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private KeyStore createP12KeyStore(PrivateKey privateKey, String commonName, X509Certificate certificate)
	{
		try
		{
			return CertificateHelper.toPkcs12KeyStore(privateKey,
					new Certificate[] { certificate, ca.getCertificate() }, commonName, CERT_PASSWORD);
		}
		catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IllegalStateException
				| IOException e)
		{
			logger.error("Error while creating P12 key-store", e);
			throw new RuntimeException(e);
		}
	}

	private void writeP12File(Path p12File, KeyStore p12KeyStore)
	{
		try
		{
			CertificateWriter.toPkcs12(p12File, p12KeyStore, CERT_PASSWORD);
		}
		catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e)
		{
			logger.error("Error while writing certificate P12 file to " + p12File.toString(), e);
			throw new RuntimeException(e);
		}
	}

	public Path createP12(CertificateFiles files)
	{
		Path certP12Path = getCertP12Path(files.commonName);

		logger.info("Saving certificate (p21) to {}, password '{}' [{}]", certP12Path.toString(),
				String.valueOf(CERT_PASSWORD), files.commonName);
		KeyStore p12KeyStore = createP12KeyStore(files.keyPair.getPrivate(), files.commonName, files.certificate);
		writeP12File(certP12Path, p12KeyStore);

		return certP12Path;
	}

	public static void main(String[] args)
	{
		CertificateAuthority.registerBouncyCastleProvider();
		new CertificateGenerator().generateCertificates();
	}
}
