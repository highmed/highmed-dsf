package org.highmed.dsf.fhir.service;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.event.Event;
import org.highmed.dsf.fhir.event.EventHandler;
import org.highmed.dsf.fhir.event.ResourceCreatedEvent;
import org.highmed.dsf.fhir.event.ResourceDeletedEvent;
import org.highmed.dsf.fhir.event.ResourceUpdatedEvent;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.context.support.ValueSetExpansionOptions;

public class ValidationSupportWithCache implements IValidationSupport, EventHandler
{
	private static final Logger logger = LoggerFactory.getLogger(ValidationSupportWithCache.class);

	private final class CacheEntry<R extends Resource>
	{
		private final Supplier<R> resourceSupplier;
		private SoftReference<R> ref;

		public CacheEntry(R resource, Supplier<R> resourceSupplier)
		{
			this.ref = new SoftReference<>(resource);
			this.resourceSupplier = resourceSupplier;
		}

		private SoftReference<R> read()
		{
			return new SoftReference<>(resourceSupplier.get());
		}

		public R get()
		{
			if (ref == null || ref.get() == null)
			{
				ref = read();
			}

			return ref.get();
		}
	}

	private static final Pattern UUID_PATTERN = Pattern
			.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");

	private final FhirContext context;
	private final IValidationSupport delegate;

	private final AtomicBoolean fetchAllStructureDefinitionsDone = new AtomicBoolean();
	private final AtomicBoolean fetchAllConformanceResourcesDone = new AtomicBoolean();

	private final ConcurrentMap<String, CacheEntry<StructureDefinition>> structureDefinitions = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, CacheEntry<CodeSystem>> codeSystems = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, CacheEntry<ValueSet>> valueSets = new ConcurrentHashMap<>();

	private final ConcurrentMap<String, String> urlAndVersionsById = new ConcurrentHashMap<>();

	public ValidationSupportWithCache(FhirContext context, IValidationSupport delegate)
	{
		this.context = context;
		this.delegate = delegate;
	}

	public ValidationSupportWithCache populateCache(List<IBaseResource> cacheValues)
	{
		logger.trace("populating cache");

		cacheValues.stream().filter(r -> r instanceof Resource).map(r -> (Resource) r).forEach(this::add);

		fetchAllConformanceResourcesDone.set(true);
		fetchAllStructureDefinitionsDone.set(true);

		return this;
	}

	@Override
	public FhirContext getFhirContext()
	{
		return context;
	}

	@Override
	public void handleEvent(Event event)
	{
		if (event == null)
			return;

		logger.trace("handling event {}", event.getClass().getSimpleName());

		if (event instanceof ResourceCreatedEvent && resourceSupported(event.getResource()))
			add(event.getResource());
		else if (event instanceof ResourceDeletedEvent && resourceSupported(event.getResourceType(), event.getId()))
			remove(event.getResourceType(), event.getId());
		else if (event instanceof ResourceUpdatedEvent && resourceSupported(event.getResource()))
			update(event.getResource());
	}

	private boolean resourceSupported(Resource resource)
	{
		return resource != null && (resource instanceof CodeSystem || resource instanceof StructureDefinition
				|| resource instanceof ValueSet);
	}

	private boolean resourceSupported(Class<? extends Resource> type, String resourceId)
	{
		return urlAndVersionsById.containsKey(resourceId) && (CodeSystem.class.equals(type)
				|| StructureDefinition.class.equals(type) || ValueSet.class.equals(type));
	}

	private void add(Resource resource)
	{
		if (resource instanceof CodeSystem)
			doAdd((CodeSystem) resource, codeSystems, CodeSystem::getUrl, CodeSystem::getVersion,
					url -> (CodeSystem) delegate.fetchCodeSystem(url));
		else if (resource instanceof StructureDefinition)
			doAdd((StructureDefinition) resource, structureDefinitions, StructureDefinition::getUrl,
					StructureDefinition::getVersion,
					url -> (StructureDefinition) delegate.fetchStructureDefinition(url));
		else if (resource instanceof ValueSet)
			doAdd((ValueSet) resource, valueSets, ValueSet::getUrl, ValueSet::getVersion,
					url -> (ValueSet) delegate.fetchValueSet(url));
	}

	private <R extends Resource> void doAdd(R resource, ConcurrentMap<String, CacheEntry<R>> cache,
			Function<R, String> toUrl, Function<R, String> toVersion, Function<String, R> fetch)
	{
		String url = toUrl.apply(resource);
		String version = toVersion.apply(resource);

		cache.put(url, new CacheEntry<>(resource, () -> fetch.apply(url)));

		if (version != null)
		{
			String urlAndVersion = url + "|" + version;
			cache.put(urlAndVersion, new CacheEntry<>(resource, () -> fetch.apply(urlAndVersion)));
		}

		if (resource.hasIdElement() && resource.getIdElement().hasIdPart()
				&& UUID_PATTERN.matcher(resource.getIdElement().getIdPart()).matches())
			urlAndVersionsById.put(resource.getIdElement().getIdPart(), url + "|" + version);
	}

	private void update(Resource resource)
	{
		remove(resource);
		add(resource);
	}

	private void remove(Resource resource)
	{
		if (resource instanceof CodeSystem)
			doRemove((CodeSystem) resource, codeSystems, CodeSystem::getUrl, CodeSystem::getVersion);
		else if (resource instanceof StructureDefinition)
			doRemove((StructureDefinition) resource, structureDefinitions, StructureDefinition::getUrl,
					StructureDefinition::getVersion);
		else if (resource instanceof ValueSet)
			doRemove((ValueSet) resource, valueSets, ValueSet::getUrl, ValueSet::getVersion);
	}

	private <R extends Resource> void doRemove(R resource, ConcurrentMap<String, CacheEntry<R>> cache,
			Function<R, String> toUrl, Function<R, String> toVersion)
	{
		String url = toUrl.apply(resource);
		String version = toVersion.apply(resource);

		cache.remove(url);
		cache.remove(url + "|" + version);
	}

	private void remove(Class<? extends Resource> type, String id)
	{
		if (CodeSystem.class.equals(type))
			doRemove(id, codeSystems);
		else if (StructureDefinition.class.equals(type))
			doRemove(id, structureDefinitions);
		else if (ValueSet.class.equals(type))
			doRemove(id, valueSets);
	}

	private <R extends Resource> void doRemove(String id, ConcurrentMap<String, CacheEntry<R>> cache)
	{
		String urlAndVersion = urlAndVersionsById.get(id);

		if (urlAndVersion != null)
		{
			String[] split = urlAndVersion.split("\\|");
			String url = split.length > 0 ? split[0] : "";
			String version = split.length > 1 ? split[1] : "";

			cache.remove(url);
			cache.remove(url + "|" + version);
		}
	}

	public List<IBaseResource> fetchAllConformanceResources()
	{
		if (!fetchAllConformanceResourcesDone.get())
		{
			logger.trace("Fetching all conformance resources");

			List<IBaseResource> allConformanceResources = delegate.fetchAllConformanceResources();

			allConformanceResources.stream().filter(r -> r instanceof Resource).map(r -> (Resource) r)
					.forEach(this::update);

			fetchAllConformanceResourcesDone.set(true);
			fetchAllStructureDefinitionsDone.set(true);

			return allConformanceResources;
		}
		else
		{
			logger.trace("Fetching all conformance resources from cache");

			return Stream
					.concat(codeSystems.values().stream(),
							Stream.concat(structureDefinitions.values().stream(), valueSets.values().stream()))
					.map(c -> (IBaseResource) c.get()).collect(Collectors.toList());
		}
	}

	@Override
	public <T extends IBaseResource> List<T> fetchAllStructureDefinitions()
	{
		if (!fetchAllStructureDefinitionsDone.get())
		{
			logger.trace("Fetching all structure-definitions");

			List<T> allStructureDefinitions = delegate.fetchAllStructureDefinitions();

			allStructureDefinitions.stream().filter(r -> r instanceof Resource).map(r -> (Resource) r)
					.forEach(this::update);

			fetchAllStructureDefinitionsDone.set(true);

			return allStructureDefinitions;
		}
		else
		{
			logger.trace("Fetching all structure-definitions from cache");

			@SuppressWarnings("unchecked")
			List<T> all = (List<T>) structureDefinitions.values().stream().map(c -> (IBaseResource) c.get())
					.collect(Collectors.toList());
			return all;
		}
	}

	@Override
	public IBaseResource fetchStructureDefinition(String url)
	{
		logger.trace("Fetiching structure-definition '{}'", url);

		if (url == null || url.isBlank())
			return null;

		return fetch(structureDefinitions, url, () -> (StructureDefinition) delegate.fetchStructureDefinition(url));
	}

	@Override
	public boolean isCodeSystemSupported(ValidationSupportContext theRootValidationSupport, String url)
	{
		return fetchCodeSystem(url) != null;
	}

	@Override
	public IBaseResource fetchCodeSystem(String url)
	{
		logger.trace("Fetiching code-system '{}'", url);

		if (url == null || url.isBlank())
			return null;

		return fetch(codeSystems, url, () -> (CodeSystem) delegate.fetchCodeSystem(url));
	}

	@Override
	public boolean isValueSetSupported(ValidationSupportContext theRootValidationSupport, String url)
	{
		return fetchValueSet(url) != null;
	}

	@Override
	public IBaseResource fetchValueSet(String url)
	{
		logger.trace("Fetiching value-set '{}'", url);

		if (url == null || url.isBlank())
			return null;

		return fetch(valueSets, url, () -> (ValueSet) delegate.fetchValueSet(url));
	}

	private <R extends Resource> R fetch(ConcurrentMap<String, CacheEntry<R>> cache, String url, Supplier<R> fetch)
	{
		CacheEntry<R> cacheEntry = cache.get(url);
		if (cacheEntry != null)
			return cacheEntry.get();

		R resource = fetch.get();
		if (resource == null)
			return null;

		cache.put(url, new CacheEntry<>(resource, fetch));
		return resource;
	}

	@Override
	public ValueSetExpansionOutcome expandValueSet(ValidationSupportContext theRootValidationSupport,
			ValueSetExpansionOptions theExpansionOptions, IBaseResource theValueSetToExpand)
	{
		return delegate.expandValueSet(theRootValidationSupport, theExpansionOptions, theValueSetToExpand);
	}

	@Override
	public <T extends IBaseResource> T fetchResource(Class<T> theClass, String theUri)
	{
		return delegate.fetchResource(theClass, theUri);
	}

	@Override
	public CodeValidationResult validateCode(ValidationSupportContext theRootValidationSupport,
			ConceptValidationOptions theOptions, String theCodeSystem, String theCode, String theDisplay,
			String theValueSetUrl)
	{
		return delegate.validateCode(theRootValidationSupport, theOptions, theCodeSystem, theCode, theDisplay,
				theValueSetUrl);
	}

	@Override
	public CodeValidationResult validateCodeInValueSet(ValidationSupportContext theRootValidationSupport,
			ConceptValidationOptions theOptions, String theCodeSystem, String theCode, String theDisplay,
			IBaseResource theValueSet)
	{
		return delegate.validateCodeInValueSet(theRootValidationSupport, theOptions, theCodeSystem, theCode, theDisplay,
				theValueSet);
	}

	@Override
	public LookupCodeResult lookupCode(ValidationSupportContext theRootValidationSupport, String theSystem,
			String theCode)
	{
		return delegate.lookupCode(theRootValidationSupport, theSystem, theCode);
	}

	@Override
	public IBaseResource generateSnapshot(ValidationSupportContext theRootValidationSupport, IBaseResource theInput,
			String theUrl, String theWebUrl, String theProfileName)
	{
		return delegate.generateSnapshot(theRootValidationSupport, theInput, theUrl, theWebUrl, theProfileName);
	}

	@Override
	public void invalidateCaches()
	{
		codeSystems.clear();
		structureDefinitions.clear();
		valueSets.clear();

		fetchAllStructureDefinitionsDone.set(false);
		fetchAllConformanceResourcesDone.set(false);

		delegate.invalidateCaches();
	}
}
