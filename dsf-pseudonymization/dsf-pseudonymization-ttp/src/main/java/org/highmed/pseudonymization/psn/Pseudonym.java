package org.highmed.pseudonymization.psn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Pseudonym
{
	private final List<MedicId> medicIds = new ArrayList<>();

	@JsonCreator
	public Pseudonym(@JsonProperty("medicIds") Collection<? extends MedicId> medicIds)
	{
		if (medicIds != null)
			this.medicIds.addAll(medicIds);
	}

	public List<MedicId> getMedicIds()
	{
		return Collections.unmodifiableList(medicIds);
	}
}
