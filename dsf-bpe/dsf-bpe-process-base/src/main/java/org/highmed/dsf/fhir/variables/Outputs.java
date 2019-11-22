package org.highmed.dsf.fhir.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.highmed.dsf.bpe.Constants;

public class Outputs
{
	private final List<Output> outputs = new ArrayList<>();

	public Outputs()
	{
	}

	public Outputs(Collection<? extends Output> outputs)
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
		outputs.add(output);
	}

	public void add(String system, String code, String value)
	{
		Output output = new Output(system, code, value);
		add(output);
	}

	public void addErrorOutput(String error)
	{
		add(Constants.CODESYSTEM_HIGHMED_BPMN, Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, error);
	}
}
