package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.exception.BadBundleException;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventManager;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.SnapshotDependencyAnalyzer;
import org.highmed.dsf.fhir.service.SnapshotGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
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
	private final ReferenceCleaner referenceCleaner;
	private final ResponseGenerator responseGenerator;
	private final ExceptionHandler exceptionHandler;
	private final EventManager eventManager;
	private final EventGenerator eventGenerator;
	private final Function<Connection, SnapshotGenerator> snapshotGenerator;
	private final SnapshotDependencyAnalyzer snapshotDependencyAnalyzer;
	private final ParameterConverter parameterConverter;
	private final AuthorizationHelper authorizationHelper;

	public CommandFactoryImpl(String serverBase, int defaultPageCount, DataSource dataSource, DaoProvider daoProvider,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver,
			ReferenceCleaner referenceCleaner, ResponseGenerator responseGenerator, ExceptionHandler exceptionHandler,
			EventManager eventManager, EventGenerator eventGenerator,
			Function<Connection, SnapshotGenerator> snapshotGenerator,
			SnapshotDependencyAnalyzer snapshotDependencyAnalyzer, ParameterConverter parameterConverter,
			AuthorizationHelper authorizationHelper)
	{
		this.serverBase = serverBase;
		this.defaultPageCount = defaultPageCount;
		this.dataSource = dataSource;
		this.daoProvider = daoProvider;
		this.referenceExtractor = referenceExtractor;
		this.referenceResolver = referenceResolver;
		this.referenceCleaner = referenceCleaner;
		this.responseGenerator = responseGenerator;
		this.exceptionHandler = exceptionHandler;
		this.eventManager = eventManager;
		this.eventGenerator = eventGenerator;
		this.snapshotGenerator = snapshotGenerator;
		this.snapshotDependencyAnalyzer = snapshotDependencyAnalyzer;
		this.parameterConverter = parameterConverter;
		this.authorizationHelper = authorizationHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(daoProvider, "daoProvider");
		Objects.requireNonNull(referenceExtractor, "referenceExtractor");
		Objects.requireNonNull(referenceResolver, "referenceResolver");
		Objects.requireNonNull(referenceCleaner, "referenceCleaner");
		Objects.requireNonNull(responseGenerator, "responseGenerator");
		Objects.requireNonNull(exceptionHandler, "exceptionHandler");
		Objects.requireNonNull(eventManager, "eventManager");
		Objects.requireNonNull(eventGenerator, "eventGenerator");
		Objects.requireNonNull(snapshotGenerator, "snapshotGenerator");
		Objects.requireNonNull(snapshotDependencyAnalyzer, "snapshotDependencyAnalyzer");
		Objects.requireNonNull(parameterConverter, "parameterConverter");
		Objects.requireNonNull(authorizationHelper, "authorizationHelper");
	}

	// read, vread
	private Command get(int index, User user, Bundle bundle, BundleEntryComponent entry)
	{
		return new ReadCommand(index, user, bundle, entry, serverBase, authorizationHelper, defaultPageCount,
				daoProvider, parameterConverter, responseGenerator, exceptionHandler, referenceCleaner);
	}

	// create, conditional create
	private <R extends Resource> Command post(int index, User user, Bundle bundle, BundleEntryComponent entry,
			EventManager eventManager, R resource)
	{
		if (resource.getResourceType().name().equals(entry.getRequest().getUrl()))
		{
			@SuppressWarnings("unchecked")
			Optional<? extends ResourceDao<R>> dao = (Optional<? extends ResourceDao<R>>) daoProvider
					.getDao(resource.getClass());

			if (resource instanceof StructureDefinition)
				return new CreateStructureDefinitionCommand(index, user, bundle, entry, serverBase, authorizationHelper,
						(StructureDefinition) resource, (StructureDefinitionDao) dao.get(), exceptionHandler,
						parameterConverter, responseGenerator, referenceExtractor, referenceResolver, referenceCleaner,
						eventManager, eventGenerator, daoProvider.getStructureDefinitionSnapshotDao(),
						snapshotGenerator, snapshotDependencyAnalyzer);
			else
				return dao.map(d -> new CreateCommand<R, ResourceDao<R>>(index, user, bundle, entry, serverBase,
						authorizationHelper, resource, d, exceptionHandler, parameterConverter, responseGenerator,
						referenceExtractor, referenceResolver, referenceCleaner, eventManager, eventGenerator))
						.orElseThrow(() -> new IllegalStateException(
								"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			throw new IllegalStateException(
					"Request url " + entry.getRequest().getUrl() + " for method POST not supported");
	}

	// update, conditional update
	private <R extends Resource> Command put(int index, User user, Bundle bundle, BundleEntryComponent entry,
			EventManager eventManager, R resource)
	{
		if (entry.getRequest().getUrl() != null && !entry.getRequest().getUrl().isBlank()
				&& entry.getRequest().getUrl().startsWith(resource.getResourceType().name()))
		{
			@SuppressWarnings("unchecked")
			Optional<? extends ResourceDao<R>> dao = (Optional<? extends ResourceDao<R>>) daoProvider
					.getDao(resource.getClass());

			if (resource instanceof StructureDefinition)
				return new UpdateStructureDefinitionCommand(index, user, bundle, entry, serverBase, authorizationHelper,
						(StructureDefinition) resource, (StructureDefinitionDao) dao.get(), exceptionHandler,
						parameterConverter, responseGenerator, referenceExtractor, referenceResolver, referenceCleaner,
						eventManager, eventGenerator, daoProvider.getStructureDefinitionSnapshotDao(),
						snapshotGenerator, snapshotDependencyAnalyzer);
			else
				return dao.map(d -> new UpdateCommand<R, ResourceDao<R>>(index, user, bundle, entry, serverBase,
						authorizationHelper, resource, d, exceptionHandler, parameterConverter, responseGenerator,
						referenceExtractor, referenceResolver, referenceCleaner, eventManager, eventGenerator))
						.orElseThrow(() -> new IllegalStateException(
								"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			throw new IllegalStateException(
					"Request url " + entry.getRequest().getUrl() + " for method POST not supported");
	}

	// delete, conditional delete
	private Command delete(int index, User user, Bundle bundle, BundleEntryComponent entry, EventManager eventManager)
	{
		if (entry.getRequest().getUrl() != null && !entry.getRequest().getUrl().isBlank())
		{
			return new DeleteCommand(index, user, bundle, entry, serverBase, authorizationHelper, responseGenerator,
					daoProvider, exceptionHandler, parameterConverter, eventManager, eventGenerator);
		}
		else
			throw new BadBundleException(
					"Request url " + entry.getRequest().getUrl() + " for method DELETE not supported");
	}

	@Override
	public CommandList createCommands(Bundle bundle, User user) throws BadBundleException
	{
		Objects.requireNonNull(bundle, "bundle");
		Objects.requireNonNull(user, "user");

		if (bundle.getType() != null)
		{
			EventManager eventManager;
			if (BundleType.TRANSACTION.equals(bundle.getType()))
				eventManager = new TransactionEventManager(this.eventManager);
			else
				eventManager = this.eventManager;

			List<Command> commands = IntStream.range(0, bundle.getEntry().size())
					.mapToObj(index -> createCommand(index, bundle, user, bundle.getEntry().get(index), eventManager))
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

	protected Stream<Command> createCommand(int index, Bundle bundle, User user, BundleEntryComponent entry,
			EventManager eventManager)
	{
		if (entry.hasRequest() && entry.getRequest().hasMethod())
		{
			if (!entry.hasResource())
			{
				switch (entry.getRequest().getMethod())
				{
					case GET: // read
						return Stream.of(get(index, user, bundle, entry));
					case DELETE: // delete
						return Stream.of(delete(index, user, bundle, entry, eventManager));
					default:
						throw new BadBundleException("Request method " + entry.getRequest().getMethod() + " at index "
								+ index + " not supported without resource");
				}
			}
			else
			{
				switch (entry.getRequest().getMethod())
				{
					case POST: // create
						Command post = post(index, user, bundle, entry, eventManager, (Resource) entry.getResource());
						return resolveReferences(post, index, user, bundle, entry, (Resource) entry.getResource());
					case PUT: // update
						Command put = put(index, user, bundle, entry, eventManager, (Resource) entry.getResource());
						return resolveReferences(put, index, user, bundle, entry, (Resource) entry.getResource());
					default:
						throw new BadBundleException("Request method " + entry.getRequest().getMethod() + " at index "
								+ index + " not supported with resource");
				}
			}
		}
		else
			throw new BadBundleException("BundleEntry at index " + index + " has no request or request has no method");
	}

	private <R extends Resource> Stream<Command> resolveReferences(Command cmd, int index, User user, Bundle bundle,
			BundleEntryComponent entry, R resource)
	{
		@SuppressWarnings("unchecked")
		Optional<? extends ResourceDao<R>> dao = (Optional<? extends ResourceDao<R>>) daoProvider
				.getDao(resource.getClass());

		if (referenceExtractor.getReferences(resource).anyMatch(r -> true)) // at least one entry
		{
			return dao
					.map(d -> Stream.of(cmd,
							new ResolveReferencesCommand<R, ResourceDao<R>>(index, user, bundle, entry, serverBase,
									authorizationHelper, resource, d, exceptionHandler, parameterConverter,
									responseGenerator, referenceExtractor, referenceResolver)))
					.orElseThrow(() -> new IllegalStateException(
							"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			return Stream.of(cmd);
	}
}
