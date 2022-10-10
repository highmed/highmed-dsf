package org.highmed.dsf.bpe.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingMailService implements MailService
{
	private static final Logger logger = LoggerFactory.getLogger(LoggingMailService.class);
	private static final Logger mailLogger = LoggerFactory.getLogger("mail-logger");

	private MimeMessage createMimeMessage(String subject, MimeBodyPart body)
	{
		try
		{
			MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
			mimeMessage.setSubject(subject);
			mimeMessage.setContent(new MimeMultipart(body));
			mimeMessage.saveChanges();

			return mimeMessage;
		}
		catch (MessagingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void send(String subject, MimeBodyPart body, Consumer<MimeMessage> messageModifier)
	{
		logger.info("SMTP mail service not configured, see debug log for mail subject / content");
		try
		{
			if (logger.isDebugEnabled() || mailLogger.isInfoEnabled())
			{
				MimeMessage message = createMimeMessage(subject, body);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				message.writeTo(out);

				logger.debug("Subject: {}, Content: {}", subject, out.toString());
				mailLogger.info("Subject: {}, Content: {}", subject, out.toString());
			}
		}
		catch (IOException | MessagingException e)
		{
			throw new RuntimeException(e);
		}
	}
}
