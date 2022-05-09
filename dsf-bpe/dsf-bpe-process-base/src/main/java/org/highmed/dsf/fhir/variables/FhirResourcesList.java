package org.highmed.dsf.fhir.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.Resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FhirResourcesList
{
	private final List<Resource> resources = new ArrayList<>();

	@JsonCreator
	public FhirResourcesList(@JsonProperty("resources") Collection<? extends Resource> resources)
	{
		if (resources != null)
			this.resources.addAll(resources);
	}

	public FhirResourcesList(Resource... resources)
	{
		this(Arrays.asList(resources));
	}

	@JsonProperty("resources")
	public List<Resource> getResources()
	{
		return Collections.unmodifiableList(resources);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public <R extends Resource> List<R> getResourcesAndCast()
	{
		return (List<R>) getResources();
	}
}
