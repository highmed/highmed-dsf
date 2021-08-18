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
	public void resolveTemporaryAndConditionalReferencesOrLiteralInternalRelatedArtifactUrls(
			Map<String, IdType> idTranslationTable, Connection connection) throws WebApplicationException
	{
		referenceExtractor.getReferences(resource)
				.filter(ref -> referenceResolver.referenceCanBeResolved(ref, connection)).forEach(ref ->
				{
					Optional<OperationOutcome> outcome = resolveTemporaryOrConditionalReferenceOrLiteralInternalRelatedArtifactUrl(
							ref, idTranslationTable, connection);
					if (outcome.isPresent())
					{
						Response response = Response.status(Status.FORBIDDEN).entity(outcome.get()).build();
						throw new WebApplicationException(response);
					}
				});
	}

	private Optional<OperationOutcome> resolveTemporaryOrConditionalReferenceOrLiteralInternalRelatedArtifactUrl(
			ResourceReference reference, Map<String, IdType> idTranslationTable, Connection connection)
	{
		ReferenceType type = reference.getType(serverBase);
		switch (type)
		{
			case TEMPORARY:
				return resolveTemporaryReference(reference, idTranslationTable);
			case RELATED_ARTEFACT_TEMPORARY_URL:
				return resolveTemporaryRelatedArtifactUrl(reference, idTranslationTable);
			case CONDITIONAL:
				return resolveConditionalReference(reference, connection);
			case RELATED_ARTEFACT_CONDITIONAL_URL:
				return resolveConditionalRelatedArtifactUrl(reference, connection);
			case RELATED_ARTEFACT_LITERAL_INTERNAL_URL:
				return resolveLiteralInternalRelatedArtifactUrl(reference);
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

	private Optional<OperationOutcome> resolveTemporaryRelatedArtifactUrl(ResourceReference reference,
			Map<String, IdType> idTranslationTable)
	{
		IdType newId = idTranslationTable.get(reference.getRelatedArtifact().getUrl());
		if (newId != null)
		{
			String absoluteUrl = newId.withServerBase(serverBase, newId.getResourceType()).getValue();
			reference.getRelatedArtifact().setUrl(absoluteUrl);

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

	private Optional<OperationOutcome> resolveConditionalRelatedArtifactUrl(ResourceReference reference,
			Connection connection)
	{
		Optional<Resource> resolvedResource = referenceResolver.resolveReference(user, reference, connection);
		if (resolvedResource.isPresent())
		{
			Resource target = resolvedResource.get();
			IdType newId = new IdType(target.getResourceType().name(), target.getIdElement().getIdPart());
			String absoluteUrl = newId.withServerBase(serverBase, newId.getResourceType()).getValue();
			reference.getRelatedArtifact().setUrl(absoluteUrl);

			return Optional.empty();
		}
		else
			return Optional.of(responseGenerator.referenceTargetNotFoundLocallyByCondition(index, resource, reference));
	}

	private Optional<OperationOutcome> resolveLiteralInternalRelatedArtifactUrl(ResourceReference reference)
	{
		if (reference.hasRelatedArtifact())
		{
			IdType newId = new IdType(reference.getValue());
			String absoluteUrl = newId.withServerBase(serverBase, newId.getResourceType()).getValue();
			reference.getRelatedArtifact().setUrl(absoluteUrl);

			return Optional.empty();
		}

		return Optional.empty();
	}

	@Override
	public void resolveLogicalReferences(Connection connection) throws WebApplicationException
	{
		referenceExtractor.getReferences(resource).filter(ref -> ReferenceType.LOGICAL.equals(ref.getType(serverBase)))
				.filter(ref -> referenceResolver.referenceCanBeResolved(ref, connection)).forEach(ref ->
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
		referenceExtractor.getReferences(resource)
				.filter(ref -> referenceResolver.referenceCanBeChecked(ref, connection)).forEach(ref ->
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
			case RELATED_ARTEFACT_LITERAL_INTERNAL_URL:
				return referenceResolver.checkLiteralInternalReference(resource, reference, connection, index);
			case LITERAL_EXTERNAL:
			case RELATED_ARTEFACT_LITERAL_EXTERNAL_URL:
				return referenceResolver.checkLiteralExternalReference(resource, reference, index);
			case LOGICAL:
				return referenceResolver.checkLogicalReference(user, resource, reference, connection, index);
			// unknown URLs to non FHIR servers in related artifacts must not be checked
			case RELATED_ARTEFACT_UNKNOWN_URL:
				return Optional.empty();
			case UNKNOWN:
			default:
				return Optional.of(responseGenerator.unknownReference(resource, reference, index));
		}
	}
}
