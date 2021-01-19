package org.highmed.dsf.bpe.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.dao.ProcessPluginResourcesDao;
import org.highmed.dsf.bpe.plugin.ProcessPluginDefinitionAndClassLoader;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.highmed.dsf.bpe.process.ProcessState;
import org.highmed.dsf.bpe.process.ProcessStateChangeOutcome;
import org.highmed.dsf.bpe.process.ProcessesResource;
import org.highmed.dsf.bpe.process.ResourceInfo;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MetadataResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class FhirResourceHandlerImpl implements FhirResourceHandler, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(FhirResourceHandlerImpl.class);

	private final FhirWebserviceClient localWebserviceClient;
	private final ProcessPluginResourcesDao dao;
	private final FhirContext fhirContext;

	private final Map<String, ResourceProvider> resouceProvidersByDpendencyNameAndVersion = new HashMap<>();

	public FhirResourceHandlerImpl(FhirWebserviceClient localWebserviceClient, ProcessPluginResourcesDao dao,
			FhirContext fhirContext, Map<String, ResourceProvider> resouceProvidersByDpendencyNameAndVersion)
	{
		this.localWebserviceClient = localWebserviceClient;
		this.dao = dao;
		this.fhirContext = fhirContext;

		if (resouceProvidersByDpendencyNameAndVersion != null)
			this.resouceProvidersByDpendencyNameAndVersion.putAll(resouceProvidersByDpendencyNameAndVersion);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(localWebserviceClient, "localWebserviceClient");
		Objects.requireNonNull(dao, "dao");
		Objects.requireNonNull(fhirContext, "fhirContext");
	}

	@Override
	public void applyStateChangesAndStoreNewResourcesInDb(
			Map<ProcessKeyAndVersion, ProcessPluginDefinitionAndClassLoader> definitionByProcessKeyAndVersion,
			List<ProcessStateChangeOutcome> changes)
	{
		Objects.requireNonNull(definitionByProcessKeyAndVersion, "definitionByProcessKeyAndVersion");
		Objects.requireNonNull(changes, "changes");

		Map<ProcessKeyAndVersion, List<ResourceInfo>> dbResourcesByProcess = getResourceInfosFromDb();

		Map<ResourceInfo, ProcessesResource> resources = new HashMap<>();
		for (ProcessStateChangeOutcome change : changes)
		{
			Stream<ProcessesResource> proccessResources = getResources(definitionByProcessKeyAndVersion,
					dbResourcesByProcess, change.getProcessKeyAndVersion());

			proccessResources.forEach(res ->
			{
				resources.computeIfPresent(res.getResourceInfo(), (k, v) ->
				{
					v.addAll(res.getProcesses());

					if (change.getNewProcessState().isHigherPriority(v.getNewProcessState()))
						v.setNewProcessState(change.getNewProcessState());

					// only override resource state if not special case for previously unknown resource (no resource id)
					if (v.getResourceInfo().hasResourceId()
							&& change.getOldProcessState().isHigherPriority(v.getOldProcessState()))
						v.setOldProcessState(change.getOldProcessState());

					return v;
				});

				ProcessesResource nullIfNotNeededByOther = resources.putIfAbsent(res.getResourceInfo(),
						res.setNewProcessState(change.getNewProcessState())
								.setOldProcessState(change.getOldProcessState()));

				if (nullIfNotNeededByOther == null)
				{
					// special case for previously unknown resource (no resource id)
					if (ProcessState.DRAFT.equals(change.getOldProcessState())
							&& ProcessState.DRAFT.equals(change.getNewProcessState())
							&& !res.getResourceInfo().hasResourceId())
					{
						logger.info("Adding new resource {}?{}", res.getResourceInfo().getResourceType(),
								res.getResourceInfo().toConditionalCreateUrl());
						res.setOldProcessState(ProcessState.NEW);
					}
				}
			});
		}

		addRemovedResources(changes, dbResourcesByProcess, resources);

		List<ProcessesResource> resourceValues = new ArrayList<>(
				resources.values().stream().filter(ProcessesResource::hasStateChangeOrDraft)
						.filter(ProcessesResource::notNewToExcludedChange).collect(Collectors.toList()));

		Bundle batchBundle = new Bundle();
		batchBundle.setType(BundleType.BATCH);

		List<BundleEntryComponent> entries = toEntries(resourceValues);
		batchBundle.setEntry(entries);

		try
		{
			if (batchBundle.getEntry().isEmpty())
				logger.debug("No transaction bundle to execute");
			else
			{
				logger.debug("Executing process plugin resources bundle");
				logger.trace("Bundle: {}", fhirContext.newJsonParser().encodeResourceToString(batchBundle));

				// TODO retries
				Bundle returnBundle = localWebserviceClient.withMinimalReturn().withRetry(1).postBundle(batchBundle);

				List<UUID> deletedResourcesIds = addIdsAndReturnDeleted(resourceValues, returnBundle);

				try
				{
					dao.addOrRemoveResources(resources.values(), deletedResourcesIds);
				}
				catch (SQLException e)
				{
					logger.error("Error while adding process plugin resource to the db", e);
					throw new RuntimeException(e);
				}
			}
		}
		catch (Exception e)
		{
			logger.warn("Error while executing process plugins resource bundle: {}", e.getMessage());
			logger.warn(
					"Resources in FHIR server may not be consitent, please check resources and execute the following bundle if necessary: {}",
					fhirContext.newJsonParser().encodeResourceToString(batchBundle));
			throw e;
		}
	}

	private void addRemovedResources(List<ProcessStateChangeOutcome> changes,
			Map<ProcessKeyAndVersion, List<ResourceInfo>> dbResourcesByProcess,
			Map<ResourceInfo, ProcessesResource> resources)
	{
		for (ProcessStateChangeOutcome change : changes)
		{
			if (ProcessState.DRAFT.equals(change.getOldProcessState())
					&& ProcessState.DRAFT.equals(change.getNewProcessState()))
			{
				List<ResourceInfo> dbResources = dbResourcesByProcess.getOrDefault(change.getProcessKeyAndVersion(),
						Collections.emptyList());

				dbResources.forEach(dbRes ->
				{
					ProcessesResource processRes = ProcessesResource.from(dbRes);
					processRes.setOldProcessState(ProcessState.DRAFT);
					processRes.setNewProcessState(ProcessState.EXCLUDED);

					ProcessesResource nullIfNotNeededByOther = resources.putIfAbsent(dbRes, processRes);

					if (nullIfNotNeededByOther == null)
						logger.info("Deleting resource {}?{} with id {} if exists", dbRes.getResourceType(),
								dbRes.toConditionalCreateUrl(), dbRes.getResourceId());
				});
			}
		}
	}

	private List<UUID> addIdsAndReturnDeleted(List<ProcessesResource> resourceValues, Bundle returnBundle)
	{
		if (resourceValues.size() != returnBundle.getEntry().size())
			throw new RuntimeException("Return bundle size unexpeced, expected " + resourceValues.size() + " got "
					+ returnBundle.getEntry().size());

		List<UUID> deletedIds = new ArrayList<>();
		for (int i = 0; i < resourceValues.size(); i++)
		{
			ProcessesResource resource = resourceValues.get(i);
			BundleEntryComponent entry = returnBundle.getEntry().get(i);

			if (resource.getResourceInfo().getResourceId() == null
					&& !entry.getResponse().getStatus().startsWith("201"))
			{
				throw new RuntimeException("Return status " + entry.getResponse().getStatus()
						+ " not starting with '201' for new resource " + resource.getResourceInfo().toString()
						+ " of processes " + resource.getProcesses());
			}
			else if (resource.getResourceInfo().getResourceId() != null
					&& entry.getResponse().getStatus().startsWith("201"))
			{
				throw new RuntimeException("Return status starting with '201' unexpected for existing resource "
						+ resource.getResourceInfo().toString() + " of processes " + resource.getProcesses());
			}

			if (!ProcessState.EXCLUDED.equals(resource.getNewProcessState()))
			{
				IdType id = new IdType(entry.getResponse().getLocation());

				if (!resource.getResourceInfo().getResourceType().equals(id.getResourceType()))
					throw new RuntimeException("Return resource type unexpected, expected "
							+ resource.getResourceInfo().getResourceType() + " got " + id.getResourceType());

				resource.getResourceInfo().setResourceId(toUuid(id.getIdPart()));
			}
			else
			{
				deletedIds.add(resource.getResourceInfo().getResourceId());

				resource.getResourceInfo().setResourceId(null);
			}
		}

		return deletedIds;
	}

	private Stream<ProcessesResource> getResources(
			Map<ProcessKeyAndVersion, ProcessPluginDefinitionAndClassLoader> definitionByProcess,
			Map<ProcessKeyAndVersion, List<ResourceInfo>> dbResourcesByProcess, ProcessKeyAndVersion process)
	{
		ProcessPluginDefinitionAndClassLoader definition = definitionByProcess.get(process);

		if (definition != null)
		{
			Stream<MetadataResource> resources = getResources(process, definition);
			return resources.map(fhirResource ->
			{
				ProcessesResource resource = ProcessesResource.from(fhirResource).add(process);

				Optional<UUID> resourceId = getResourceId(dbResourcesByProcess, process, resource.getResourceInfo());
				resourceId.ifPresent(id -> resource.getResourceInfo().setResourceId(id));
				// not present: new resource, unknown to bpe db

				return resource;
			});
		}
		else
		{
			return dbResourcesByProcess.get(process).stream().map(info -> ProcessesResource.from(info).add(process));
		}
	}

	private Stream<MetadataResource> getResources(ProcessKeyAndVersion process,
			ProcessPluginDefinitionAndClassLoader definition)
	{
		Function<String, ResourceProvider> providerByNameAndVersion = nameAndVersion ->
		{
			if (resouceProvidersByDpendencyNameAndVersion.containsKey(nameAndVersion))
			{
				logger.trace("Resource provider for dependency {} found", nameAndVersion);
				return resouceProvidersByDpendencyNameAndVersion.get(nameAndVersion);
			}
			else
			{
				logger.warn("Resource provider for dependency {} not found", nameAndVersion);
				return ResourceProvider.empty();
			}
		};

		return definition.getResourceProvider().getResources(process.toString(), providerByNameAndVersion);
	}

	private Optional<UUID> getResourceId(Map<ProcessKeyAndVersion, List<ResourceInfo>> dbResourcesByProcess,
			ProcessKeyAndVersion process, ResourceInfo resourceInfo)
	{
		return dbResourcesByProcess.getOrDefault(process, Collections.emptyList()).stream()
				.filter(r -> r.equals(resourceInfo)).findFirst().map(ResourceInfo::getResourceId);
	}

	private List<BundleEntryComponent> toEntries(List<ProcessesResource> resources)
	{
		return resources.stream().map(this::toEntry).collect(Collectors.toList());
	}

	private BundleEntryComponent toEntry(ProcessesResource resource)
	{
		switch (resource.getOldProcessState())
		{
			case NEW:
				return fromNew(resource);
			case ACTIVE:
				return fromActive(resource);
			case DRAFT:
				return fromDraft(resource);
			case RETIRED:
				return fromRetired(resource);
			case EXCLUDED:
				return fromExcluded(resource);
			default:
				throw new RuntimeException(
						ProcessState.class.getSimpleName() + " " + resource.getOldProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromNew(ProcessesResource resource)
	{
		switch (resource.getNewProcessState())
		{
			case ACTIVE:
				return createAsActive(resource);
			case DRAFT:
				return createAsDraft(resource);
			case RETIRED:
				return createAsRetired(resource);
			default:
				throw new RuntimeException("State change " + resource.getOldProcessState() + " -> "
						+ resource.getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromActive(ProcessesResource resource)
	{
		switch (resource.getNewProcessState())
		{
			case DRAFT:
				return updateToDraft(resource);
			case RETIRED:
				return updateToRetired(resource);
			case EXCLUDED:
				return delete(resource);
			default:
				throw new RuntimeException("State change " + resource.getOldProcessState() + " -> "
						+ resource.getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromDraft(ProcessesResource resource)
	{
		switch (resource.getNewProcessState())
		{
			case ACTIVE:
				return updateToActive(resource);
			case DRAFT:
				return updateToDraft(resource);
			case RETIRED:
				return updateToRetired(resource);
			case EXCLUDED:
				return delete(resource);
			default:
				throw new RuntimeException("State change " + resource.getOldProcessState() + " -> "
						+ resource.getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromRetired(ProcessesResource resource)
	{
		switch (resource.getNewProcessState())
		{
			case ACTIVE:
				return updateToActive(resource);
			case DRAFT:
				return updateToDraft(resource);
			case EXCLUDED:
				return delete(resource);
			default:
				throw new RuntimeException("State change " + resource.getOldProcessState() + " -> "
						+ resource.getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromExcluded(ProcessesResource resource)
	{
		switch (resource.getNewProcessState())
		{
			case ACTIVE:
				return createAsActive(resource);
			case DRAFT:
				return createAsDraft(resource);
			case RETIRED:
				return createAsRetired(resource);
			default:
				throw new RuntimeException("State change " + resource.getOldProcessState() + " -> "
						+ resource.getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent createAsActive(ProcessesResource resource)
	{
		resource.getResource().setStatus(PublicationStatus.ACTIVE);
		return create(resource);
	}

	private BundleEntryComponent createAsDraft(ProcessesResource resource)
	{
		resource.getResource().setStatus(PublicationStatus.DRAFT);
		return create(resource);
	}

	private BundleEntryComponent createAsRetired(ProcessesResource resource)
	{
		resource.getResource().setStatus(PublicationStatus.RETIRED);
		return create(resource);
	}

	private BundleEntryComponent create(ProcessesResource resource)
	{
		BundleEntryComponent entry = new BundleEntryComponent();
		entry.setResource(resource.getResource());
		entry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());

		BundleEntryRequestComponent request = entry.getRequest();
		request.setMethod(HTTPVerb.POST);
		request.setUrl(resource.getResourceInfo().getResourceType());
		request.setIfNoneExist(resource.getResourceInfo().toConditionalCreateUrl());
		return entry;
	}

	private BundleEntryComponent updateToActive(ProcessesResource resource)
	{
		resource.getResource().setStatus(PublicationStatus.ACTIVE);
		return update(resource);
	}

	private BundleEntryComponent updateToDraft(ProcessesResource resource)
	{
		resource.getResource().setStatus(PublicationStatus.DRAFT);
		return update(resource);
	}

	private BundleEntryComponent updateToRetired(ProcessesResource resource)
	{
		resource.getResource().setStatus(PublicationStatus.RETIRED);
		return update(resource);
	}

	private BundleEntryComponent update(ProcessesResource resource)
	{
		IdType id = new IdType(localWebserviceClient.getBaseUrl(), resource.getResourceInfo().getResourceType(),
				resource.getResourceInfo().getResourceId().toString(), null);

		resource.getResource().setIdElement(id.toUnqualifiedVersionless());

		BundleEntryComponent entry = new BundleEntryComponent();
		entry.setResource(resource.getResource());
		entry.setFullUrl(id.toString());

		BundleEntryRequestComponent request = entry.getRequest();
		request.setMethod(HTTPVerb.PUT);
		request.setUrl(resource.getResourceInfo().getResourceType() + "/"
				+ resource.getResourceInfo().getResourceId().toString());
		return entry;
	}

	private BundleEntryComponent delete(ProcessesResource resource)
	{
		IdType id = new IdType(localWebserviceClient.getBaseUrl(), resource.getResourceInfo().getResourceType(),
				resource.getResourceInfo().getResourceId().toString(), null);

		BundleEntryComponent entry = new BundleEntryComponent();
		entry.setFullUrl(id.toString());

		BundleEntryRequestComponent request = entry.getRequest();
		request.setMethod(HTTPVerb.DELETE);
		request.setUrl(resource.getResourceInfo().getResourceType() + "?_id="
				+ resource.getResourceInfo().getResourceId().toString());

		return entry;
	}

	private Map<ProcessKeyAndVersion, List<ResourceInfo>> getResourceInfosFromDb()
	{
		try
		{
			return dao.getResources();
		}
		catch (SQLException e)
		{
			logger.warn("Error while retrieving resource infos from db", e);
			throw new RuntimeException(e);
		}
	}

	private UUID toUuid(String id)
	{
		if (id == null)
			return null;

		// TODO control flow by exception
		try
		{
			return UUID.fromString(id);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}
}
