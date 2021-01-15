package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsBase.PROCESS_HIGHMED_URI_BASE;
import static org.highmed.dsf.bpe.PingProcessPluginDefinition.VERSION;

public interface ConstantsPing
{
	String PROFILE_HIGHMED_TASK_START_PING = "http://highmed.org/fhir/StructureDefinition/task-start-ping-process";
	String PROFILE_HIGHMED_TASK_START_PING_MESSAGE_NAME = "startPingProcessMessage";

	String PROFILE_HIGHMED_TASK_PING = "http://highmed.org/fhir/StructureDefinition/task-ping";
	String PROFILE_HIGHMED_TASK_PING_PROCESS_URI = PROCESS_HIGHMED_URI_BASE + "ping/";
	String PROFILE_HIGHMED_TASK_PING_PROCESS_URI_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_PING_PROCESS_URI + VERSION;
	String PROFILE_HIGHMED_TASK_PING_MESSAGE_NAME = "pingMessage";

	String PROFILE_HIGHMED_TASK_PONG_TASK = "http://highmed.org/fhir/StructureDefinition/task-pong";
	String PROFILE_HIGHMED_TASK_PONG_PROCESS_URI = PROCESS_HIGHMED_URI_BASE + "pong/";
	String PROFILE_HIGHMED_TASK_PONG_PROCESS_URI_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_PONG_PROCESS_URI + VERSION;
	String PROFILE_HIGHMED_TASK_PONG_MESSAGE_NAME = "pongMessage";
}
