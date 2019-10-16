package org.highmed.dsf.fhir.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SnapshotDependencies
{
	private final List<String> profiles = new ArrayList<>();
	private final List<String> targetProfiles = new ArrayList<>();

	@JsonCreator
	public SnapshotDependencies(@JsonProperty("profiles") List<String> profiles,
			@JsonProperty("targetProfiles") List<String> targetProfiles)
	{
		if (profiles != null)
			this.profiles.addAll(profiles);
		if (targetProfiles != null)
			this.targetProfiles.addAll(targetProfiles);
	}

	public List<String> getProfiles()
	{
		return Collections.unmodifiableList(profiles);
	}

	public List<String> getTargetProfiles()
	{
		return Collections.unmodifiableList(targetProfiles);
	}
}
