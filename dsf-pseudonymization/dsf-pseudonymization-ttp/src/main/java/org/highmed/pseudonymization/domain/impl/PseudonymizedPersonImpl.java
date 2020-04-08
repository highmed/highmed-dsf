package org.highmed.pseudonymization.domain.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.highmed.pseudonymization.domain.MdatContainer;
import org.highmed.pseudonymization.domain.PseudonymizedPersonWithMdat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PseudonymizedPersonImpl implements PseudonymizedPersonWithMdat
{
	private final String pseudonym;
	private final List<MdatContainer> mdatContainers = new ArrayList<>();

	@JsonCreator
	public PseudonymizedPersonImpl(@JsonProperty("pseudonym") String pseudonym,
			@JsonProperty("mdatContainers") Collection<? extends MdatContainer> mdatContainers)
	{
		this.pseudonym = pseudonym;

		if (mdatContainers != null)
			this.mdatContainers.addAll(mdatContainers);
	}

	@Override
	public String getPseudonym()
	{
		return pseudonym;
	}

	@Override
	public List<MdatContainer> getMdatContainers()
	{
		return mdatContainers;
	}
}
