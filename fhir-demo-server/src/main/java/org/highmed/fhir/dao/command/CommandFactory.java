package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.highmed.fhir.dao.DomainResourceDao;
import org.highmed.fhir.dao.StructureDefinitionDao;
import org.highmed.fhir.dao.exception.BadBundleException;
import org.highmed.fhir.dao.provider.DaoProvider;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.SnapshotDependencyAnalyzer;
import org.highmed.fhir.service.SnapshotGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.springframework.beans.factory.InitializingBean;

public class CommandFactory implements InitializingBean
{
	private final String serverBase;
	private final DataSource dataSource;
	private final DaoProvider daoProvider;
	private final ReferenceReplacer referenceReplacer;
	private final ReferenceExtractor referenceExtractor;
	private final ResponseGenerator responseGenerator;
	private final ExceptionHandler exceptionHandler;
	private final EventManager eventManager;
	private final EventGenerator eventGenerator;
	private final SnapshotGenerator snapshotGenerator;
	private final SnapshotDependencyAnalyzer snapshotDependencyAnalyzer;
	private final ParameterConverter parameterConverter;

	public CommandFactory(String serverBase, DataSource dataSource, DaoProvider daoProvider,
			ReferenceReplacer referenceReplacer, ReferenceExtractor referenceExtractor,
			ResponseGenerator responseGenerator, ExceptionHandler exceptionHandler, EventManager eventManager,
			EventGenerator eventGenerator, SnapshotGenerator snapshotGenerator,
			SnapshotDependencyAnalyzer snapshotDependencyAnalyzer, ParameterConverter parameterConverter)
	{
		this.serverBase = serverBase;
		this.dataSource = dataSource;
		this.daoProvider = daoProvider;
		this.referenceReplacer = referenceReplacer;
		this.referenceExtractor = referenceExtractor;
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
		Objects.requireNonNull(referenceReplacer, "referenceReplacer");
		Objects.requireNonNull(referenceExtractor, "referenceExtractor");
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
		// TODO
		return new Command()
		{
			@Override
			public int getIndex()
			{
				return index;
			}

			@Override
			public int getTransactionPriority()
			{
				return 4;
			}

			@Override
			public void preExecute(Map<String, IdType> idTranslationTable)
			{
			}

			@Override
			public void execute(Map<String, IdType> idTranslationTable, Connection connection) throws SQLException
			{
			}

			@Override
			public BundleEntryComponent postExecute()
			{
				return new BundleEntryComponent();
			}
		};
	}

	// create, conditional create
	private <R extends DomainResource> Command post(Bundle bundle, int index, BundleEntryComponent entry, R resource)
	{
		if (resource.getResourceType().name().equals(entry.getRequest().getUrl()))
		{
			@SuppressWarnings("unchecked")
			Optional<? extends DomainResourceDao<R>> dao = (Optional<? extends DomainResourceDao<R>>) daoProvider
					.getDao(resource.getClass());

			if (resource instanceof StructureDefinition)
				return new CreateStructureDefinitionCommand(index, bundle, entry, serverBase,
						(StructureDefinition) resource, (StructureDefinitionDao) dao.get(), referenceReplacer,
						responseGenerator, exceptionHandler, eventManager, eventGenerator, parameterConverter,
						daoProvider.getStructureDefinitionSnapshotDao(), snapshotGenerator, snapshotDependencyAnalyzer);
			else
				return dao
						.map(d -> new CreateCommand<R, DomainResourceDao<R>>(index, bundle, entry, serverBase, resource,
								d, referenceReplacer, responseGenerator, exceptionHandler, eventManager, eventGenerator,
								parameterConverter))
						.orElseThrow(() -> new IllegalStateException(
								"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			throw new IllegalStateException(
					"Request url " + entry.getRequest().getUrl() + " for method POST not supported");
	}

	// update, conditional update
	private <R extends DomainResource> Command put(Bundle bundle, int index, BundleEntryComponent entry, R resource)
	{
		if (entry.getRequest().getUrl() != null && !entry.getRequest().getUrl().isBlank()
				&& entry.getRequest().getUrl().startsWith(resource.getResourceType().name()))
		{
			@SuppressWarnings("unchecked")
			Optional<? extends DomainResourceDao<R>> dao = (Optional<? extends DomainResourceDao<R>>) daoProvider
					.getDao(resource.getClass());

			if (resource instanceof StructureDefinition)
				return new UpdateStructureDefinitionCommand(index, bundle, entry, serverBase,
						(StructureDefinition) resource, (StructureDefinitionDao) dao.get(), referenceReplacer,
						responseGenerator, exceptionHandler, eventManager, eventGenerator,
						daoProvider.getStructureDefinitionSnapshotDao(), snapshotGenerator, snapshotDependencyAnalyzer,
						parameterConverter);
			else
				return dao
						.map(d -> new UpdateCommand<R, DomainResourceDao<R>>(index, bundle, entry, serverBase, resource,
								d, referenceReplacer, responseGenerator, exceptionHandler, eventManager,
								eventGenerator))
						.orElseThrow(() -> new IllegalStateException(
								"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			throw new IllegalStateException(
					"Request url " + entry.getRequest().getUrl() + " for method POST not supported");
	}

	// delete, conditional delete
	private Command delete(Bundle bundle, int index, BundleEntryComponent entry)
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

	public CommandList createCommands(Bundle bundle) throws BadBundleException
	{
		Objects.requireNonNull(bundle, "bundle");

		if (bundle.getType() != null)
		{
			List<Command> commands = IntStream.range(0, bundle.getEntry().size())
					.mapToObj(index -> createCommand(bundle, index, bundle.getEntry().get(index)))
					.flatMap(Function.identity()).collect(Collectors.toList());

			switch (bundle.getType())
			{
				case BATCH:
					return new BatchCommandList(dataSource, exceptionHandler, commands);
				case TRANSACTION:
					return new TransactionCommandList(dataSource, exceptionHandler, commands);
				default:
					throw new BadBundleException("Unsupported bundle type " + bundle.getType());
			}
		}
		else
			throw new BadBundleException("Missing bundle type");
	}

	public Stream<Command> createCommand(Bundle bundle, int index, BundleEntryComponent entry)
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
						return Stream.of(delete(bundle, index, entry));
					default:
						throw new BadBundleException("Request method " + entry.getRequest().getMethod() + " at index "
								+ index + " not supported without resource of type " + DomainResource.class.getName());
				}
			}
			else if (entry.getResource() instanceof DomainResource)
			{
				switch (entry.getRequest().getMethod())
				{
					case POST:
						Command post = post(bundle, index, entry, (DomainResource) entry.getResource());
						return resolveReferences(post, bundle, index, entry, (DomainResource) entry.getResource());
					case PUT:
						Command put = put(bundle, index, entry, (DomainResource) entry.getResource());
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
		Optional<? extends DomainResourceDao<R>> dao = (Optional<? extends DomainResourceDao<R>>) daoProvider
				.getDao(resource.getClass());

		if (referenceExtractor.getReferences(resource).anyMatch(r -> true))
		{
			return dao
					.map(d -> Stream.of(cmd,
							new ResolveReferencesCommand<R, DomainResourceDao<R>>(index, bundle, entry, serverBase,
									resource, d, referenceExtractor, responseGenerator, daoProvider, exceptionHandler)))
					.orElseThrow(() -> new IllegalStateException(
							"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			return Stream.of(cmd);
	}
}
