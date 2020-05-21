package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResolveReferencesHelper<R extends Resource>
{
	private static final Logger logger = LoggerFactory.getLogger(ResolveReferencesHelper.class);

	private final int index;
	private final User user;
	private final String serverBase;
	private final ReferenceExtractor referenceExtractor;
	private final ReferenceResolver referenceResolver;
	private final ResponseGenerator responseGenerator;

	public ResolveReferencesHelper(int index, User user, String serverBase, ReferenceExtractor referenceExtractor,
			ReferenceResolver referenceResolver, ResponseGenerator responseGenerator)
	{
		this.index = index;
		this.user = user;
		this.serverBase = serverBase;
		this.referenceExtractor = referenceExtractor;
		this.referenceResolver = referenceResolver;
		this.responseGenerator = responseGenerator;
	}

	public boolean resolveReferences(Map<String, IdType> idTranslationTable, Connection connection, R resource)
			throws WebApplicationException
	{
		boolean resourceNeedsUpdated = false;
		List<ResourceReference> references = referenceExtractor.getReferences(resource).collect(Collectors.toList());
		// Don't use stream.map(...).anyMatch(b -> b), anyMatch is a shortcut operation stopping after first match
		for (ResourceReference ref : references)
		{
			boolean needsUpdate = resolveReference(idTranslationTable, connection, resource, ref);
			if (needsUpdate)
				resourceNeedsUpdated = true;
		}
		return resourceNeedsUpdated;
	}

	public void resolveReferencesIgnoreAndLogExceptions(Map<String, IdType> idTranslationTable, Connection connection,
			R resource) throws WebApplicationException
	{
		List<ResourceReference> references = referenceExtractor.getReferences(resource).collect(Collectors.toList());
		// Don't use stream.map(...).anyMatch(b -> b), anyMatch is a shortcut operation stopping after first match
		for (ResourceReference ref : references)
		{
			try
			{
				resolveReference(idTranslationTable, connection, resource, ref);
			}
			catch (WebApplicationException e)
			{
				logger.warn("Error while resolving reference {}", e.getMessage());
			}
		}
	}

	private boolean resolveReference(Map<String, IdType> idTranslationTable, Connection connection, R resource,
			ResourceReference reference) throws WebApplicationException
	{
		ReferenceType type = reference.getType(serverBase);
		switch (type)
		{
			case TEMPORARY:
				return resolveTemporaryReference(reference, idTranslationTable, resource);
			case LITERAL_INTERNAL:
				return referenceResolver.resolveLiteralInternalReference(resource, index, reference, connection);
			case LITERAL_EXTERNAL:
				return referenceResolver.resolveLiteralExternalReference(resource, index, reference);
			case CONDITIONAL:
				return referenceResolver.resolveConditionalReference(user, resource, index, reference, connection);
			case LOGICAL:
				return referenceResolver.resolveLogicalReference(user, resource, index, reference, connection);
			case UNKNOWN:
			default:
				throw new WebApplicationException(responseGenerator.unknownReference(index, resource, reference));
		}
	}

	private boolean resolveTemporaryReference(ResourceReference resourceReference,
			Map<String, IdType> idTranslationTable, R resource)
	{
		IdType newId = idTranslationTable.get(resourceReference.getReference().getReference());
		if (newId == null)
			throw new WebApplicationException(responseGenerator.unknownReference(index, resource, resourceReference));
		else
			resourceReference.getReference().setReferenceElement(newId);

		return true; // throws exception if reference could not be resolved
	}
}
