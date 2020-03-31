package org.highmed.dsf.tools.proxy;

import java.io.Console;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyTest
{
	private static final Logger logger = LoggerFactory.getLogger(ProxyTest.class);

	public static void main(String[] args)
	{
		logger.info(Arrays.toString(args));

		Console cons;
		char[] passwd;
		if ((cons = System.console()) != null && (passwd = cons.readPassword("[%s]", "Password:")) != null)
		{
			TestClient client = new TestClient(args[0], args.length > 1 ? args[1] : null,
					args.length > 2 ? args[2] : null, passwd);
			client.testBaseUrl();

			java.util.Arrays.fill(passwd, ' ');
		}
		else
			logger.info("null");
	}
}
