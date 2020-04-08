package org.highmed.pseudonymization.domain.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.highmed.openehr.model.structure.RowElement;
import org.highmed.pseudonymization.domain.MdatContainer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenEhrMdatContainer implements MdatContainer
{
	private final List<RowElement> elements = new ArrayList<>();

	@JsonCreator
	public OpenEhrMdatContainer(@JsonProperty("elements") Collection<? extends RowElement> elements)
	{
		if (elements != null)
			this.elements.addAll(elements);
	}

	public List<RowElement> getElements()
	{
		return Collections.unmodifiableList(elements);
	}
}
