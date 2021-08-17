package org.highmed.dsf.tools.docker.secrets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

public class DockerSecretsPropertySourceFactory
{
	private static final Logger logger = LoggerFactory.getLogger(DockerSecretsPropertySourceFactory.class);

	private final Map<String, String> secretFilesByFinalPropertyName = new HashMap<>();
	private final ConfigurableEnvironment environment;

	public DockerSecretsPropertySourceFactory(ConfigurableEnvironment environment, String... properties)
	{
		List.of(properties).forEach(key ->
		{
			String fileName = environment.getProperty(key + ".file", String.class, null);
			secretFilesByFinalPropertyName.put(key, fileName);
		});

		this.environment = environment;
	}

	public PropertiesPropertySource readDockerSecretsAndAddPropertiesToEnvironment()
	{
		MutablePropertySources sources = environment.getPropertySources();
		PropertiesPropertySource propertiesFromDockerSecrets = getPropertiesFromDockerSecrets();
		sources.addFirst(propertiesFromDockerSecrets);
		return propertiesFromDockerSecrets;
	}

	private PropertiesPropertySource getPropertiesFromDockerSecrets()
	{
		Properties properties = new Properties();

		secretFilesByFinalPropertyName.forEach((key, secretsFile) ->
		{
			String readSecretsFileValue = readSecretsFile(key, secretsFile);
			if (readSecretsFileValue != null)
				properties.put(key, readSecretsFileValue);
		});

		return new PropertiesPropertySource("docker-secrets", properties);
	}

	private String readSecretsFile(String key, String secretsFile)
	{
		if (secretsFile == null)
		{
			logger.debug("Secrets file for property {} not defined", key);
			return null;
		}

		Path secretsFilePath = Paths.get(secretsFile);

		if (!Files.isReadable(secretsFilePath))
		{
			logger.warn("Secrets file at {} not readable", secretsFilePath.toString());
			return null;
		}

		try
		{
			return new String(Files.readAllBytes(secretsFilePath), StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			logger.warn("Error while reading secrets file {}: {}", secretsFilePath.toString(), e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
