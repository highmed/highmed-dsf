package org.highmed.dsf.bpe.db;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BpeDbMigrationMain
{
	private static final Logger logger = LoggerFactory.getLogger(BpeDbMigrationMain.class);

	public static void main(String[] args) throws Exception
	{
		Path propertiesPath = Paths.get("db.properties");
		if (!Files.isReadable(propertiesPath))
		{
			logger.error("Properties file not readable: {}", propertiesPath.toAbsolutePath().toString());
			System.exit(1);
		}

		Properties dbProperties = read(propertiesPath, StandardCharsets.UTF_8);

		DbMigrator.migrate(dbProperties);
	}

	private static Properties read(Path propertiesFile, Charset encoding)
	{
		Properties properties = new Properties();

		try (Reader reader = new InputStreamReader(Files.newInputStream(propertiesFile), encoding))
		{
			properties.load(reader);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		return properties;
	}
}
