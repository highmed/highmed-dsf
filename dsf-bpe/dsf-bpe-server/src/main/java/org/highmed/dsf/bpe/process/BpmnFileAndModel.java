package org.highmed.dsf.bpe.process;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public final class BpmnFileAndModel
{
	private final String file;
	private final BpmnModelInstance model;
	private final List<Path> jars = new ArrayList<>();

	public BpmnFileAndModel(String file, BpmnModelInstance model, Collection<? extends Path> jars)
	{
		this.file = file;
		this.model = model;

		if (jars != null)
			this.jars.addAll(jars);
	}

	public String getFile()
	{
		return file;
	}

	public BpmnModelInstance getModel()
	{
		return model;
	}

	public List<Path> getJars()
	{
		return Collections.unmodifiableList(jars);
	}

	public ProcessKeyAndVersion getProcessKeyAndVersion()
	{
		return ProcessKeyAndVersion.fromModel(getModel());
	}
}