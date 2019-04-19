package org.highmed.fhir.dao.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Reference;

public class ResourceReference
{
	private final Reference reference;
	private final List<Class<? extends DomainResource>> referenceTypes = new ArrayList<>();

	public ResourceReference(Reference reference, List<Class<? extends DomainResource>> referenceTypes)
	{
		this.reference = reference;

		if (referenceTypes != null)
			this.referenceTypes.addAll(referenceTypes);
	}

	public Reference getReference()
	{
		return reference;
	}

	public List<Class<? extends DomainResource>> getReferenceTypes()
	{
		return Collections.unmodifiableList(referenceTypes);
	}
}