package org.highmed.dsf.fhir.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.highmed.dsf.bpe.ConstantsBase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Outputs
{
	private final List<Output> outputs = new ArrayList<>();

	public Outputs()
	{
	}

	@JsonCreator
	public Outputs(
			@JsonProperty("outputs")
					Collection<? extends Output> outputs)
	{
		if (outputs != null)
			this.outputs.addAll(outputs);
	}

	public List<Output> getOutputs()
	{
		return Collections.unmodifiableList(outputs);
	}
	
	public void add(Output output)
	{
		if (output != null)
			outputs.add(output);
	}

	public void add(String system, String code, String value)
	{
		add(system, code, value, null, null);
	}
	
	public void add(String system, String code, String value, String extensionUrl, String extensionValue)
	{
		Output output = new Output(system, code, value, extensionUrl, extensionValue);
		outputs.add(output);
	}

	public void addErrorOutput(String error)
	{
		add(ConstantsBase.CODESYSTEM_HIGHMED_BPMN, ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, error);
	}
}
