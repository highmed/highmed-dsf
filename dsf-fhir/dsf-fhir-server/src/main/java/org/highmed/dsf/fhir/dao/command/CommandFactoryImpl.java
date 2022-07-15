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
import org.highmed.dsf.fhir.event.EventHandler;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.prefer.PreferHandlingType;
import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.SnapshotGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
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
	private final ParameterConverter parameterConverter;
	private final EventHandler eventHandler;
	private final EventGenerator eventGenerator;
	private final AuthorizationHelper authorizationHelper;
	private final ValidationHelper validationHelper;
	private final SnapshotGenerator snapshotGenerator;
	private final Function<Connection, TransactionResources> transactionResourcesFactory;

	public CommandFactoryImpl(String serverBase, int defaultPageCount, DataSource dataSource, DaoProvider daoProvider,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver,
			ReferenceCleaner referenceCleaner, ResponseGenerator responseGenerator, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, EventHandler eventHandler, EventGenerator eventGenerator,
			AuthorizationHelper authorizationHelper, ValidationHelper validationHelper,
			SnapshotGenerator snapshotGenerator, Function<Connection, TransactionResources> transactionResourcesFactory)
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
		this.parameterConverter = parameterConverter;
		this.eventHandler = eventHandler;
		this.eventGenerator = eventGenerator;
		this.authorizationHelper = authorizationHelper;
		this.validationHelper = validationHelper;
		this.snapshotGenerator = snapshotGenerator;
		this.transactionResourcesFactory = transactionResourcesFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(dataSource, "dataSource");
		Objects.requireNonNull(daoProvider, "daoProvider");
		Objects.requireNonNull(referenceExtractor, "referenceExtractor");
		Objects.requireNonNull(referenceResolver, "referenceResolver");
		Objects.requireNonNull(referenceCleaner, "referenceCleaner");
		Objects.requireNonNull(responseGenerator, "responseGenerator");
		Objects.requireNonNull(exceptionHandler, "exceptionHandler");
		Objects.requireNonNull(parameterConverter, "parameterConverter");
		Objects.requireNonNull(eventHandler, "eventHandler");
		Objects.requireNonNull(eventGenerator, "eventGenerator");
		Objects.requireNonNull(authorizationHelper, "authorizationHelper");
		Objects.requireNonNull(validationHelper, "validationHelper");
		Objects.requireNonNull(snapshotGenerator, "snapshotGenerator");
		Objects.requireNonNull(transactionResourcesFactory, "transactionResourcesFactory");
	}

	// head
	private Command head(int index, User user, PreferReturnType returnType, Bundle bundle, BundleEntryComponent entry,
			PreferHandlingType handlingType)
	{
		return new HeadCommand(index, user, returnType, bundle, entry, serverBase, authorizationHelper,
				defaultPageCount, daoProvider, parameterConverter, responseGenerator, exceptionHandler,
				referenceCleaner, handlingType);
	}

	// read, vread
	private Command get(int index, User user, PreferReturnType returnType, Bundle bundle, BundleEntryComponent entry,
			PreferHandlingType handlingType)
	{
		return new ReadCommand(index, user, returnType, bundle, entry, serverBase, authorizationHelper,
				defaultPageCount, daoProvider, parameterConverter, responseGenerator, exceptionHandler,
				referenceCleaner, handlingType);
	}

	// create, conditional create
	private <R extends Resource> Command post(int index, User user, PreferReturnType returnType, Bundle bundle,
			BundleEntryComponent entry, R resource)
	{
		if (resource.getResourceType().name().equals(entry.getRequest().getUrl()))
		{
			@SuppressWarnings("unchecked")
			Optional<? extends ResourceDao<R>> dao = (Optional<? extends ResourceDao<R>>) daoProvider
					.getDao(resource.getClass());

			if (resource instanceof StructureDefinition)
				return new CreateStructureDefinitionCommand(index, user, returnType, bundle, entry, serverBase,
						authorizationHelper, (StructureDefinition) resource, (StructureDefinitionDao) dao.get(),
						exceptionHandler, parameterConverter, responseGenerator, referenceExtractor, referenceResolver,
						referenceCleaner, eventGenerator, daoProvider.getStructureDefinitionSnapshotDao());
			else
				return dao.map(d -> new CreateCommand<R, ResourceDao<R>>(index, user, returnType, bundle, entry,
						serverBase, authorizationHelper, resource, d, exceptionHandler, parameterConverter,
						responseGenerator, referenceExtractor, referenceResolver, referenceCleaner, eventGenerator))
						.orElseThrow(() -> new IllegalStateException(
								"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			throw new IllegalStateException(
					"Request url " + entry.getRequest().getUrl() + " for method POST not supported");
	}

	// update, conditional update
	private <R extends Resource> Command put(int index, User user, PreferReturnType returnType, Bundle bundle,
			BundleEntryComponent entry, R resource)
	{
		if (entry.getRequest().getUrl() != null && !entry.getRequest().getUrl().isBlank()
				&& entry.getRequest().getUrl().startsWith(resource.getResourceType().name()))
		{
			@SuppressWarnings("unchecked")
			Optional<? extends ResourceDao<R>> dao = (Optional<? extends ResourceDao<R>>) daoProvider
					.getDao(resource.getClass());

			if (resource instanceof StructureDefinition)
				return new UpdateStructureDefinitionCommand(index, user, returnType, bundle, entry, serverBase,
						authorizationHelper, (StructureDefinition) resource, (StructureDefinitionDao) dao.get(),
						exceptionHandler, parameterConverter, responseGenerator, referenceExtractor, referenceResolver,
						referenceCleaner, eventGenerator, daoProvider.getStructureDefinitionSnapshotDao());
			else
				return dao.map(d -> new UpdateCommand<R, ResourceDao<R>>(index, user, returnType, bundle, entry,
						serverBase, authorizationHelper, resource, d, exceptionHandler, parameterConverter,
						responseGenerator, referenceExtractor, referenceResolver, referenceCleaner, eventGenerator))
						.orElseThrow(() -> new IllegalStateException(
								"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			throw new IllegalStateException(
					"Request url " + entry.getRequest().getUrl() + " for method POST not supported");
	}

	// delete, conditional delete
	private Command delete(int index, User user, PreferReturnType returnType, Bundle bundle, BundleEntryComponent entry)
	{
		if (entry.getRequest().getUrl() != null && !entry.getRequest().getUrl().isBlank())
		{
			if (entry.getRequest().getUrl().startsWith("StructureDefinition"))
				return new DeleteStructureDefinitionCommand(index, user, returnType, bundle, entry, serverBase,
						authorizationHelper, responseGenerator, daoProvider, exceptionHandler, parameterConverter,
						eventGenerator);
			else
				return new DeleteCommand(index, user, returnType, bundle, entry, serverBase, authorizationHelper,
						responseGenerator, daoProvider, exceptionHandler, parameterConverter, eventGenerator);
		}
		else
			throw new BadBundleException(
					"Request url " + entry.getRequest().getUrl() + " for method DELETE not supported");
	}

	@Override
	public CommandList createCommands(Bundle bundle, User user, PreferReturnType returnType,
			PreferHandlingType handlingType) throws BadBundleException
	{
		Objects.requireNonNull(bundle, "bundle");
		Objects.requireNonNull(user, "user");
		Objects.requireNonNull(returnType, "returnType");
		Objects.requireNonNull(handlingType, "handlingType");

		if (bundle.getType() != null)
		{
			List<Command> commands = IntStream.range(0, bundle.getEntry().size()).mapToObj(
					index -> createCommand(index, user, returnType, handlingType, bundle, bundle.getEntry().get(index)))
					.flatMap(Function.identity()).collect(Collectors.toList());

			switch (bundle.getType())
			{
				case BATCH:
					return new BatchCommandList(dataSource, exceptionHandler, validationHelper, snapshotGenerator,
							eventHandler, commands);
				case TRANSACTION:
					return new TransactionCommandList(dataSource, exceptionHandler, transactionResourcesFactory,
							commands);
				default:
					throw new BadBundleException("Unsupported bundle type " + bundle.getType());
			}
		}
		else
			throw new BadBundleException("Missing bundle type");
	}

	protected Stream<Command> createCommand(int index, User user, PreferReturnType returnType,
			PreferHandlingType handlingType, Bundle bundle, BundleEntryComponent entry)
	{
		if (entry.hasRequest() && entry.getRequest().hasMethod())
		{
			if (!entry.hasResource())
			{
				switch (entry.getRequest().getMethod())
				{
					case GET: // read, vread
						return Stream.of(get(index, user, returnType, bundle, entry, handlingType));
					case HEAD: // head -> read, vread
						return Stream.of(head(index, user, returnType, bundle, entry, handlingType));
					case DELETE: // delete
						return Stream.of(delete(index, user, returnType, bundle, entry));
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
						Command post = post(index, user, returnType, bundle, entry, (Resource) entry.getResource());
						return resolveReferences(post, index, user, returnType, bundle, entry,
								(Resource) entry.getResource(), HTTPVerb.POST);
					case PUT: // update
						Command put = put(index, user, returnType, bundle, entry, (Resource) entry.getResource());
						return resolveReferences(put, index, user, returnType, bundle, entry,
								(Resource) entry.getResource(), HTTPVerb.PUT);
					default:
						throw new BadBundleException("Request method " + entry.getRequest().getMethod() + " at index "
								+ index + " not supported with resource");
				}
			}
		}
		else
			throw new BadBundleException("BundleEntry at index " + index + " has no request or request has no method");
	}

	private <R extends Resource> Stream<Command> resolveReferences(Command cmd, int index, User user,
			PreferReturnType returnType, Bundle bundle, BundleEntryComponent entry, R resource, HTTPVerb verb)
	{
		@SuppressWarnings("unchecked")
		Optional<? extends ResourceDao<R>> dao = (Optional<? extends ResourceDao<R>>) daoProvider
				.getDao(resource.getClass());

		if (referenceExtractor.getReferences(resource).anyMatch(r -> true)) // at least one entry
		{
			return dao
					.map(d -> Stream.of(cmd,
							new CheckReferencesCommand<R, ResourceDao<R>>(index, user, returnType, bundle, entry,
									serverBase, authorizationHelper, resource, verb, d, exceptionHandler,
									parameterConverter, responseGenerator, referenceExtractor, referenceResolver)))
					.orElseThrow(() -> new IllegalStateException(
							"Resource of type " + resource.getClass().getName() + " not supported"));
		}
		else
			return Stream.of(cmd);
	}
}
