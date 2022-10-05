package org.highmed.dsf.bpe.service;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

public interface MailService
{
	default void send(String subject, String message)
	{
		try
		{
			MimeBodyPart body = new MimeBodyPart();
			body.setText(message, StandardCharsets.UTF_8.displayName());

			send(subject, body);
		}
		catch (MessagingException e)
		{
			throw new RuntimeException(e);
		}
	}

	default void send(String subject, MimeBodyPart body)
	{
		send(subject, body, m ->
		{});
	}

	void send(String subject, MimeBodyPart body, Consumer<MimeMessage> messageModifier);
}
