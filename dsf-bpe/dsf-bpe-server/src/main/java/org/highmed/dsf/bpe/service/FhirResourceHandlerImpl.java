package org.highmed.dsf.bpe.service;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.highmed.fhir.client.BasicFhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.PreferReturnMinimal;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MetadataResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class FhirResourceHandlerImpl implements FhirResourceHandler, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(FhirResourceHandlerImpl.class);

	private static final String ACTIVITY_DEFINITION_URL_PATTERN_STRING = "(?<processUrl>http://(?<processDomain>(?:(?:[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*(?:[A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]))/bpe/Process/(?<processDefinitionKey>[-\\w]+))";
	private static final Pattern ACTIVITY_DEFINITION_URL_PATTERN = Pattern
			.compile(ACTIVITY_DEFINITION_URL_PATTERN_STRING);
	private static final String ACTIVITY_DEFINITION_VERSION_PATTERN_STRING = "(?<processVersion>\\d+\\.\\d+\\.\\d+)";
	private static final Pattern ACTIVITY_DEFINITION_VERSION_PATTERN = Pattern
			.compile(ACTIVITY_DEFINITION_VERSION_PATTERN_STRING);

	private final FhirWebserviceClient localWebserviceClient;
	private final ProcessPluginResourcesDao dao;
	private final FhirContext fhirContext;
	private final int fhirServerRequestMaxRetries;
	private final long fhirServerRetryDelayMillis;

	private final Map<String, ResourceProvider> resouceProvidersByDpendencyNameAndVersion = new HashMap<>();

	public FhirResourceHandlerImpl(FhirWebserviceClient localWebserviceClient, ProcessPluginResourcesDao dao,
			FhirContext fhirContext, int fhirServerRequestMaxRetries, long fhirServerRetryDelayMillis,
			Map<String, ResourceProvider> resouceProvidersByDpendencyNameAndVersion)
	{
		this.localWebserviceClient = localWebserviceClient;
		this.dao = dao;
		this.fhirContext = fhirContext;
		this.fhirServerRequestMaxRetries = fhirServerRequestMaxRetries;
		this.fhirServerRetryDelayMillis = fhirServerRetryDelayMillis;

		if (resouceProvidersByDpendencyNameAndVersion != null)
			this.resouceProvidersByDpendencyNameAndVersion.putAll(resouceProvidersByDpendencyNameAndVersion);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(localWebserviceClient, "localWebserviceClient");
		Objects.requireNonNull(dao, "dao");
		Objects.requireNonNull(fhirContext, "fhirContext");
		if (fhirServerRequestMaxRetries < -1)
			throw new IllegalArgumentException("fhirServerRequestMaxRetries < -1");
		if (fhirServerRetryDelayMillis < 0)
			throw new IllegalArgumentException("fhirServerRetryDelayMillis < 0");
	}

	private PreferReturnMinimal minimalReturnRetryClient()
	{
		if (fhirServerRequestMaxRetries == FhirWebserviceClient.RETRY_FOREVER)
			return localWebserviceClient.withMinimalReturn().withRetryForever(fhirServerRetryDelayMillis);
		else
			return localWebserviceClient.withMinimalReturn().withRetry(fhirServerRequestMaxRetries,
					fhirServerRetryDelayMillis);
	}

	private BasicFhirWebserviceClient retryClient()
	{
		if (fhirServerRequestMaxRetries == FhirWebserviceClient.RETRY_FOREVER)
			return localWebserviceClient.withRetryForever(fhirServerRetryDelayMillis);
		else
			return localWebserviceClient.withRetry(fhirServerRequestMaxRetries, fhirServerRetryDelayMillis);
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
			Stream<ProcessesResource> currentOrOldProccessResources = getCurrentOrOldResources(
					definitionByProcessKeyAndVersion, dbResourcesByProcess, change.getProcessKeyAndVersion());

			currentOrOldProccessResources.forEach(res ->
			{
				resources.computeIfPresent(res.getResourceInfo(), (processInfo, processResource) ->
				{
					processResource.addAll(res.getProcesses());

					if (change.getNewProcessState().isHigherPriority(processResource.getNewProcessState()))
						processResource.setNewProcessState(change.getNewProcessState());

					// only override resource state if not special case for previously unknown resource (no resource id)
					if (processResource.getResourceInfo().hasResourceId()
							&& change.getOldProcessState().isHigherPriority(processResource.getOldProcessState()))
						processResource.setOldProcessState(change.getOldProcessState());

					return processResource;
				});

				ProcessesResource nullIfNotNeededByOther = resources.putIfAbsent(res.getResourceInfo(),
						res.setNewProcessState(change.getNewProcessState())
								.setOldProcessState(change.getOldProcessState()));

				if (nullIfNotNeededByOther == null)
				{
					// special DRAFT case for previously unknown resource (no resource id)
					if (ProcessState.DRAFT.equals(change.getOldProcessState())
							&& ProcessState.DRAFT.equals(change.getNewProcessState())
							&& !res.getResourceInfo().hasResourceId())
					{
						logger.info("Adding new resource {}?{}", res.getResourceInfo().getResourceType(),
								res.getResourceInfo().toConditionalUrl());
						res.setOldProcessState(ProcessState.NEW);
					}
				}
			});
		}

		addResourcesRemovedFromDraftProcess(changes, dbResourcesByProcess, resources);

		findMissingResourcesAndModifyOldState(resources.values());

		List<ProcessesResource> resourceValues = new ArrayList<>(
				resources.values().stream().filter(ProcessesResource::hasStateChangeOrDraft)
						.filter(ProcessesResource::notNewToExcludedChange).collect(Collectors.toList()));

		Bundle batchBundle = new Bundle();
		batchBundle.setType(BundleType.BATCH);

		List<BundleEntryComponent> entries = resourceValues.stream()
				.map(r -> r.toBundleEntry(localWebserviceClient.getBaseUrl())).collect(Collectors.toList());
		batchBundle.setEntry(entries);

		try
		{
			if (batchBundle.getEntry().isEmpty())
				logger.debug("No transaction bundle to execute");
			else
			{
				logger.debug("Executing process plugin resources bundle");
				logger.trace("Bundle: {}", fhirContext.newJsonParser().encodeResourceToString(batchBundle));

				Bundle returnBundle = minimalReturnRetryClient().postBundle(batchBundle);

				List<UUID> deletedResourcesIds = addIdsAndReturnDeleted(resourceValues, returnBundle);
				List<ProcessKeyAndVersion> excludedProcesses = changes.stream()
						.filter(change -> ProcessState.EXCLUDED.equals(change.getNewProcessState()))
						.map(ProcessStateChangeOutcome::getProcessKeyAndVersion).collect(Collectors.toList());
				try
				{
					dao.addOrRemoveResources(resources.values(), deletedResourcesIds, excludedProcesses);
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

	private void addResourcesRemovedFromDraftProcess(List<ProcessStateChangeOutcome> changes,
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
								dbRes.toConditionalUrl(), dbRes.getResourceId());
				});
			}
		}
	}

	private void findMissingResourcesAndModifyOldState(Collection<ProcessesResource> resources)
	{
		List<ProcessesResource> resourceValues = resources.stream().filter(ProcessesResource::shouldExist)
				.collect(Collectors.toList());

		Bundle batchBundle = new Bundle();
		batchBundle.setType(BundleType.BATCH);

		batchBundle.setEntry(
				resourceValues.stream().map(ProcessesResource::toSearchBundleEntryCount0).collect(Collectors.toList()));

		if (batchBundle.getEntry().isEmpty())
			return;

		Bundle returnBundle = retryClient().postBundle(batchBundle);

		if (resourceValues.size() != returnBundle.getEntry().size())
			throw new RuntimeException("Return bundle size unexpeced, expected " + resourceValues.size() + " got "
					+ returnBundle.getEntry().size());

		for (int i = 0; i < resourceValues.size(); i++)
		{
			ProcessesResource resource = resourceValues.get(i);
			BundleEntryComponent entry = returnBundle.getEntry().get(i);

			if (!entry.getResponse().getStatus().startsWith("200"))
			{
				logger.warn("Response status for {} not 200 OK but {}, missing resource will not be added",
						resource.getSearchBundleEntryUrl(), entry.getResponse().getStatus());
			}
			else if (!entry.hasResource() || !(entry.getResource() instanceof Bundle)
					|| !(BundleType.SEARCHSET.equals(((Bundle) entry.getResource()).getType())))
			{
				logger.warn("Response for {} not a searchset Bundle, missing resource will not be added",
						resource.getSearchBundleEntryUrl());
			}

			Bundle searchBundle = (Bundle) entry.getResource();

			if (searchBundle.getTotal() <= 0)
			{
				resource.setOldProcessState(ProcessState.MISSING);

				logger.warn("Resource {} not found, setting old process state for resource to {}",
						resource.getSearchBundleEntryUrl(), ProcessState.MISSING);
			}
			else
				logger.info("Resource {} found", resource.getSearchBundleEntryUrl());
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
			List<String> expectedStatus = resource.getExpectedStatus();

			if (!expectedStatus.stream().anyMatch(eS -> entry.getResponse().getStatus().startsWith(eS)))
			{
				throw new RuntimeException("Return status " + entry.getResponse().getStatus() + " not starting with "
						+ (expectedStatus.size() > 1 ? "one of " : "") + expectedStatus + " for resource "
						+ resource.getResourceInfo().toString() + " of processes " + resource.getProcesses());

			}

			// create or update
			if (!ProcessState.EXCLUDED.equals(resource.getNewProcessState()))
			{
				IdType id = new IdType(entry.getResponse().getLocation());

				if (!resource.getResourceInfo().getResourceType().equals(id.getResourceType()))
					throw new RuntimeException("Return resource type unexpected, expected "
							+ resource.getResourceInfo().getResourceType() + " got " + id.getResourceType());

				resource.getResourceInfo().setResourceId(toUuid(id.getIdPart()));
			}

			// delete
			else
			{
				deletedIds.add(resource.getResourceInfo().getResourceId());

				resource.getResourceInfo().setResourceId(null);
			}
		}

		return deletedIds;
	}

	private Stream<ProcessesResource> getCurrentOrOldResources(
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
			List<ResourceInfo> resources = dbResourcesByProcess.get(process);
			if (resources == null)
			{
				logger.debug("No resources found in BPE DB for process {}", process);
				resources = Collections.emptyList();
			}

			return resources.stream().map(info -> ProcessesResource.from(info).add(process));
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

		List<MetadataResource> resources = definition.getResourceProvider()
				.getResources(process.toString(), providerByNameAndVersion).collect(Collectors.toList());
		if (resources.isEmpty())
		{
			logger.warn("No FHIR resources found in {} for process {}",
					definition.getJars().stream().map(Path::toString).collect(Collectors.joining(",")),
					process.toString());
			return Stream.empty();
		}
		else
		{
			if (!hasActivityDefinition(resources, process.getKey(), process.getVersion()))
			{
				logger.warn("None or more than one ActivityDefinition found in {} matching process {}",
						definition.getJars().stream().map(Path::toString).collect(Collectors.joining(",")),
						process.toString());
			}

			return resources.stream();
		}
	}

	private boolean hasActivityDefinition(List<MetadataResource> resources, String processKey, String processVersion)
	{
		return resources.stream().filter(r -> r instanceof ActivityDefinition).map(r -> (ActivityDefinition) r)
				.filter(matches(processKey, processVersion)).count() == 1;
	}

	private Predicate<? super ActivityDefinition> matches(String processKey, String processVersion)
	{
		return a ->
		{
			if (!a.hasUrl() || !a.hasVersion())
				return false;

			Matcher urlMatcher = ACTIVITY_DEFINITION_URL_PATTERN.matcher(a.getUrl());
			if (!urlMatcher.matches())
			{
				logger.warn("ActivityDefinition.url {} does not match {}", a.getUrl(),
						ACTIVITY_DEFINITION_URL_PATTERN_STRING);
				return false;
			}

			String aDprocessDomain = urlMatcher.group("processDomain").replace(".", "");
			String aDprocessDefinitionKey = urlMatcher.group("processDefinitionKey");
			String aDprocessKey = aDprocessDomain + "_" + aDprocessDefinitionKey;

			Matcher versionMatcher = ACTIVITY_DEFINITION_VERSION_PATTERN.matcher(a.getVersion());
			if (!versionMatcher.matches())
			{
				logger.warn("ActivityDefinition.version {} does not match {}", a.getVersion(),
						ACTIVITY_DEFINITION_VERSION_PATTERN_STRING);
				return false;
			}

			String aDprocessVersion = versionMatcher.group("processVersion");

			return aDprocessVersion.equals(processVersion) && aDprocessKey.equals(processKey);
		};
	}

	private Optional<UUID> getResourceId(Map<ProcessKeyAndVersion, List<ResourceInfo>> dbResourcesByProcess,
			ProcessKeyAndVersion process, ResourceInfo resourceInfo)
	{
		return dbResourcesByProcess.getOrDefault(process, Collections.emptyList()).stream()
				.filter(r -> r.equals(resourceInfo)).findFirst().map(ResourceInfo::getResourceId);
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
