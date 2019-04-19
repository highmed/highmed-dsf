package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;

import org.highmed.fhir.dao.DomainResourceDao;
import org.highmed.fhir.dao.StructureDefinitionDao;
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
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.DomainResource;
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

	private <R extends DomainResource> Stream<Command> get(Bundle bundle, int index, BundleEntryComponent entry,
			R resource)
	{
		// TODO
		return Stream.of(new Command()
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
			public void preExecute(Connection connection) throws SQLException
			{
			}

			@Override
			public void execute(Connection connection) throws SQLException
			{
			}

			@Override
			public BundleEntryComponent postExecute(Connection connection) throws SQLException, WebApplicationException
			{
				return new BundleEntryComponent();
			}
		});
	}

	private <R extends DomainResource> Command post(Bundle bundle, int index, BundleEntryComponent entry, R resource)
	{
		if (resource.getResourceType().name().equals(entry.getRequest().getUrl()))
		{
			@SuppressWarnings("unchecked")
			Optional<? extends DomainResourceDao<R>> dao = (Optional<? extends DomainResourceDao<R>>) daoProvider
					.getDao(resource.getClass());

			if (resource instanceof StructureDefinition)
				return new CreateStructureDefinitionCommand(index, bundle, entry, (StructureDefinition) resource,
						serverBase, (StructureDefinitionDao) dao.get(), referenceReplacer, responseGenerator,
						exceptionHandler, eventManager, eventGenerator, daoProvider.getStructureDefinitionSnapshotDao(),
						snapshotGenerator, snapshotDependencyAnalyzer, parameterConverter);
			else
				return dao
						.map(d -> new CreateCommand<R, DomainResourceDao<R>>(index, bundle, entry, resource, serverBase,
								d, referenceReplacer, responseGenerator, exceptionHandler, eventManager,
								eventGenerator))
						.orElseThrow(() -> new IllegalStateException(
								"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			throw new IllegalStateException(
					"Request url " + entry.getRequest().getUrl() + " for method POST not supported");
	}

	private <R extends DomainResource> Command put(Bundle bundle, int index, BundleEntryComponent entry, R resource)
	{
		if (resource.getResourceType().name().equals(entry.getRequest().getUrl()))
		{
			@SuppressWarnings("unchecked")
			Optional<? extends DomainResourceDao<R>> dao = (Optional<? extends DomainResourceDao<R>>) daoProvider
					.getDao(resource.getClass());

			if (resource instanceof StructureDefinition)
				return new UpdateStructureDefinitionCommand(index, bundle, entry, (StructureDefinition) resource,
						serverBase, (StructureDefinitionDao) dao.get(), referenceReplacer, responseGenerator,
						exceptionHandler, eventManager, eventGenerator, daoProvider.getStructureDefinitionSnapshotDao(),
						snapshotGenerator, snapshotDependencyAnalyzer, parameterConverter);
			else
				return dao
						.map(d -> new UpdateCommand<R, DomainResourceDao<R>>(index, bundle, entry, resource, serverBase,
								d, referenceReplacer, responseGenerator, exceptionHandler, eventManager,
								eventGenerator))
						.orElseThrow(() -> new IllegalStateException(
								"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			throw new IllegalStateException(
					"Request url " + entry.getRequest().getUrl() + " for method POST not supported");
	}

	private <R extends DomainResource> Stream<Command> delete(Bundle bundle, int index, BundleEntryComponent entry,
			R resource)
	{
		// TODO
		return Stream.of(new Command()
		{
			@Override
			public int getIndex()
			{
				return index;
			}

			@Override
			public int getTransactionPriority()
			{
				return 1;
			}

			@Override
			public void preExecute(Connection connection) throws SQLException
			{
			}

			@Override
			public void execute(Connection connection) throws SQLException
			{
			}

			@Override
			public BundleEntryComponent postExecute(Connection connection) throws SQLException, WebApplicationException
			{
				return new BundleEntryComponent();
			}
		});
	}

	public CommandList createCommands(Bundle bundle)
	{
		Objects.requireNonNull(bundle, "bundle");
		Objects.requireNonNull(bundle.getType(), "bundle.type");

		if (BundleType.BATCH.equals(bundle.getType()) || BundleType.TRANSACTION.equals(bundle.getType()))
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
					// see below
			}
		}

		throw new IllegalStateException("Unsupported bundle type " + bundle.getType());
	}

	public Stream<Command> createCommand(Bundle bundle, int index, BundleEntryComponent entry)
	{
		if (entry.hasRequest() && entry.getRequest().hasMethod() && entry.hasResource()
				&& entry.getResource() instanceof DomainResource)
		{
			switch (entry.getRequest().getMethod())
			{
				case GET:
					return get(bundle, index, entry, (DomainResource) entry.getResource());
				case POST:
					Command post = post(bundle, index, entry, (DomainResource) entry.getResource());
					return resolveReferences(post, bundle, index, entry, (DomainResource) entry.getResource());
				case PUT:
					Command put = put(bundle, index, entry, (DomainResource) entry.getResource());
					return resolveReferences(put, bundle, index, entry, (DomainResource) entry.getResource());
				case DELETE:
					return delete(bundle, index, entry, (DomainResource) entry.getResource());
				default:
					throw new IllegalStateException(
							"Request method " + entry.getRequest().getMethod() + " not supported");
			}
		}
		else
			throw new IllegalStateException(
					"BundleEntry has no request or request has no method or request has no resource of type "
							+ DomainResource.class.getName());
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
							new ResolveReferencesCommand<R, DomainResourceDao<R>>(index, bundle, entry, resource,
									serverBase, d)))
					.orElseThrow(() -> new IllegalStateException(
							"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			return Stream.of(cmd);
	}

	// public static void main(String[] args)
	// {
	// var b = new Bundle();
	// var e1 = b.addEntry();
	// e1.setFullUrl("urn:uid:" + UUID.randomUUID().toString());
	// var r1 = e1.getRequest();
	// r1.setUrl("Patient");
	// r1.setMethod(HTTPVerb.POST);
	// e1.setResource(new Patient().setActive(true));
	//
	// PatientDao patienDao = new PatientDaoJdbc(null, null);
	// DaoProvider daoProvider = new DaoProviderImpl(null, null, null, null, null, patienDao, null, null, null, null,
	// null,
	// null, null, null, null);
	// var f = new CommandFactory(null, daoProvider, null, null, null, null, null);
	// List<JdbcCommand> c = f.createCommands(b);
	//
	// System.out.println(c.size());
	// }
}
