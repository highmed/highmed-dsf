package org.highmed.dsf.bpe.process;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Questionnaire;
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
		else if (resource instanceof Questionnaire)
			return from((Questionnaire) resource);
		else if (resource instanceof StructureDefinition)
			return from((StructureDefinition) resource);
		else if (resource instanceof ValueSet)
			return from((ValueSet) resource);
		else
			throw new IllegalArgumentException(
					"MetadataResource of type " + resource.getClass().getName() + " not supported");
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

	public static ProcessesResource from(Questionnaire resource)
	{
		return new ProcessesResource(
				new ResourceInfo(resource.getResourceType().name(), resource.getUrl(), resource.getVersion(), null),
				resource);
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

	public boolean shouldExist()
	{
		return (ProcessState.ACTIVE.equals(getOldProcessState()) && ProcessState.ACTIVE.equals(getNewProcessState()))
				|| (ProcessState.RETIRED.equals(getOldProcessState())
						&& ProcessState.RETIRED.equals(getNewProcessState()));
	}

	public BundleEntryComponent toBundleEntry(String baseUrl)
	{
		switch (getOldProcessState())
		{
			case MISSING:
				return fromMissing();
			case NEW:
				return fromNew();
			case ACTIVE:
				return fromActive(baseUrl);
			case DRAFT:
				return fromDraft(baseUrl);
			case RETIRED:
				return fromRetired(baseUrl);
			case EXCLUDED:
				return fromExcluded();
			default:
				throw new RuntimeException(
						ProcessState.class.getSimpleName() + " " + getOldProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromMissing()
	{
		switch (getNewProcessState())
		{
			case ACTIVE:
				return createAsActive();
			case RETIRED:
				return createAsRetired();
			default:
				throw new RuntimeException(
						"State change " + getOldProcessState() + " -> " + getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromNew()
	{
		switch (getNewProcessState())
		{
			case ACTIVE:
				return createAsActive();
			case DRAFT:
				return createAsDraft();
			case RETIRED:
				return createAsRetired();
			default:
				throw new RuntimeException(
						"State change " + getOldProcessState() + " -> " + getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromActive(String baseUrl)
	{
		switch (getNewProcessState())
		{
			case DRAFT:
				return updateToDraft(baseUrl);
			case RETIRED:
				return updateToRetired(baseUrl);
			case EXCLUDED:
				return delete();
			default:
				throw new RuntimeException(
						"State change " + getOldProcessState() + " -> " + getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromDraft(String baseUrl)
	{
		switch (getNewProcessState())
		{
			case ACTIVE:
				return updateToActive(baseUrl);
			case DRAFT:
				return updateToDraft(baseUrl);
			case RETIRED:
				return updateToRetired(baseUrl);
			case EXCLUDED:
				return delete();
			default:
				throw new RuntimeException(
						"State change " + getOldProcessState() + " -> " + getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromRetired(String baseUrl)
	{
		switch (getNewProcessState())
		{
			case ACTIVE:
				return updateToActive(baseUrl);
			case DRAFT:
				return updateToDraft(baseUrl);
			case EXCLUDED:
				return delete();
			default:
				throw new RuntimeException(
						"State change " + getOldProcessState() + " -> " + getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromExcluded()
	{
		switch (getNewProcessState())
		{
			case ACTIVE:
				return createAsActive();
			case DRAFT:
				return createAsDraft();
			case RETIRED:
				return createAsRetired();
			default:
				throw new RuntimeException(
						"State change " + getOldProcessState() + " -> " + getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent createAsActive()
	{
		getResource().setStatus(PublicationStatus.ACTIVE);
		return create();
	}

	private BundleEntryComponent createAsDraft()
	{
		getResource().setStatus(PublicationStatus.DRAFT);
		return create();
	}

	private BundleEntryComponent createAsRetired()
	{
		getResource().setStatus(PublicationStatus.RETIRED);
		return create();
	}

	private BundleEntryComponent create()
	{
		BundleEntryComponent entry = new BundleEntryComponent();
		entry.setResource(getResource());
		entry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());

		BundleEntryRequestComponent request = entry.getRequest();
		request.setMethod(HTTPVerb.POST);
		request.setUrl(getResourceInfo().getResourceType());
		request.setIfNoneExist(getResourceInfo().toConditionalUrl());

		return entry;
	}

	private BundleEntryComponent updateToActive(String baseUrl)
	{
		getResource().setStatus(PublicationStatus.ACTIVE);
		return update(baseUrl);
	}

	private BundleEntryComponent updateToDraft(String baseUrl)
	{
		getResource().setStatus(PublicationStatus.DRAFT);
		return update(baseUrl);
	}

	private BundleEntryComponent updateToRetired(String baseUrl)
	{
		getResource().setStatus(PublicationStatus.RETIRED);
		return update(baseUrl);
	}

	private BundleEntryComponent update(String baseUrl)
	{
		BundleEntryComponent entry = new BundleEntryComponent();
		entry.setResource(getResource());
		entry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());

		BundleEntryRequestComponent request = entry.getRequest();
		request.setMethod(HTTPVerb.PUT);
		request.setUrl(getResourceInfo().getResourceType() + "?" + getResourceInfo().toConditionalUrl());

		return entry;
	}

	private BundleEntryComponent delete()
	{
		BundleEntryComponent entry = new BundleEntryComponent();

		BundleEntryRequestComponent request = entry.getRequest();
		request.setMethod(HTTPVerb.DELETE);
		request.setUrl(getResourceInfo().getResourceType() + "?" + getResourceInfo().toConditionalUrl());

		return entry;
	}

	public List<String> getExpectedStatus()
	{
		switch (getOldProcessState())
		{
			case MISSING:
				switch (getNewProcessState())
				{
					case ACTIVE:
						// conditional create NamingSystem: name=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					case RETIRED:
						// conditional create NamingSystem: name=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					default:
						throw new RuntimeException("State change " + getOldProcessState() + " -> "
								+ getNewProcessState() + " not supported");
				}
			case NEW:
				switch (getNewProcessState())
				{
					case ACTIVE:
						// conditional create NamingSystem: name=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					case DRAFT:
						// conditional create NamingSystem: name=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					case RETIRED:
						// conditional create NamingSystem: name=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					default:
						throw new RuntimeException("State change " + getOldProcessState() + " -> "
								+ getNewProcessState() + " not supported");
				}
			case ACTIVE:
				switch (getNewProcessState())
				{
					case DRAFT:
						// standard update with resource id
						return Collections.singletonList("200");
					case RETIRED:
						// standard update with resource id
						return Collections.singletonList("200");
					case EXCLUDED:
						// standard delete with resource id
						return Arrays.asList("200", "204");
					default:
						throw new RuntimeException("State change " + getOldProcessState() + " -> "
								+ getNewProcessState() + " not supported");
				}
			case DRAFT:
				switch (getNewProcessState())
				{
					case ACTIVE:
						// standard update with resource id
						return Collections.singletonList("200");
					case DRAFT:
						// standard update with resource id
						return Collections.singletonList("200");
					case RETIRED:
						// standard update with resource id
						return Collections.singletonList("200");
					case EXCLUDED:
						// standard delete with resource id
						return Arrays.asList("200", "204");
					default:
						throw new RuntimeException("State change " + getOldProcessState() + " -> "
								+ getNewProcessState() + " not supported");
				}
			case RETIRED:
				switch (getNewProcessState())
				{
					case ACTIVE:
						// standard update with resource id
						return Collections.singletonList("200");
					case DRAFT:
						// standard update with resource id
						return Collections.singletonList("200");
					case EXCLUDED:
						// standard delete with resource id
						return Arrays.asList("200", "204");
					default:
						throw new RuntimeException("State change " + getOldProcessState() + " -> "
								+ getNewProcessState() + " not supported");
				}
			case EXCLUDED:
				switch (getNewProcessState())
				{
					case ACTIVE:
						// conditional create NamingSystem: name=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					case DRAFT:
						// conditional create NamingSystem: name=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					case RETIRED:
						// conditional create NamingSystem: name=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					default:
						throw new RuntimeException("State change " + getOldProcessState() + " -> "
								+ getNewProcessState() + " not supported");
				}
			default:
				throw new RuntimeException(
						ProcessState.class.getSimpleName() + " " + getOldProcessState() + " not supported");
		}
	}

	public BundleEntryComponent toSearchBundleEntryCount0()
	{
		BundleEntryComponent entry = new BundleEntryComponent();

		BundleEntryRequestComponent request = entry.getRequest();
		request.setMethod(HTTPVerb.GET);
		request.setUrl(getSearchBundleEntryUrl() + "&_count=0");

		return entry;
	}

	public String getSearchBundleEntryUrl()
	{
		return getResourceInfo().getResourceType() + "?" + getResourceInfo().toConditionalUrl();
	}
}
