package org.highmed.dsf.fhir.service;

import java.util.Objects;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceCleanerImpl implements ReferenceCleaner
{
	private static final Logger logger = LoggerFactory.getLogger(ReferenceCleanerImpl.class);

	private ReferenceExtractor referenceExtractor;

	public ReferenceCleanerImpl(ReferenceExtractor referenceExtractor)
	{
		this.referenceExtractor = Objects.requireNonNull(referenceExtractor, "referenceExtractor");
	}

	@Override
	public <R extends Resource> R cleanLiteralReferences(R resource)
	{
		if (resource == null)
			return null;

		Stream<ResourceReference> references = referenceExtractor.getReferences(resource);
		references.forEach(this::cleanupReference);

		return resource;
	}

	private void cleanupReference(ResourceReference resourceReference)
	{
		if (resourceReference.hasReference())
		{
			Reference ref = resourceReference.getReference();
			if (ref.hasIdentifier() && ref.hasReference())
				ref.setReferenceElement((IdType) null);
		}
	}

	@Override
	public <R extends Resource> R cleanReferenceResourcesIfBundle(R resource)
	{
		if (resource == null)
			return null;

		if (resource instanceof Bundle)
		{
			Bundle bundle = (Bundle) resource;
			bundle.getEntry().stream().map(e -> e.getResource()).forEach(this::fixBundleEntry);
		}

		return resource;
	}

	private void fixBundleEntry(Resource resource)
	{
		if (resource instanceof Bundle)
		{
			cleanReferenceResourcesIfBundle(resource);
		}
		else
		{
			Stream<ResourceReference> references = referenceExtractor.getReferences(resource);

			references.filter(ResourceReference::hasReference).forEach(r -> r.getReference().setResource(null));

			if (resource instanceof DomainResource && ((DomainResource) resource).hasContained())
			{
				logger.warn("{} has contained resources, removing resources", resource.getClass().getName());
				((DomainResource) resource).setContained(null);
			}
		}
	}
}
