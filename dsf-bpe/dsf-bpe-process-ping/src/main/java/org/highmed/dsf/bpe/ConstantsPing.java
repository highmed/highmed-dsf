package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsBase.PROCESS_URI_BASE;
import static org.highmed.dsf.bpe.PingProcessPluginDefinition.VERSION;

public interface ConstantsPing
{
	String START_PING_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-start-ping-process";
	String START_PING_MESSAGE_NAME = "startPingProcessMessage";

	String PING_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-ping";
	String PING_PROCESS_URI = PROCESS_URI_BASE + "ping/";
	String PING_PROCESS_URI_AND_LATEST_VERSION = PING_PROCESS_URI + VERSION;
	String PING_MESSAGE_NAME = "pingMessage";

	String PONG_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-pong";
	String PONG_PROCESS_URI = PROCESS_URI_BASE + "pong/";
	String PONG_PROCESS_URI_AND_LATEST_VERSION = PONG_PROCESS_URI + VERSION;
	String PONG_MESSAGE_NAME = "pongMessage";
}
