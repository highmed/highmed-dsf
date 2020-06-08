package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;

public final class ReferencesHelperImpl<R extends Resource> implements ReferencesHelper<R>
{
	private final int index;
	private final User user;
	private final R resource;
	private final String serverBase;
	private final ReferenceExtractor referenceExtractor;
	private final ReferenceResolver referenceResolver;
	private final ResponseGenerator responseGenerator;

	public ReferencesHelperImpl(int index, User user, R resource, String serverBase,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver,
			ResponseGenerator responseGenerator)
	{
		this.index = index;
		this.user = user;
		this.resource = resource;
		this.serverBase = serverBase;
		this.referenceExtractor = referenceExtractor;
		this.referenceResolver = referenceResolver;
		this.responseGenerator = responseGenerator;
	}

	@Override
	public void resolveTemporaryAndConditionalReferences(Map<String, IdType> idTranslationTable, Connection connection)
			throws WebApplicationException
	{
		referenceExtractor.getReferences(resource).forEach(ref ->
		{
			Optional<OperationOutcome> outcome = resolveTemporaryOrConditionalReference(ref, idTranslationTable,
					connection);
			if (outcome.isPresent())
			{
				Response response = Response.status(Status.FORBIDDEN).entity(outcome.get()).build();
				throw new WebApplicationException(response);
			}
		});
	}

	private Optional<OperationOutcome> resolveTemporaryOrConditionalReference(ResourceReference reference,
			Map<String, IdType> idTranslationTable, Connection connection)
	{
		ReferenceType type = reference.getType(serverBase);
		switch (type)
		{
			case TEMPORARY:
				return resolveTemporaryReference(reference, idTranslationTable);
			case CONDITIONAL:
				return resolveConditionalReference(reference, connection);
			default:
				return Optional.empty();
		}
	}

	private Optional<OperationOutcome> resolveTemporaryReference(ResourceReference reference,
			Map<String, IdType> idTranslationTable)
	{
		IdType newId = idTranslationTable.get(reference.getReference().getReference());
		if (newId != null)
		{
			reference.getReference().setReferenceElement(newId);

			return Optional.empty();
		}
		else
			return Optional.of(responseGenerator.unknownReference(resource, reference, index));
	}

	private Optional<OperationOutcome> resolveConditionalReference(ResourceReference reference, Connection connection)
	{
		Optional<Resource> resolvedResource = referenceResolver.resolveReference(user, reference, connection);
		if (resolvedResource.isPresent())
		{
			Resource target = resolvedResource.get();
			reference.getReference().setReferenceElement(
					new IdType(target.getResourceType().name(), target.getIdElement().getIdPart()));

			return Optional.empty();
		}
		else
			return Optional.of(responseGenerator.referenceTargetNotFoundLocallyByCondition(index, resource, reference));
	}

	@Override
	public void resolveLogicalReferences(Connection connection) throws WebApplicationException
	{
		referenceExtractor.getReferences(resource).filter(ref -> ReferenceType.LOGICAL.equals(ref.getType(serverBase)))
				.forEach(ref ->
				{
					Optional<OperationOutcome> outcome = resolveLogicalReference(ref, connection);
					if (outcome.isPresent())
					{
						Response response = Response.status(Status.FORBIDDEN).entity(outcome.get()).build();
						throw new WebApplicationException(response);
					}
				});
	}

	private Optional<OperationOutcome> resolveLogicalReference(ResourceReference reference, Connection connection)
	{
		Optional<Resource> resolvedResource = referenceResolver.resolveReference(user, reference, connection);
		if (resolvedResource.isPresent())
		{
			Resource target = resolvedResource.get();
			reference.getReference().setReferenceElement(
					new IdType(target.getResourceType().name(), target.getIdElement().getIdPart()));

			return Optional.empty();
		}
		else
			return Optional
					.of(responseGenerator.referenceTargetNotFoundLocallyByIdentifier(index, resource, reference));
	}

	@Override
	public void checkReferences(Map<String, IdType> idTranslationTable, Connection connection)
			throws WebApplicationException
	{
		referenceExtractor.getReferences(resource).forEach(ref ->
		{
			Optional<OperationOutcome> outcome = checkReference(idTranslationTable, connection, ref);
			if (outcome.isPresent())
			{
				Response response = Response.status(Status.FORBIDDEN).entity(outcome.get()).build();
				throw new WebApplicationException(response);
			}
		});
	}

	private Optional<OperationOutcome> checkReference(Map<String, IdType> idTranslationTable, Connection connection,
			ResourceReference reference) throws WebApplicationException
	{
		ReferenceType type = reference.getType(serverBase);
		switch (type)
		{
			case LITERAL_INTERNAL:
				return referenceResolver.checkLiteralInternalReference(resource, reference, connection, index);
			case LITERAL_EXTERNAL:
				return referenceResolver.checkLiteralExternalReference(resource, reference, index);
			case LOGICAL:
				return referenceResolver.checkLogicalReference(user, resource, reference, connection, index);
			case UNKNOWN:
			default:
				return Optional.of(responseGenerator.unknownReference(resource, reference, index));
		}
	}
}
