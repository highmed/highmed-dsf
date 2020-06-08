package org.highmed.dsf.fhir.service;

import java.util.Objects;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.InitializingBean;

public class ReferenceCleanerImpl implements ReferenceCleaner, InitializingBean
{
	private ReferenceExtractor referenceExtractor;

	public ReferenceCleanerImpl(ReferenceExtractor referenceExtractor)
	{
		this.referenceExtractor = referenceExtractor;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(referenceExtractor, "referenceExtractor");
	}

	@Override
	public <R extends Resource> R cleanupReferences(R resource)
	{
		if (resource == null)
			return null;

		Stream<ResourceReference> references = referenceExtractor.getReferences(resource);
		references.forEach(this::cleanupReference);

		return resource;
	}

	private void cleanupReference(ResourceReference resourceReference)
	{
		Reference ref = resourceReference.getReference();
		if (ref.hasIdentifier() && ref.hasReference())
			ref.setReferenceElement((IdType) null);
	}
}
