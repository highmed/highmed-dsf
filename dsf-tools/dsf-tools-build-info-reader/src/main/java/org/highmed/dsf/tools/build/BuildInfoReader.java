package org.highmed.dsf.tools.build;

import java.time.ZonedDateTime;
import java.util.Date;

public interface BuildInfoReader
{
	String getProjectArtifact();

	String getProjectVersion();

	String getBuildBranch();

	String getBuildNumber();

	ZonedDateTime getBuildDate();

	Date getBuildDateAsDate();

	void logSystemDefaultTimezone();

	void logBuildInfo();
}