package org.highmed.fhir.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SnapshotInfo
{
	private final SnapshotDependencies dependencies;

	@JsonCreator
	public SnapshotInfo(@JsonProperty("dependencies") SnapshotDependencies dependencies)
	{
		this.dependencies = dependencies;
	}

	public SnapshotDependencies getDependencies()
	{
		return dependencies;
	}
}
