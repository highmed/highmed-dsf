package org.highmed.dsf.bpe.process;

import java.util.Objects;

public class ProcessStateChangeOutcome
{
	private final ProcessKeyAndVersion processKeyAndVersion;
	private final ProcessState oldProcessState;
	private final ProcessState newProcessState;

	public ProcessStateChangeOutcome(ProcessKeyAndVersion processKeyAndVersion, ProcessState oldProcessState,
			ProcessState newProcessState)
	{
		this.processKeyAndVersion = Objects.requireNonNull(processKeyAndVersion, "processKeyAndVersion");
		this.oldProcessState = Objects.requireNonNull(oldProcessState, "oldProcessState");
		this.newProcessState = Objects.requireNonNull(newProcessState, "newProcessState");
	}

	public ProcessKeyAndVersion getProcessKeyAndVersion()
	{
		return processKeyAndVersion;
	}

	public ProcessState getOldProcessState()
	{
		return oldProcessState;
	}

	public ProcessState getNewProcessState()
	{
		return newProcessState;
	}

	@Override
	public String toString()
	{
		return getProcessKeyAndVersion().toString() + " " + getOldProcessState() + " -> " + getNewProcessState();
	}
}
