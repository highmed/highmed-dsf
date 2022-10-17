package org.highmed.dsf.bpe.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.SSLSocketFactory;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.apache.logging.log4j.core.net.MailManager;
import org.apache.logging.log4j.core.net.MailManager.FactoryData;
import org.apache.logging.log4j.core.net.MailManagerFactory;
import org.apache.logging.log4j.core.net.SmtpManager;
import org.apache.logging.log4j.util.Strings;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.rwh.utils.crypto.context.SSLContextFactory;

public class SmtpMailService implements MailService, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SmtpMailService.class);

	private static final class Layout implements StringLayout
	{
		final HtmlLayout delegate = HtmlLayout.newBuilder().setDatePattern("yyyy-MM-dd HH:mm:ss.nnnn").build();

		final String debugLogLocation;

		Layout(String debugLogLocation)
		{
			this.debugLogLocation = debugLogLocation;
		}

		@Override
		public byte[] getFooter()
		{
			StringBuilder sbuf = new StringBuilder();
			sbuf.append("</table>").append(Strings.LINE_SEPARATOR);
			sbuf.append("<br>").append(Strings.LINE_SEPARATOR);
			sbuf.append("For more details see debug log at <i>").append(Strings.LINE_SEPARATOR);
			sbuf.append(debugLogLocation).append(Strings.LINE_SEPARATOR);
			sbuf.append("</i>").append(Strings.LINE_SEPARATOR);
			sbuf.append("<br>").append(Strings.LINE_SEPARATOR);
			sbuf.append("</body></html>").append(Strings.LINE_SEPARATOR);

			return sbuf.toString().getBytes(getCharset());
		}

		@Override
		public byte[] getHeader()
		{
			return delegate.getHeader();
		}

		@Override
		public byte[] toByteArray(LogEvent event)
		{
			return delegate.toByteArray(event);
		}

		@Override
		public String getContentType()
		{
			return delegate.getContentType();
		}

		@Override
		public Map<String, String> getContentFormat()
		{
			return delegate.getContentFormat();
		}

		@Override
		public void encode(LogEvent source, ByteBufferDestination destination)
		{
			delegate.encode(source, destination);
		}

		@Override
		public Charset getCharset()
		{
			return delegate.getCharset();
		}

		@Override
		public String toSerializable(LogEvent event)
		{
			return delegate.toSerializable(event);
		}
	}

	private static final class Log4jAppender extends AbstractAppender
	{
		private final MailManager manager;

		private Log4jAppender(Session session, MimeMessage message, String subject, int messageBufferSize,
				String debugLogLocation)
		{
			super("SmtpMailService.Log4jAppender", ThresholdFilter.createFilter(null, null, null),
					new Layout(debugLogLocation), false, null);

			MailManagerFactory factory = (name, data) ->
			{
				return new SmtpManager(name, session, message, data)
				{
				};
			};
			FactoryData data = new FactoryData(null, null, null, null, null, null, event -> subject, null, null, 0,
					null, null, false, messageBufferSize, null, null);
			manager = AbstractManager.getManager("SmtpMailService.Log4jAppender.Manager", factory, data);
		}

		@Override
		public boolean isFiltered(LogEvent event)
		{
			boolean filtered = super.isFiltered(event);

			if (filtered)
				manager.add(event);

			return filtered;
		}

		@Override
		public void append(LogEvent event)
		{
			manager.sendEvents(getLayout(), event);
		}
	}

	public static final String DEFAULT_DEBUG_LOG_LOCATION = "/opt/bpe/log/bpe.log";

	private final InternetAddress fromAddress;
	private final InternetAddress[] toAddresses;
	private final InternetAddress[] toAddressesCc;
	private final InternetAddress[] replyToAddresses;

	private final Session session;
	private final SMIMESignedGenerator smimeSignedGenerator;

	private final Log4jAppender log4jAppender;

	/**
	 * SMTP, non authentication, mails not signed, no mails on error log events, value of
	 * {@link #DEFAULT_DEBUG_LOG_LOCATION} as debug log location
	 *
	 * @param fromAddress
	 *            not <code>null</code>
	 * @param toAddresses
	 *            not <code>null</code>, at least one
	 * @param mailServerHostname
	 *            not <code>null</code>
	 * @param mailServerPort
	 *            not <code>null</code>
	 */
	public SmtpMailService(String fromAddress, List<String> toAddresses, String mailServerHostname, int mailServerPort)
	{
		this(fromAddress, toAddresses, null, null, false, mailServerHostname, mailServerPort, null, null, null, null,
				null, null, null, false, 0, DEFAULT_DEBUG_LOG_LOCATION);
	}

	/**
	 * @param fromAddress
	 *            not <code>null</code>
	 * @param toAddresses
	 *            not <code>null</code>, at least one
	 * @param toAddressesCc
	 *            may be <code>null</code>
	 * @param replyToAddresses
	 *            may be <code>null</code>
	 * @param useSmtps
	 * @param mailServerHostname
	 *            not <code>null</code>
	 * @param mailServerPort
	 *            <code>&gt; 0</code>
	 * @param mailServerUsername
	 *            may be <code>null</code>
	 * @param mailServerPassword
	 *            may be <code>null</code>
	 * @param trustStore
	 *            may be <code>null</code>
	 * @param keyStore
	 *            may be <code>null</code>
	 * @param keyStorePassword
	 * @param signStore
	 *            may be <code>null</code>
	 * @param signStorePassword
	 * @param mailOnErrorLogEvent
	 *            <code>true</code> if mail should be send for error log events
	 * @param mailOnErrorLogEventBufferSize
	 *            <code>&gt;= 0</code>
	 * @param debugLogLocation
	 *            not <code>null</code>
	 */
	public SmtpMailService(String fromAddress, List<String> toAddresses, List<String> toAddressesCc,
			List<String> replyToAddresses, boolean useSmtps, String mailServerHostname, int mailServerPort,
			String mailServerUsername, char[] mailServerPassword, KeyStore trustStore, KeyStore keyStore,
			char[] keyStorePassword, KeyStore signStore, char[] signStorePassword, boolean mailOnErrorLogEvent,
			int mailOnErrorLogEventBufferSize, String debugLogLocation)
	{
		this.fromAddress = toInternetAddress(fromAddress).orElse(null);
		this.toAddresses = toAddresses == null ? new InternetAddress[0]
				: toAddresses.stream().flatMap(s -> toInternetAddress(s).stream()).toArray(InternetAddress[]::new);
		this.toAddressesCc = toAddressesCc == null ? new InternetAddress[0]
				: toAddressesCc.stream().flatMap(s -> toInternetAddress(s).stream()).toArray(InternetAddress[]::new);
		this.replyToAddresses = replyToAddresses == null ? new InternetAddress[0]
				: replyToAddresses.stream().flatMap(s -> toInternetAddress(s).stream()).toArray(InternetAddress[]::new);

		session = createSession(useSmtps, mailServerHostname, mailServerPort, mailServerUsername, mailServerPassword,
				trustStore, keyStore, keyStorePassword);

		smimeSignedGenerator = createSmimeSignedGenerator(fromAddress, signStore, signStorePassword);

		log4jAppender = !mailOnErrorLogEvent ? null
				: new Log4jAppender(session, createMimeMessage("DSF BPE Error", null), "DSF BPE Error",
						mailOnErrorLogEventBufferSize, debugLogLocation);
	}

	private Optional<InternetAddress> toInternetAddress(String fromAddress)
	{
		if (fromAddress == null || fromAddress.isBlank())
			return Optional.empty();

		try
		{
			return Optional.of(new InternetAddress(fromAddress));
		}
		catch (AddressException e)
		{
			logger.warn("Unable to create {} from {}: {} - {}", InternetAddress.class.getName(), fromAddress,
					e.getClass().getName(), e.getMessage());

			return Optional.empty();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (fromAddress == null)
			throw new IllegalArgumentException("no valid from address configured");
		if (toAddresses.length == 0)
			throw new IllegalArgumentException("no valid to addresses configured");
	}

	private Session createSession(boolean useSmtps, String mailServerHostname, int mailServerPort,
			String mailServerUsername, char[] mailServerPassword, KeyStore trustStore, KeyStore keyStore,
			char[] keyStorePassword)
	{
		Properties properties = new Properties();

		Authenticator authenticator = null;
		if (mailServerUsername != null && mailServerPassword != null)
		{
			authenticator = new Authenticator()
			{
				@Override
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(mailServerUsername, String.copyValueOf(mailServerPassword));
				}
			};

			properties.put("mail.smtp.auth", "true");

			if (!useSmtps)
				logger.warn(
						"Username/Password configured, SMTPS not enabled. Password will be send without encryption! Consider activating/using SMTP over TLS (aka SMTPS)");
		}

		if (useSmtps)
		{
			properties.put("mail.smtp.ssl.enable", "true");
			properties.put("mail.transport.protocol", "smtps");
			properties.put("mail.smtp.socketFactory.fallback", "false");
			properties.put("mail.smtp.ssl.checkserveridentity", "true");
			properties.put("mail.smtp.ssl.socketFactory",
					createSslSocketFactory(trustStore, keyStore, keyStorePassword));
		}

		properties.put("mail.smtp.host", mailServerHostname);
		properties.put("mail.smtp.port", mailServerPort);

		return Session.getInstance(properties, authenticator);
	}

	public SSLSocketFactory createSslSocketFactory(KeyStore trustStore, KeyStore keyStore, char[] keyStorePassword)
	{
		try
		{
			// uses default jvm trust / keys if not configured (respective trustStore/keyStore fields null)
			return new SSLContextFactory().createSSLContext(trustStore, keyStore, keyStorePassword).getSocketFactory();
		}
		catch (UnrecoverableKeyException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException e)
		{
			logger.warn("Unable to create custom ssl socket factory: {} - {}", e.getClass().getName(), e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private SMIMESignedGenerator createSmimeSignedGenerator(String fromAddress, KeyStore signStore,
			char[] signStorePassword)
	{
		if (signStore == null)
			return null;

		try
		{
			Optional<Certificate[]> certificates = getFirstCertificateChain(signStore)
					.filter(hasCertificateForAddress(fromAddress));
			if (certificates.isEmpty())
			{
				logger.warn("Mail signing certificate store has no S/MIME certificate for {}, not signing mails",
						fromAddress);
				return null;
			}

			Optional<PrivateKey> pivateKey = getFirstPrivateKey(signStore, signStorePassword);
			if (pivateKey.isEmpty())
			{
				logger.warn("Mail signing certificate store has no private key, not signing mails", fromAddress);
				return null;
			}

			Certificate certificate = certificates
					.flatMap(c -> Stream.of(c).filter(hasSubjectAlternativeNameRfc822Name(fromAddress)).findFirst())
					.get();

			ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
			SMIMECapabilityVector caps = new SMIMECapabilityVector();
			caps.addCapability(SMIMECapability.aES128_CBC);
			caps.addCapability(SMIMECapability.aES256_CBC);
			signedAttrs.add(new SMIMECapabilitiesAttribute(caps));

			SMIMESignedGenerator generator = new SMIMESignedGenerator();
			generator.addSignerInfoGenerator(
					new JcaSimpleSignerInfoGeneratorBuilder().setProvider(new BouncyCastleProvider())
							.setSignedAttributeGenerator(new AttributeTable(signedAttrs)).build("SHA256withRSA",
									pivateKey.get(), new X509CertificateHolder(certificate.getEncoded())));
			generator.addCertificates(new JcaCertStore(certificates.map(Arrays::asList).get()));

			return generator;
		}
		catch (KeyStoreException | CertificateException | OperatorCreationException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private Optional<Certificate[]> getFirstCertificateChain(KeyStore store) throws KeyStoreException
	{
		return Collections.list(store.aliases()).stream().map(getCertificateChain(store)).findFirst();
	}

	private Function<String, Certificate[]> getCertificateChain(KeyStore store)
	{
		return alias ->
		{
			try
			{
				return store.getCertificateChain(alias);
			}
			catch (KeyStoreException e)
			{
				throw new RuntimeException(e);
			}
		};
	}

	private Predicate<Certificate[]> hasCertificateForAddress(String address)
	{
		return chain -> hasSubjectAlternativeNameRfc822Name(address).test(chain[0]);
	}

	private Predicate<Certificate> hasSubjectAlternativeNameRfc822Name(String address)
	{
		return certificate ->
		{
			try
			{
				X509CertificateHolder holder = new X509CertificateHolder(certificate.getEncoded());
				X509Certificate x509Certificate = new JcaX509CertificateConverter().getCertificate(holder);
				Collection<List<?>> sanCollections = x509Certificate.getSubjectAlternativeNames();
				if (sanCollections == null)
					return false;
				else
					// first entry SAN type (1 = Rfc822Name), second entry SAN value
					return sanCollections.stream()
							.anyMatch(l -> Objects.equals(1, l.get(0)) && Objects.equals(address, l.get(1)));
			}
			catch (CertificateException | IOException e)
			{
				throw new RuntimeException(e);
			}
		};
	}

	private Optional<PrivateKey> getFirstPrivateKey(KeyStore store, char[] keyPassword) throws KeyStoreException
	{
		return Collections.list(store.aliases()).stream().map(getPrivateKey(store, keyPassword)).findFirst()
				.filter(k -> k instanceof PrivateKey).map(k -> (PrivateKey) k);
	}

	private Function<String, Key> getPrivateKey(KeyStore store, char[] keyPassword)
	{
		return alias ->
		{
			try
			{
				return store.getKey(alias, keyPassword);
			}
			catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e)
			{
				throw new RuntimeException(e);
			}
		};
	}

	private MimeMessage createMimeMessage(String subject, MimeMultipart mimeMultipart)
	{
		try
		{
			MimeMessage mimeMessage = new MimeMessage(session);
			mimeMessage.setFrom(fromAddress);
			mimeMessage.setRecipients(RecipientType.TO, toAddresses);
			mimeMessage.setRecipients(RecipientType.CC, toAddressesCc);
			mimeMessage.setReplyTo(replyToAddresses);
			mimeMessage.setSubject(subject);
			if (mimeMultipart != null)
				mimeMessage.setContent(mimeMultipart);
			mimeMessage.saveChanges();

			return mimeMessage;
		}
		catch (MessagingException e)
		{
			throw new RuntimeException(e);
		}
	}

	private MimeMultipart signMessage(MimeBodyPart body)
	{
		if (smimeSignedGenerator != null)
		{
			try
			{
				return smimeSignedGenerator.generate(body);
			}
			catch (SMIMEException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			try
			{
				if (body.getContent() != null && body.getContent() instanceof MimeMultipart)
					return (MimeMultipart) body.getContent();
				else
					return new MimeMultipart(body);
			}
			catch (MessagingException | IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void send(String subject, MimeBodyPart body, Consumer<MimeMessage> messageModifier)
	{
		MimeMessage message = createMimeMessage(subject, signMessage(body));

		if (messageModifier != null)
			messageModifier.accept(message);

		try
		{
			Transport.send(message);
		}
		catch (MessagingException e)
		{
			logger.warn("Unable to send message: {} - {}", e.getClass().getName(), e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public Log4jAppender getLog4jAppender()
	{
		return log4jAppender;
	}
}
