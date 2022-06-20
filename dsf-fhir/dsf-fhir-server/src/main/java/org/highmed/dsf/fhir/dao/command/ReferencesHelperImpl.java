package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Type;

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
	public void resolveTemporaryAndConditionalReferencesOrLiteralInternalRelatedArtifactOrAttachmentUrls(
			Map<String, IdType> idTranslationTable, Connection connection) throws WebApplicationException
	{
		referenceExtractor.getReferences(resource)
				.filter(ref -> referenceResolver.referenceCanBeResolved(ref, connection)).forEach(ref ->
				{
					Optional<OperationOutcome> outcome = resolveTemporaryOrConditionalReferenceOrLiteralInternalRelatedArtifactOrAttachmentUrl(
							ref, idTranslationTable, connection);
					if (outcome.isPresent())
					{
						Response response = Response.status(Status.FORBIDDEN).entity(outcome.get()).build();
						throw new WebApplicationException(response);
					}
				});
	}

	private Optional<OperationOutcome> resolveTemporaryOrConditionalReferenceOrLiteralInternalRelatedArtifactOrAttachmentUrl(
			ResourceReference reference, Map<String, IdType> idTranslationTable, Connection connection)
	{
		ReferenceType type = reference.getType(serverBase);
		switch (type)
		{
			case TEMPORARY:
				return resolveTemporary(reference, idTranslationTable, reference.getReference()::getReference,
						reference.getReference()::setReferenceElement);

			case RELATED_ARTEFACT_TEMPORARY_URL:
				return resolveTemporary(reference, idTranslationTable, reference.getRelatedArtifact()::getUrl,
						newIdToAbsoluteUrl(reference.getRelatedArtifact()::setUrl));

			case ATTACHMENT_TEMPORARY_URL:
				return resolveTemporary(reference, idTranslationTable, reference.getAttachment()::getUrl,
						newIdToAbsoluteUrl(reference.getAttachment()::setUrl));

			case CONDITIONAL:
				return resolveConditional(reference, connection, target -> reference.getReference().setReferenceElement(
						new IdType(target.getResourceType().name(), target.getIdElement().getIdPart())));

			case RELATED_ARTEFACT_CONDITIONAL_URL:
				return resolveConditional(reference, connection,
						targetToAbsoluteUrl(reference.getRelatedArtifact()::setUrl));

			case ATTACHMENT_CONDITIONAL_URL:
				return resolveConditional(reference, connection,
						targetToAbsoluteUrl(reference.getAttachment()::setUrl));

			case RELATED_ARTEFACT_LITERAL_INTERNAL_URL:
				return resolveLiteralInternalUrl(reference::getRelatedArtifact, RelatedArtifact::getUrl,
						RelatedArtifact::setUrl);

			case ATTACHMENT_LITERAL_INTERNAL_URL:
				return resolveLiteralInternalUrl(reference::getAttachment, Attachment::getUrl, Attachment::setUrl);

			default:
				return Optional.empty();
		}
	}

	private Consumer<IdType> newIdToAbsoluteUrl(Consumer<String> absoluteUrlConsumer)
	{
		return newId ->
		{
			String absoluteUrl = newId.withServerBase(serverBase, newId.getResourceType()).getValue();
			absoluteUrlConsumer.accept(absoluteUrl);
		};
	}

	private Consumer<Resource> targetToAbsoluteUrl(Consumer<String> absoluteUrlConsumer)
	{
		return target ->
		{
			IdType newId = new IdType(target.getResourceType().name(), target.getIdElement().getIdPart());
			String absoluteUrl = newId.withServerBase(serverBase, newId.getResourceType()).getValue();
			absoluteUrlConsumer.accept(absoluteUrl);
		};
	}

	private Optional<OperationOutcome> resolveTemporary(ResourceReference reference,
			Map<String, IdType> idTranslationTable, Supplier<String> temporaryIdSupplier,
			Consumer<IdType> newIdConsumer)
	{
		IdType newId = idTranslationTable.get(temporaryIdSupplier.get());
		if (newId != null)
		{
			newIdConsumer.accept(newId);
			return Optional.empty();
		}
		else
			return Optional.of(responseGenerator.unknownReference(resource, reference, index));
	}

	private Optional<OperationOutcome> resolveConditional(ResourceReference reference, Connection connection,
			Consumer<Resource> targetConsumer)
	{
		Optional<Resource> resolvedResource = referenceResolver.resolveReference(user, reference, connection);
		if (resolvedResource.isPresent())
		{
			Resource target = resolvedResource.get();
			targetConsumer.accept(target);

			return Optional.empty();
		}
		else
			return Optional.of(responseGenerator.referenceTargetNotFoundLocallyByCondition(index, resource, reference));
	}

	private <T extends Type> Optional<OperationOutcome> resolveLiteralInternalUrl(Supplier<T> element,
			Function<T, String> oldUrlValue, BiConsumer<T, String> newAbsoluteUrlConsumer)
	{
		T type = element.get();
		if (type != null)
		{
			IdType newId = new IdType(oldUrlValue.apply(type));
			String absoluteUrl = newId.withServerBase(serverBase, newId.getResourceType()).getValue();
			newAbsoluteUrlConsumer.accept(type, absoluteUrl);

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
	public void checkReferences(Map<String, IdType> idTranslationTable, Connection connection,
			Predicate<ResourceReference> checkReference) throws WebApplicationException
	{
		referenceExtractor.getReferences(resource).filter(checkReference)
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
			case ATTACHMENT_LITERAL_INTERNAL_URL:
				return referenceResolver.checkLiteralInternalReference(resource, reference, connection, index);
			case LITERAL_EXTERNAL:
			case RELATED_ARTEFACT_LITERAL_EXTERNAL_URL:
			case ATTACHMENT_LITERAL_EXTERNAL_URL:
				return referenceResolver.checkLiteralExternalReference(resource, reference, index);
			case LOGICAL:
				return referenceResolver.checkLogicalReference(user, resource, reference, connection, index);
			// unknown URLs to non FHIR servers in related artifacts must not be checked
			case RELATED_ARTEFACT_UNKNOWN_URL:
			case ATTACHMENT_UNKNOWN_URL:
				return Optional.empty();
			case UNKNOWN:
			default:
				return Optional.of(responseGenerator.unknownReference(resource, reference, index));
		}
	}
}
