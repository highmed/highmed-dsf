package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsFeasibility.LOCAL_SERVICES_PROCESS_URI;
import static org.highmed.dsf.bpe.LocalServicesProcessPluginDefinition.VERSION;

public interface ConstantsLocalServices
{
	String LOCAL_SERVICES_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-local-services-integration";
	String LOCAL_SERVICES_PROCESS_URI_AND_LATEST_VERSION = LOCAL_SERVICES_PROCESS_URI + VERSION;

	String LOCAL_SERVICES_MESSAGE_NAME = "localServicesIntegrationMessage";
}
