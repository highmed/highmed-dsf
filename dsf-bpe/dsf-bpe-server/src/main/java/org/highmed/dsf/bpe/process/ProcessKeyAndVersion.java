package org.highmed.dsf.bpe.process;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;

public class ProcessKeyAndVersion implements Comparable<ProcessKeyAndVersion>
{
	public static ProcessKeyAndVersion fromString(String keyAndVersion)
	{
		Objects.requireNonNull(keyAndVersion, "keyAndVersion");

		String[] split = keyAndVersion.split("/");
		if (split.length != 2)
			throw new IllegalArgumentException("Format: 'key/version' expected");

		return new ProcessKeyAndVersion(split[0], split[1]);
	}

	public static List<ProcessKeyAndVersion> fromStrings(List<String> keysAndVersions)
	{
		Objects.requireNonNull(keysAndVersions, "keysAndVersions");

		return keysAndVersions.stream().filter(s -> s != null && !s.isBlank()).map(ProcessKeyAndVersion::fromString)
				.collect(Collectors.toList());
	}

	public static ProcessKeyAndVersion fromDefinition(ProcessDefinition definition)
	{
		Objects.requireNonNull(definition, "definition");

		return new ProcessKeyAndVersion(definition.getKey(), definition.getVersionTag());
	}

	public static ProcessKeyAndVersion fromModel(BpmnModelInstance model)
	{
		Objects.requireNonNull(model, "model");

		Process process = model.getModelElementsByType(Process.class).stream().findFirst().get();
		return new ProcessKeyAndVersion(process.getId(), process.getCamundaVersionTag());
	}

	private final String key;
	private final String version;

	public ProcessKeyAndVersion(String key, String version)
	{
		this.key = key;
		this.version = version;
	}

	public String getKey()
	{
		return key;
	}

	public String getVersion()
	{
		return version;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessKeyAndVersion other = (ProcessKeyAndVersion) obj;
		if (key == null)
		{
			if (other.key != null)
				return false;
		}
		else if (!key.equals(other.key))
			return false;
		if (version == null)
		{
			if (other.version != null)
				return false;
		}
		else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getKey() + "/" + getVersion();
	}

	@Override
	public int compareTo(ProcessKeyAndVersion o)
	{
		return Comparator.comparing(ProcessKeyAndVersion::getKey).thenComparing(ProcessKeyAndVersion::getVersion)
				.compare(this, o);
	}
}