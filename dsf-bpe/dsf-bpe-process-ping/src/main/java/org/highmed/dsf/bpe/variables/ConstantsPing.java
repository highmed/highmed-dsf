package org.highmed.dsf.bpe.variables;

import static org.highmed.dsf.bpe.ConstantsBase.PROCESS_URI_BASE;

public interface ConstantsPing
{
	String START_PING_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-start-ping-process";
	String START_PING_MESSAGE_NAME = "startPingProcessMessage";

	String PING_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-ping";
	String PING_PROCESS_URI = PROCESS_URI_BASE + "ping/";
	String PING_PROCESS_LATEST_VERSION = "0.4.0";
	String PING_PROCESS_URI_AND_LATEST_VERSION = PING_PROCESS_URI + PING_PROCESS_LATEST_VERSION;
	String PING_MESSAGE_NAME = "pingMessage";

	String PONG_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-pong";
	String PONG_PROCESS_URI = PROCESS_URI_BASE + "pong/";
	String PONG_PROCESS_LATEST_VERSION = "0.4.0";
	String PONG_PROCESS_URI_AND_LATEST_VERSION = PONG_PROCESS_URI + PONG_PROCESS_LATEST_VERSION;
	String PONG_MESSAGE_NAME = "pongMessage";
}
