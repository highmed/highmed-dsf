package org.highmed.dsf.tools.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
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
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
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
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Streams;

import de.rwh.utils.crypto.CertificateAuthority;
import de.rwh.utils.crypto.CertificateAuthority.CertificateAuthorityBuilder;
import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.CertificationRequestBuilder;
import de.rwh.utils.crypto.io.CertificateReader;
import de.rwh.utils.crypto.io.CertificateWriter;
import de.rwh.utils.crypto.io.CsrIo;
import de.rwh.utils.crypto.io.PemIo;

public class CertificateGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(CertificateGenerator.class);

	private static final String SERVER_DNS_ALTERNATIVE_NAME = "fhir";
	private static final String CERT_PASSWORD = "password";

	private static final String[] SERVER_COMMON_NAMES = { "ttp", "medic1", "medic2", "medic3", "localhost" };
	private static final String[] CLIENT_COMMON_NAMES = { "ttp-client", "medic1-client", "medic2-client",
			"medic3-client", "test-client", "Webbrowser Test User" };

	private static enum CertificateType
	{
		CLIENT, SERVER
	}

	public static final class CertificateFiles
	{
		private final String commonName;
		private final CertificateType certificateType;

		private final JcaPKCS10CertificationRequest certificateRequest;
		private final Path certificateRequestFile;

		private final KeyStore p12KeyStore;
		private final Path p12KeyStoreFile;
		private final KeyPair keyPair;
		private final Path keyPairPrivateKeyFile;
		private final Path keyPairPublicKeyFile;
		private final X509Certificate certificate;
		private final Path certificateFile;

		private final byte[] certificateSha512Thumbprint;

		CertificateFiles(String commonName, CertificateType certificateType,
				JcaPKCS10CertificationRequest certificateRequest, Path certificateRequestFile, KeyStore p12KeyStore,
				Path p12KeyStoreFile, KeyPair keyPair, Path keyPairPrivateKeyFile, Path keyPairPublicKeyFile,
				X509Certificate certificate, Path certificateFile, byte[] certificateSha512Thumbprint)
		{
			this.commonName = commonName;
			this.certificateType = certificateType;
			this.certificateRequest = certificateRequest;
			this.certificateRequestFile = certificateRequestFile;
			this.p12KeyStore = p12KeyStore;
			this.p12KeyStoreFile = p12KeyStoreFile;
			this.keyPair = keyPair;
			this.keyPairPrivateKeyFile = keyPairPrivateKeyFile;
			this.keyPairPublicKeyFile = keyPairPublicKeyFile;
			this.certificate = certificate;
			this.certificateFile = certificateFile;
			this.certificateSha512Thumbprint = certificateSha512Thumbprint;
		}

		public String getCommonName()
		{
			return commonName;
		}

		public CertificateType getCertificateType()
		{
			return certificateType;
		}

		public JcaPKCS10CertificationRequest getCertificateRequest()
		{
			return certificateRequest;
		}

		public Path getCertificateRequestFile()
		{
			return certificateRequestFile;
		}

		public KeyStore getP12KeyStore()
		{
			return p12KeyStore;
		}

		public Path getP12KeyStoreFile()
		{
			return p12KeyStoreFile;
		}

		public KeyPair getKeyPair()
		{
			return keyPair;
		}

		public Path getKeyPairPrivateKeyFile()
		{
			return keyPairPrivateKeyFile;
		}

		public Path getKeyPairPublicKeyFile()
		{
			return keyPairPublicKeyFile;
		}

		public X509Certificate getCertificate()
		{
			return certificate;
		}

		public Path getCertificateFile()
		{
			return certificateFile;
		}

		public byte[] getCertificateSha512Thumbprint()
		{
			return certificateSha512Thumbprint;
		}

		public String getCertificateSha512ThumbprintHex()
		{
			return Hex.encodeHexString(certificateSha512Thumbprint);
		}
	}

	private static final class CertificateAndP12File
	{
		final KeyStore p12KeyStore;
		final Path p12KeyStoreFile;
		final X509Certificate certificate;
		final Path certificateFile;

		CertificateAndP12File(KeyStore p12KeyStore, Path p12KeyStoreFile, X509Certificate certificate,
				Path certificateFile)
		{
			this.p12KeyStore = p12KeyStore;
			this.p12KeyStoreFile = p12KeyStoreFile;
			this.certificate = certificate;
			this.certificateFile = certificateFile;
		}
	}

	private CertificateAuthority ca;
	private Map<String, CertificateFiles> serverCertificateFilesByCommonName;
	private Map<String, CertificateFiles> clientCertificateFilesByCommonName;

	public void generateCertificates()
	{
		ca = initCA();

		serverCertificateFilesByCommonName = Arrays.stream(SERVER_COMMON_NAMES).map(createCert(CertificateType.SERVER))
				.collect(Collectors.toMap(CertificateFiles::getCommonName, Function.identity()));
		clientCertificateFilesByCommonName = Arrays.stream(CLIENT_COMMON_NAMES).map(createCert(CertificateType.CLIENT))
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
			RSAPrivateCrtKey caPrivateKey = readPrivatekey(caPrivateKeyFile);

			return CertificateAuthorityBuilder.create(caCertificate, caPrivateKey).initialize();
		}
		else
		{
			logger.info("Initializing CA with new cert file: {}, private key {}", caCertFile.toString(),
					caPrivateKeyFile.toString());

			CertificateAuthority ca = CertificateAuthorityBuilder.create("DE", null, null, null, null, "Test")
					.initialize();

			writeCertificate(caCertFile, ca.getCertificate());
			writePrivateKey(caPrivateKeyFile, (RSAPrivateCrtKey) ca.getCaKeyPair().getPrivate());

			return ca;
		}
	}

	private void writePrivateKey(Path privateKeyFile, RSAPrivateCrtKey privateKey)
	{
		try
		{
			PemIo.writePrivateKeyToPem(privateKey, privateKeyFile);
		}
		catch (IOException e)
		{
			logger.error("Error while writing private-key to " + privateKeyFile.toString(), e);
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

	private RSAPrivateCrtKey readPrivatekey(Path privateKeyFile)
	{
		try
		{
			return PemIo.readPrivateKeyFromPem(privateKeyFile);
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e)
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
				.map(c -> c.getCommonName() + "\n\t" + c.getCertificateSha512ThumbprintHex() + " (SHA-512)\n");

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

	public Function<String, CertificateFiles> createCert(CertificateType certificateType)
	{
		return commonName ->
		{
			Path privateKeyFile = createFolderIfNotExists(getPrivateKeyPath(commonName));
			Path publicKeyFile = createFolderIfNotExists(getPublicKeyPath(commonName));
			KeyPair keyPair = createOrReadKeyPair(privateKeyFile, publicKeyFile, commonName);

			Path certificateRequestFile = createFolderIfNotExists(getCertReqPath(commonName));
			JcaPKCS10CertificationRequest certificateRequest = createOrReadCertificateRequest(certificateRequestFile,
					keyPair, commonName, certificateType);

			Path certificateP12File = createFolderIfNotExists(getCertP12Path(commonName));
			Path certificatePemFile = createFolderIfNotExists(getCertPemPath(commonName));
			CertificateAndP12File certificateAndP12File = signOrReadCertificate(certificatePemFile, certificateP12File,
					certificateRequest, keyPair.getPrivate(), commonName, certificateType);

			return new CertificateFiles(commonName, certificateType, certificateRequest, certificateRequestFile,
					certificateAndP12File.p12KeyStore, certificateAndP12File.p12KeyStoreFile, keyPair, privateKeyFile,
					publicKeyFile, certificateAndP12File.certificate, certificateAndP12File.certificateFile,
					calculateSha512CertificateThumbprint(certificateAndP12File.certificate));
		};
	}

	private CertificateAndP12File signOrReadCertificate(Path certificateFile, Path p12KeyStoreFile,
			JcaPKCS10CertificationRequest certificateRequest, PrivateKey privateKey, String commonName,
			CertificateType certificateType)
	{
		if (Files.isReadable(certificateFile))
		{
			logger.info("Reading certificate (pem) from {} [{}]", certificateFile.toString(), commonName);
			X509Certificate certificate = readCertificate(certificateFile);

			KeyStore p12KeyStore;
			if (!Files.isReadable(p12KeyStoreFile))
			{
				logger.info("Saving certificate (p21) to {}, password '{}' [{}]", p12KeyStoreFile.toString(),
						CERT_PASSWORD, commonName);
				p12KeyStore = createP12KeyStore(privateKey, commonName, certificate);
				writeP12File(p12KeyStoreFile, p12KeyStore);
			}
			else
				p12KeyStore = readP12File(p12KeyStoreFile);

			return new CertificateAndP12File(p12KeyStore, p12KeyStoreFile, certificate, certificateFile);
		}
		else
		{
			logger.info("Signing {} certificate [{}]", certificateType.toString().toLowerCase(), commonName);
			X509Certificate certificate = signCertificateRequest(certificateRequest, certificateType);

			logger.info("Saving certificate (pem) to {} [{}]", certificateFile.toString(), commonName);
			writeCertificate(certificateFile, certificate);

			logger.info("Saving certificate (p21) to {}, password '{}' [{}]", p12KeyStoreFile.toString(), CERT_PASSWORD,
					commonName);
			KeyStore p12KeyStore = createP12KeyStore(privateKey, commonName, certificate);
			writeP12File(p12KeyStoreFile, p12KeyStore);

			return new CertificateAndP12File(p12KeyStore, p12KeyStoreFile, certificate, certificateFile);
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

	private KeyStore readP12File(Path p12File)
	{
		try
		{
			return CertificateReader.fromPkcs12(p12File, CERT_PASSWORD);
		}
		catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e)
		{
			logger.error("Error while reading certificate P12 file from " + p12File.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private JcaPKCS10CertificationRequest createOrReadCertificateRequest(Path certificateRequestFile, KeyPair keyPair,
			String commonName, CertificateType certificateType)
	{
		if (Files.isReadable(certificateRequestFile))
		{
			logger.info("Reading certificate request (csr) from {} [{}]", certificateRequestFile.toString(),
					commonName);
			return readCertificateRequest(certificateRequestFile);
		}
		else
		{
			X500Name subject = CertificationRequestBuilder.createSubject("DE", null, null, null, null, commonName);
			JcaPKCS10CertificationRequest certificateRequest = createCertificateRequest(keyPair, commonName,
					certificateType, subject);

			logger.info("Saving certificate request (csr) to {} [{}]", certificateRequestFile.toString(), commonName);
			writeCertificateRequest(certificateRequestFile, certificateRequest);

			return certificateRequest;
		}
	}

	private JcaPKCS10CertificationRequest createCertificateRequest(KeyPair keyPair, String commonName,
			CertificateType certificateType, X500Name subject)
	{
		try
		{
			switch (certificateType)
			{
				case CLIENT:
					return CertificationRequestBuilder.createClientCertificationRequest(subject, keyPair);
				case SERVER:
					return CertificationRequestBuilder.createServerCertificationRequest(subject, keyPair, null,
							commonName, SERVER_DNS_ALTERNATIVE_NAME);
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

	private KeyPair createOrReadKeyPair(Path privateKeyFile, Path publicKeyFile, String commonName)
	{
		if (Files.isReadable(privateKeyFile) && Files.isReadable(publicKeyFile))
		{
			logger.info("Reading private-key from {} [{}]", privateKeyFile.toString(), commonName);
			PrivateKey privateKey = readPrivatekey(privateKeyFile);

			logger.info("Reading public-key from {} [{}]", publicKeyFile.toString(), commonName);
			PublicKey publicKey = readPublicKey(publicKeyFile);

			return new KeyPair(publicKey, privateKey);
		}
		else
		{
			logger.info("Generating 4096 bit key pair [{}]", commonName);
			KeyPair keyPair = createKeyPair();

			logger.info("Saving private-key to {} [{}]", privateKeyFile.toString(), commonName);
			writePrivateKey(privateKeyFile, (RSAPrivateCrtKey) keyPair.getPrivate());

			logger.info("Saving public-key to {} [{}]", publicKeyFile.toString(), commonName);
			writePublicKey(publicKeyFile, keyPair);

			return keyPair;
		}
	}

	private void writePublicKey(Path publicKeyFile, KeyPair keyPair)
	{
		try
		{
			PemIo.writePublicKeyToPem((RSAPublicKey) keyPair.getPublic(), publicKeyFile);
		}
		catch (IOException e)
		{
			logger.error("Error while writing public-key to " + publicKeyFile.toString(), e);
			throw new RuntimeException(e);
		}
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

	private RSAPublicKey readPublicKey(Path publicKeyFile)
	{
		try
		{
			return PemIo.readPublicKeyFromPem(publicKeyFile);
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e)
		{
			logger.error("Error while reading public-key from " + publicKeyFile.toString(), e);
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

	private Path getPublicKeyPath(String commonName)
	{
		commonName = commonName.replaceAll("\\s+", "_");
		return Paths.get("cert", commonName, commonName + "_" + "public-key.pem");
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

		Path bpeP12File = Paths.get("../../dsf-bpe/dsf-bpe-server-jetty/target/localhost_certificate.p12");
		logger.info("Copying localhost certificate p12 file to {}", bpeP12File);
		writeP12File(bpeP12File, localhost.getP12KeyStore());

		Path fhirP12File = Paths.get("../../dsf-fhir/dsf-fhir-server-jetty/target/localhost_certificate.p12");
		logger.info("Copying localhost certificate p12 file to {}", fhirP12File);
		writeP12File(fhirP12File, localhost.getP12KeyStore());

		CertificateFiles testClient = clientCertificateFilesByCommonName.get("test-client");

		Path bpeClientP12File = Paths.get("../../dsf-bpe/dsf-bpe-server-jetty/target/test-client_certificate.p12");
		logger.info("Copying test-client certificate p12 file to {}", bpeClientP12File);
		writeP12File(bpeClientP12File, testClient.getP12KeyStore());

		Path fhirClientP12File = Paths.get("../../dsf-fhir/dsf-fhir-server-jetty/target/test-client_certificate.p12");
		logger.info("Copying test-client certificate p12 file to {}", fhirClientP12File);
		writeP12File(fhirClientP12File, testClient.getP12KeyStore());
	}

	public void copyDockerTestCertificates()
	{
		copyProxyFiles("dsf-docker-test-setup", "localhost");
		copyClientCertFiles("dsf-docker-test-setup", "test-client");
	}

	public void copyDockerTest3MedicTtpCertificates()
	{
		List<String> commonNames = Arrays.asList("medic1", "medic2", "medic3", "ttp");
		commonNames.forEach(cn -> copyProxyFiles("dsf-docker-test-setup-3medic-ttp/" + cn, cn));
		commonNames.forEach(cn -> copyClientCertFiles("dsf-docker-test-setup-3medic-ttp/" + cn, cn + "-client"));
	}

	private void copyProxyFiles(String dockerTestFolder, String commonName)
	{
		X509Certificate testCaCertificate = ca.getCertificate();
		CertificateFiles serverCertFiles = serverCertificateFilesByCommonName.get(commonName);

		Path baseFolder = Paths.get("../../", dockerTestFolder);

		Path bpeCertificateFile = baseFolder.resolve("bpe/proxy/ssl/certificate.pem");
		logger.info("Copying {} certificate pem file to {}", commonName, bpeCertificateFile);
		writeCertificate(bpeCertificateFile, serverCertFiles.getCertificate());

		Path fhirCertificateFile = baseFolder.resolve("fhir/proxy/ssl/certificate.pem");
		logger.info("Copying {} certificate pem file to {}", commonName, fhirCertificateFile);
		writeCertificate(fhirCertificateFile, serverCertFiles.getCertificate());

		Path bpePrivateKeyFile = baseFolder.resolve("bpe/proxy/ssl/private-key.pem");
		logger.info("Copying {} private-key file to {}", commonName, bpePrivateKeyFile);
		writePrivateKey(bpePrivateKeyFile, (RSAPrivateCrtKey) serverCertFiles.getKeyPair().getPrivate());

		Path fhirPrivateKeyFile = baseFolder.resolve("fhir/proxy/ssl/private-key.pem");
		logger.info("Copying {} private-key file to {}", commonName, fhirPrivateKeyFile);
		writePrivateKey(fhirPrivateKeyFile, (RSAPrivateCrtKey) serverCertFiles.getKeyPair().getPrivate());

		Path bpeCaCertFile = baseFolder.resolve("bpe/proxy/ssl/ca_certificate.pem");
		logger.info("Copying Test CA certificate file to {}", bpeCaCertFile.toString());
		writeCertificate(bpeCaCertFile, testCaCertificate);

		Path fhirCacertFile = baseFolder.resolve("fhir/proxy/ssl/ca_certificate.pem");
		logger.info("Copying Test CA certificate file to {}", fhirCacertFile.toString());
		writeCertificate(fhirCacertFile, testCaCertificate);
	}

	private void copyClientCertFiles(String dockerTestFolder, String commonName)
	{
		CertificateFiles clientCertFiles = clientCertificateFilesByCommonName.get(commonName);

		Path baseFolder = Paths.get("../../", dockerTestFolder);

		Path bpeClientP12File = baseFolder.resolve("bpe/app/conf/" + commonName + "_certificate.p12");
		logger.info("Copying {} certificate p12 file to {}", commonName, bpeClientP12File);
		writeP12File(bpeClientP12File, clientCertFiles.getP12KeyStore());

		Path fhirClientP12File = baseFolder.resolve("fhir/app/conf/" + commonName + "_certificate.p12");
		logger.info("Copying {} certificate p12 file to {}", commonName, fhirClientP12File);
		writeP12File(fhirClientP12File, clientCertFiles.getP12KeyStore());
	}
}
