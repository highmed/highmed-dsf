package org.highmed.dsf.bpe.service;

import org.junit.Test;

public class LoggingMailServiceTest
{
	@Test
	public void testSend() throws Exception
	{
		new LoggingMailService().send("subject test", "message test");
	}
}
