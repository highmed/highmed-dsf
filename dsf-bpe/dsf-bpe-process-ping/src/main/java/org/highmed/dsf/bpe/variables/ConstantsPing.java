package org.highmed.dsf.bpe.variables;

public interface ConstantsPing
{
	String START_PING_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-start-ping-process";
	String START_PING_PROCESS_URI = "http://highmed.org/bpe/Process/ping";
	String START_PING_PROCESS_LATEST_VERSION = "0.4.0";
	String START_PING_PROCESS_URI_AND_LATEST_VERSION =
			START_PING_PROCESS_URI + "/" + START_PING_PROCESS_LATEST_VERSION;

	String START_PING_MESSAGE_NAME = "startPingProcessMessage";
}
