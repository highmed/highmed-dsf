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
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

public class DockerSecretsPropertySourceFactory
{
	private static final Logger logger = LoggerFactory.getLogger(DockerSecretsPropertySourceFactory.class);

	private final Map<String, String> secretFilesByFinalPropertyName = new HashMap<>();
	private final ConfigurableEnvironment environment;

	public DockerSecretsPropertySourceFactory(ConfigurableEnvironment environment)
	{
		Stream<String> passwordProperties = environment.getPropertySources().stream()
				.filter(s -> s instanceof EnumerablePropertySource).map(s -> (EnumerablePropertySource<?>) s)
				.flatMap(s -> List.of(s.getPropertyNames()).stream()).filter(key -> key != null)
				.filter(key -> key.toLowerCase().endsWith(".password.file")
						|| key.toLowerCase().endsWith("_password_file"));

		passwordProperties.forEach(key ->
		{
			String fileName = environment.getProperty(key, String.class, null);
			secretFilesByFinalPropertyName.put(key.toLowerCase().replace('_', '.').substring(0, key.length() - 5),
					fileName);
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
			List<String> secretLines = Files.readAllLines(secretsFilePath, StandardCharsets.UTF_8);

			if (secretLines.isEmpty())
			{
				logger.warn("Secrets file for property {} is empty", key);
				return null;
			}

			if (secretLines.size() > 1)
				logger.warn("Secrets file for property {} contains multiple lines, using only the first line", key);

			return secretLines.get(0);
		}
		catch (IOException e)
		{
			logger.warn("Error while reading secrets file {}: {}", secretsFilePath.toString(), e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
