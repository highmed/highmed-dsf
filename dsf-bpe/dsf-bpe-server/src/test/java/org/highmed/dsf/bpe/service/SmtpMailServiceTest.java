package org.highmed.dsf.bpe.service;

import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import de.rwh.utils.crypto.io.CertificateReader;

@Ignore
public class SmtpMailServiceTest
{
	@Test
	public void testSend() throws Exception
	{
		new SmtpMailService("from@localhost", Arrays.asList("to@localhost"), "localhost", 1025).send("test subject",
				"test message");
	}

	@Test
	public void testSendTo() throws Exception
	{
		new SmtpMailService("from@localhost", Arrays.asList("to@localhost"), "localhost", 1025).send("test subject",
				"test message", "to-test@localhost");
	}

	@Test
	public void testSendReplyAndCc() throws Exception
	{
		new SmtpMailService("from@localhost", Arrays.asList("to1@localhost", "to2@localhost"),
				Arrays.asList("cc1@localhost", "cc2@localhost"),
				Arrays.asList("replyTo1@localhost", "replyTo2@localhost"), false, "localhost", 1025, null, null, null,
				null, null, null, null, false, 0, SmtpMailService.DEFAULT_DEBUG_LOG_LOCATION)
				.send("test subject", "test message");
	}

	@Test
	public void testSendSigned() throws Exception
	{
		char[] signStorePassword = "password".toCharArray();
		KeyStore signStore = CertificateReader.fromPkcs12(Paths.get("cert.p12"), signStorePassword);

		new SmtpMailService("from@localhost", Arrays.asList("to@localhost"), null, null, false, "localhost", 1025, null,
				null, null, null, null, signStore, signStorePassword, false, 0,
				SmtpMailService.DEFAULT_DEBUG_LOG_LOCATION).send("test subject", "test message");
	}

	@Test
	public void testSendViaSmtps() throws Exception
	{
		KeyStore trustStore = CertificateReader.allFromCer(Paths.get("cert.pem"));
		new SmtpMailService("from@localhost", Arrays.asList("to@localhost"), null, null, true, "localhost", 465, null,
				null, trustStore, null, null, null, null, false, 0, SmtpMailService.DEFAULT_DEBUG_LOG_LOCATION)
				.send("test subject", "test message");

	}

	@Test
	public void testSendViaGmail() throws Exception
	{
		new SmtpMailService("foo@gmail.com", Arrays.asList("foo@gmail.com"), null, null, true, "smtp.gmail.com", 465,
				"foo", "password".toCharArray(), null, null, null, null, null, false, 0,
				SmtpMailService.DEFAULT_DEBUG_LOG_LOCATION).send("test subject", "test message");

	}
}
