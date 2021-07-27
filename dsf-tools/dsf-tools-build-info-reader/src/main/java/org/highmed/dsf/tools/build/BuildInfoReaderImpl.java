package org.highmed.dsf.tools.build;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildInfoReaderImpl implements BuildInfoReader
{
	private static final Logger logger = LoggerFactory.getLogger(BuildInfoReaderImpl.class);

	private static final String VERSION_PROPERTIES_FILE = "/version.properties";

	private static final String PROPERTY_PROJECT_ARTIFACT = "project.artifact";
	private static final String PROPERTY_PROJECT_VERSION = "project.version";
	private static final String PROPERTY_BUILD_BRANCH = "build.branch";
	private static final String PROPERTY_BUILD_NUMBER = "build.number";
	private static final String PROPERTY_BUILD_DATE = "build.date";

	private Properties versionProperties;

	private Properties getVersionProperties()
	{
		if (versionProperties == null)
		{
			try (InputStream in = BuildInfoReaderImpl.class.getResourceAsStream(VERSION_PROPERTIES_FILE))
			{
				versionProperties = new Properties();
				versionProperties.load(in);
			}
			catch (IOException e)
			{
				logger.warn("Error while reading version properties", e);
				throw new RuntimeException(e);
			}
		}

		return versionProperties;
	}

	@Override
	public String getProjectArtifact()
	{
		String artifact = getVersionProperties().getProperty(PROPERTY_PROJECT_ARTIFACT);

		if ("${project.artifactId}".equals(artifact))
		{
			logger.warn("No project artifact provided via version properties");
			return "";
		}
		else
			return artifact;
	}

	@Override
	public String getProjectVersion()
	{
		String version = getVersionProperties().getProperty(PROPERTY_PROJECT_VERSION);

		if ("${project.version}".equals(version))
		{
			logger.warn("No project version provided via version properties");
			return "";
		}
		else
			return version;
	}

	@Override
	public String getBuildBranch()
	{
		String branch = getVersionProperties().getProperty(PROPERTY_BUILD_BRANCH);

		if ("${scmBranch}".equals(branch))
		{
			logger.warn("No build branch provided via version properties");
			return "";
		}
		else
			return branch;
	}

	@Override
	public String getBuildNumber()
	{
		String buildNumber = getVersionProperties().getProperty(PROPERTY_BUILD_NUMBER);

		if ("${buildNumber}".equals(buildNumber))
		{
			logger.warn("No build number provided via version properties");
			return "";
		}
		else
			return buildNumber;
	}

	@Override
	public ZonedDateTime getBuildDate()
	{
		String timestamp = getVersionProperties().getProperty(PROPERTY_BUILD_DATE);
		if ("${maven.build.timestamp}".equals(timestamp))
		{
			logger.warn("No build date provided via version properties, returning current date");
			return ZonedDateTime.now();
		}
		else
		{
			return ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		}
	}

	@Override
	public Date getBuildDateAsDate()
	{
		return Date.from(getBuildDate().toInstant());
	}

	@Override
	public void logSystemDefaultTimezone()
	{
		logger.info("System default timezone: {}",
				ZoneOffset.systemDefault().getDisplayName(TextStyle.NARROW, Locale.ENGLISH));
	}

	@Override
	public void logBuildInfo()
	{
		logger.info("Artifact: {}, version: {}, build: {}, branch: {}, commit: {}", getProjectArtifact(),
				getProjectVersion(), getBuildDate().withZoneSameInstant(ZoneId.systemDefault())
						.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
				getBuildBranch(), getBuildNumber());
	}
}
