package org.highmed.dsf.bpe.process;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

import com.google.common.base.Objects;

public final class ProcessesResource
{
	public static ProcessesResource from(MetadataResource resource)
	{
		if (resource instanceof ActivityDefinition)
			return from((ActivityDefinition) resource);
		else if (resource instanceof CodeSystem)
			return from((CodeSystem) resource);
		else if (resource instanceof NamingSystem)
			return from((NamingSystem) resource);
		else if (resource instanceof StructureDefinition)
			return from((StructureDefinition) resource);
		else if (resource instanceof ValueSet)
			return from((ValueSet) resource);
		else
			throw new IllegalArgumentException(
					"MetadataResource of type" + resource.getClass().getName() + " not supported");
	}

	public static ProcessesResource from(ActivityDefinition resource)
	{
		return new ProcessesResource(
				new ResourceInfo(resource.getResourceType().name(), resource.getUrl(), resource.getVersion(), null),
				resource);
	}

	public static ProcessesResource from(CodeSystem resource)
	{
		return new ProcessesResource(
				new ResourceInfo(resource.getResourceType().name(), resource.getUrl(), resource.getVersion(), null),
				resource);
	}

	public static ProcessesResource from(NamingSystem resource)
	{
		return new ProcessesResource(
				new ResourceInfo(resource.getResourceType().name(), null, null, resource.getName()), resource);
	}

	public static ProcessesResource from(StructureDefinition resource)
	{
		return new ProcessesResource(
				new ResourceInfo(resource.getResourceType().name(), resource.getUrl(), resource.getVersion(), null),
				resource);
	}

	public static ProcessesResource from(ValueSet resource)
	{
		return new ProcessesResource(
				new ResourceInfo(resource.getResourceType().name(), resource.getUrl(), resource.getVersion(), null),
				resource);
	}

	public static ProcessesResource from(ResourceInfo resourceInfo)
	{
		return new ProcessesResource(resourceInfo, null);
	}

	private final ResourceInfo resourceInfo;
	private final MetadataResource resource;
	private final Set<ProcessKeyAndVersion> processes = new HashSet<>();

	private ProcessState oldState;
	private ProcessState newState;

	private ProcessesResource(ResourceInfo resourceInfo, MetadataResource resource)
	{
		this.resourceInfo = resourceInfo;
		this.resource = resource;
	}

	public ResourceInfo getResourceInfo()
	{
		return resourceInfo;
	}

	public MetadataResource getResource()
	{
		return resource;
	}

	public Set<ProcessKeyAndVersion> getProcesses()
	{
		return Collections.unmodifiableSet(processes);
	}

	public ProcessesResource add(ProcessKeyAndVersion process)
	{
		processes.add(process);

		return this;
	}

	public void addAll(Set<ProcessKeyAndVersion> processes)
	{
		this.processes.addAll(processes);
	}

	public ProcessesResource setOldProcessState(ProcessState oldState)
	{
		this.oldState = oldState;

		return this;
	}

	public ProcessState getOldProcessState()
	{
		return oldState;
	}

	public ProcessesResource setNewProcessState(ProcessState newState)
	{
		this.newState = newState;

		return this;
	}

	public ProcessState getNewProcessState()
	{
		return newState;
	}

	public boolean hasStateChangeOrDraft()
	{
		return !Objects.equal(getOldProcessState(), getNewProcessState())
				|| (ProcessState.DRAFT.equals(getOldProcessState()) && ProcessState.DRAFT.equals(getNewProcessState()));
	}

	public boolean notNewToExcludedChange()
	{
		return !(ProcessState.NEW.equals(getOldProcessState()) && ProcessState.EXCLUDED.equals(getNewProcessState()));
	}
}