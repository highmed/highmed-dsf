package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.ws.rs.WebApplicationException;

import org.highmed.fhir.dao.DomainResourceDao;
import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.dao.provider.DaoProvider;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ResponseGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;

public class ResolveReferencesCommand<R extends DomainResource, D extends DomainResourceDao<R>>
		extends AbstractCommand<R, D> implements Command
{
	private final ReferenceExtractor referenceExtractor;
	private final ResponseGenerator responseGenerator;
	private final DaoProvider daoProvider;
	private final ExceptionHandler exceptionHandler;

	public ResolveReferencesCommand(int index, Bundle bundle, BundleEntryComponent entry, R resource, String serverBase,
			D dao, ReferenceExtractor referenceExtractor, ResponseGenerator responseGenerator, DaoProvider daoProvider,
			ExceptionHandler exceptionHandler)
	{
		super(5, index, bundle, entry, resource, serverBase, dao);

		this.referenceExtractor = referenceExtractor;
		this.responseGenerator = responseGenerator;
		this.daoProvider = daoProvider;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public void preExecute(Map<String, IdType> idTranslationTable)
	{
	}

	@Override
	public void execute(Map<String, IdType> idTranslationTable, Connection connection)
			throws SQLException, WebApplicationException
	{
		R latest = latest(idTranslationTable, connection);

		boolean resourceNeedsUpdated = referenceExtractor.getReferences(latest)
				.map(resolveReference(idTranslationTable)).anyMatch(b -> b);
		if (resourceNeedsUpdated)
		{
			try
			{
				dao.updateSameRowWithTransaction(connection, latest);
			}
			catch (ResourceNotFoundException e)
			{
				throw exceptionHandler.internalServerError(e);
			}
		}
	}

	private R latest(Map<String, IdType> idTranslationTable, Connection connection) throws SQLException
	{
		try
		{
			String id = idTranslationTable.get(entry.getFullUrl()).getIdPart();
			return dao.readWithTransaction(connection, toUuid(resource.getResourceType().name(), id))
					.orElseThrow(() -> exceptionHandler.internalServerError(new ResourceNotFoundException(id)));
		}
		catch (ResourceDeletedException e)
		{
			throw exceptionHandler.internalServerError(e);
		}
	}

	public UUID toUuid(String resourceTypeName, String id)
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
			throw exceptionHandler.notFound(resourceTypeName, e);
		}
	}

	private Function<ResourceReference, Boolean> resolveReference(Map<String, IdType> idTranslationTable)
			throws WebApplicationException
	{
		return resourceReference ->
		{
			switch (resourceReference.getType(serverBase))
			{
				case TEMPORARY:
					return resolveTemporaryReference(resourceReference, idTranslationTable);
				case LITERAL_INTERNAL:
					return resolveLiteralInternalReference(resourceReference);
				case LITERAL_EXTERNAL:
					return resolveLiteralExternalReference(resourceReference);
				case CONDITIONAL:
					return resolveConditionalReference(resourceReference);
				case LOGICAL:
					return resolveLogicalReference(resourceReference);
				case UNKNOWN:
				default:
					throw new WebApplicationException(
							responseGenerator.unknownReference(index, resource, resourceReference));
			}
		};
	}

	private boolean resolveTemporaryReference(ResourceReference resourceReference,
			Map<String, IdType> idTranslationTable)
	{
		IdType newId = idTranslationTable.get(resourceReference.getReference().getReference());
		if (newId == null)
			throw new WebApplicationException(responseGenerator.unknownReference(index, resource, resourceReference));
		else
			resourceReference.getReference().setReferenceElement(newId);

		return true; // throws exception if reference could not be resolved
	}

	private boolean resolveLiteralInternalReference(ResourceReference resourceReference)
	{
		IdType id = new IdType(resourceReference.getReference().getReference());
		Optional<DomainResourceDao<?>> referenceDao = daoProvider.getDao(id.getResourceType());

		if (referenceDao.isEmpty())
			throw new WebApplicationException(responseGenerator.referenceTargetTypeNotSupportedByImplementation(index,
					resource, resourceReference));
		else
		{
			DomainResourceDao<?> d = referenceDao.get();
			if (!resourceReference.supportsType(d.getResourceType()))
				throw new WebApplicationException(responseGenerator.referenceTargetTypeNotSupportedByResource(index,
						resource, resourceReference));

			Optional<IdType> localId = exceptionHandler
					.handleSqlException(() -> d.exists(id.getIdPart(), id.getVersionIdPart()));
			if (localId.isEmpty())
				throw new WebApplicationException(
						responseGenerator.referenceTargetNotFoundLocally(index, resource, resourceReference));
		}

		return false; // throws exception if reference could not be resolved
	}

	private boolean resolveLiteralExternalReference(ResourceReference resourceReference)
	{
		// TODO Auto-generated method stub
		// check type of referenced resource is acceptable

		return false; // throws exception if reference could not be resolvedF
	}

	private boolean resolveConditionalReference(ResourceReference resourceReference)
	{
		// TODO Auto-generated method stub
		// check type of referenced resource is acceptable ?

		return true; // throws exception if reference could not be resolved
	}

	private boolean resolveLogicalReference(ResourceReference resourceReference)
	{
		// TODO Auto-generated method stub
		// check type of referenced resource is acceptable ?

		return true; // throws exception if reference could not be resolved
	}

	@Override
	public BundleEntryComponent postExecute()
	{
		return null;
	}
}
