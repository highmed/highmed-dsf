package org.highmed.pseudonymization.domain.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.highmed.pseudonymization.domain.MdatContainer;
import org.hl7.fhir.r4.model.Resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FhirMdatContainer implements MdatContainer
{
	private final List<Resource> elements = new ArrayList<>();

	@JsonCreator
	public FhirMdatContainer(@JsonProperty("elements") Collection<? extends Resource> elements)
	{
		if (elements != null)
			this.elements.addAll(elements);
	}

	public List<Resource> getElements()
	{
		return Collections.unmodifiableList(elements);
	}
}
