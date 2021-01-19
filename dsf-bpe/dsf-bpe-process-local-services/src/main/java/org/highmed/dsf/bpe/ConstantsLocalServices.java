package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_LOCAL_SERVICES_PROCESS_URI;
import static org.highmed.dsf.bpe.LocalServicesProcessPluginDefinition.VERSION;

public interface ConstantsLocalServices
{
	String PROFILE_HIGHMED_TASK_LOCAL_SERVICES = "http://highmed.org/fhir/StructureDefinition/task-local-services-integration";
	String PROFILE_HIGHMED_TASK_LOCAL_SERVICES_PROCESS_URI_AND_LATEST_VERSION =
			PROFILE_HIGHMED_TASK_LOCAL_SERVICES_PROCESS_URI + VERSION;
	String PROFILE_HIGHMED_TASK_LOCAL_SERVICES_MESSAGE_NAME = "localServicesIntegrationMessage";
}
