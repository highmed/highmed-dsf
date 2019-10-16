package org.highmed.dsf.fhir.dao.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.exception.BadBundleException;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventManager;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.SnapshotDependencyAnalyzer;
import org.highmed.dsf.fhir.service.SnapshotGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.springframework.beans.factory.InitializingBean;

public class CommandFactoryImpl implements InitializingBean, CommandFactory
{
	private final String serverBase;
	private final int defaultPageCount;
	private final DataSource dataSource;
	private final DaoProvider daoProvider;
	private final ReferenceExtractor referenceExtractor;
	private final ReferenceResolver referenceResolver;
	private final ResponseGenerator responseGenerator;
	private final ExceptionHandler exceptionHandler;
	private final EventManager eventManager;
	private final EventGenerator eventGenerator;
	private final SnapshotGenerator snapshotGenerator;
	private final SnapshotDependencyAnalyzer snapshotDependencyAnalyzer;
	private final ParameterConverter parameterConverter;

	public CommandFactoryImpl(String serverBase, int defaultPageCount, DataSource dataSource, DaoProvider daoProvider,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver,
			ResponseGenerator responseGenerator, ExceptionHandler exceptionHandler, EventManager eventManager,
			EventGenerator eventGenerator, SnapshotGenerator snapshotGenerator,
			SnapshotDependencyAnalyzer snapshotDependencyAnalyzer, ParameterConverter parameterConverter)
	{
		this.serverBase = serverBase;
		this.defaultPageCount = defaultPageCount;
		this.dataSource = dataSource;
		this.daoProvider = daoProvider;
		this.referenceExtractor = referenceExtractor;
		this.referenceResolver = referenceResolver;
		this.responseGenerator = responseGenerator;
		this.exceptionHandler = exceptionHandler;
		this.eventManager = eventManager;
		this.eventGenerator = eventGenerator;
		this.snapshotGenerator = snapshotGenerator;
		this.snapshotDependencyAnalyzer = snapshotDependencyAnalyzer;
		this.parameterConverter = parameterConverter;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(daoProvider, "daoProvider");
		Objects.requireNonNull(referenceExtractor, "referenceExtractor");
		Objects.requireNonNull(referenceResolver, "referenceResolver");
		Objects.requireNonNull(responseGenerator, "responseGenerator");
		Objects.requireNonNull(exceptionHandler, "exceptionHandler");
		Objects.requireNonNull(eventManager, "eventManager");
		Objects.requireNonNull(eventGenerator, "eventGenerator");
		Objects.requireNonNull(snapshotGenerator, "snapshotGenerator");
		Objects.requireNonNull(snapshotDependencyAnalyzer, "snapshotDependencyAnalyzer");
		Objects.requireNonNull(parameterConverter, "parameterConverter");
	}

	// read, vread
	private Command get(Bundle bundle, int index, BundleEntryComponent entry)
	{
		return new ReadCommand(index, bundle, entry, serverBase, defaultPageCount, daoProvider, parameterConverter,
				responseGenerator, exceptionHandler);
	}

	// create, conditional create
	private <R extends Resource> Command post(Bundle bundle, int index, BundleEntryComponent entry,
			EventManager eventManager, R resource)
	{
		if (resource.getResourceType().name().equals(entry.getRequest().getUrl()))
		{
			@SuppressWarnings("unchecked")
			Optional<? extends ResourceDao<R>> dao = (Optional<? extends ResourceDao<R>>) daoProvider
					.getDao(resource.getClass());

			if (resource instanceof StructureDefinition)
				return new CreateStructureDefinitionCommand(index, bundle, entry, serverBase,
						(StructureDefinition) resource, (StructureDefinitionDao) dao.get(), exceptionHandler,
						parameterConverter, responseGenerator, eventManager, eventGenerator,
						daoProvider.getStructureDefinitionSnapshotDao(), snapshotGenerator, snapshotDependencyAnalyzer);
			else
				return dao
						.map(d -> new CreateCommand<R, ResourceDao<R>>(index, bundle, entry, serverBase, resource, d,
								exceptionHandler, parameterConverter, responseGenerator, eventManager, eventGenerator))
						.orElseThrow(() -> new IllegalStateException(
								"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			throw new IllegalStateException(
					"Request url " + entry.getRequest().getUrl() + " for method POST not supported");
	}

	// update, conditional update
	private <R extends DomainResource> Command put(Bundle bundle, int index, BundleEntryComponent entry,
			EventManager eventManager, R resource)
	{
		if (entry.getRequest().getUrl() != null && !entry.getRequest().getUrl().isBlank()
				&& entry.getRequest().getUrl().startsWith(resource.getResourceType().name()))
		{
			@SuppressWarnings("unchecked")
			Optional<? extends ResourceDao<R>> dao = (Optional<? extends ResourceDao<R>>) daoProvider
					.getDao(resource.getClass());

			if (resource instanceof StructureDefinition)
				return new UpdateStructureDefinitionCommand(index, bundle, entry, serverBase,
						(StructureDefinition) resource, (StructureDefinitionDao) dao.get(), exceptionHandler,
						parameterConverter, responseGenerator, eventManager, eventGenerator,
						daoProvider.getStructureDefinitionSnapshotDao(), snapshotGenerator, snapshotDependencyAnalyzer);
			else
				return dao
						.map(d -> new UpdateCommand<R, ResourceDao<R>>(index, bundle, entry, serverBase, resource, d,
								exceptionHandler, parameterConverter, responseGenerator, eventManager, eventGenerator))
						.orElseThrow(() -> new IllegalStateException(
								"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			throw new IllegalStateException(
					"Request url " + entry.getRequest().getUrl() + " for method POST not supported");
	}

	// delete, conditional delete
	private Command delete(Bundle bundle, int index, BundleEntryComponent entry, EventManager eventManager)
	{
		if (entry.getRequest().getUrl() != null && !entry.getRequest().getUrl().isBlank())
		{
			return new DeleteCommand(index, bundle, entry, serverBase, responseGenerator, daoProvider, exceptionHandler,
					parameterConverter, eventManager, eventGenerator);
		}
		else
			throw new BadBundleException(
					"Request url " + entry.getRequest().getUrl() + " for method DELETE not supported");
	}

	@Override
	public CommandList createCommands(Bundle bundle) throws BadBundleException
	{
		Objects.requireNonNull(bundle, "bundle");

		if (bundle.getType() != null)
		{
			EventManager eventManager;
			if (BundleType.TRANSACTION.equals(bundle.getType()))
				eventManager = new TransactionEventManager(this.eventManager);
			else
				eventManager = this.eventManager;

			List<Command> commands = IntStream.range(0, bundle.getEntry().size())
					.mapToObj(index -> createCommand(bundle, index, bundle.getEntry().get(index), eventManager))
					.flatMap(Function.identity()).collect(Collectors.toList());

			switch (bundle.getType())
			{
				case BATCH:
					return new BatchCommandList(dataSource, exceptionHandler, commands);
				case TRANSACTION:
					return new TransactionCommandList(dataSource, exceptionHandler, commands,
							(TransactionEventManager) eventManager);
				default:
					throw new BadBundleException("Unsupported bundle type " + bundle.getType());
			}
		}
		else
			throw new BadBundleException("Missing bundle type");
	}

	protected Stream<Command> createCommand(Bundle bundle, int index, BundleEntryComponent entry,
			EventManager eventManager)
	{
		if (entry.hasRequest() && entry.getRequest().hasMethod())
		{
			if (!entry.hasResource())
			{
				switch (entry.getRequest().getMethod())
				{
					case GET:
						return Stream.of(get(bundle, index, entry));
					case DELETE:
						return Stream.of(delete(bundle, index, entry, eventManager));
					default:
						throw new BadBundleException("Request method " + entry.getRequest().getMethod() + " at index "
								+ index + " not supported without resource of type " + DomainResource.class.getName());
				}
			}
			else if (entry.getResource() instanceof DomainResource)
			{
				switch (entry.getRequest().getMethod())
				{
					case POST: // create
						Command post = post(bundle, index, entry, eventManager, (DomainResource) entry.getResource());
						return resolveReferences(post, bundle, index, entry, (DomainResource) entry.getResource());
					case PUT: // update
						Command put = put(bundle, index, entry, eventManager, (DomainResource) entry.getResource());
						return resolveReferences(put, bundle, index, entry, (DomainResource) entry.getResource());
					default:
						throw new BadBundleException("Request method " + entry.getRequest().getMethod() + " at index "
								+ index + " not supported with resource");
				}
			}
			else
				throw new BadBundleException("BundleEntry  at index " + index + " has no resource of type "
						+ DomainResource.class.getName());
		}
		else
			throw new BadBundleException("BundleEntry at index " + index + " has no request or request has no method");
	}

	private <R extends DomainResource> Stream<Command> resolveReferences(Command cmd, Bundle bundle, int index,
			BundleEntryComponent entry, R resource)
	{
		@SuppressWarnings("unchecked")
		Optional<? extends ResourceDao<R>> dao = (Optional<? extends ResourceDao<R>>) daoProvider
				.getDao(resource.getClass());

		if (referenceExtractor.getReferences(resource).anyMatch(r -> true)) // at least one entry
		{
			return dao
					.map(d -> Stream.of(cmd,
							new ResolveReferencesCommand<R, ResourceDao<R>>(index, bundle, entry, serverBase, resource,
									d, exceptionHandler, parameterConverter, referenceExtractor, responseGenerator,
									referenceResolver)))
					.orElseThrow(() -> new IllegalStateException(
							"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			return Stream.of(cmd);
	}
}
