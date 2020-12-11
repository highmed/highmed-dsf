package org.highmed.dsf.bpe.variables;

import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.LOCAL_SERVICES_PROCESS_URI;

public interface ConstantsLocalServices
{
	String LOCAL_SERVICES_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-local-services-integration";
	String LOCAL_SERVICES_PROCESS_LATEST_VERSION = "0.4.0";
	String LOCAL_SERVICES_PROCESS_URI_AND_LATEST_VERSION =
			LOCAL_SERVICES_PROCESS_URI + LOCAL_SERVICES_PROCESS_LATEST_VERSION;

	String LOCAL_SERVICES_MESSAGE_NAME = "localServicesIntegrationMessage";
}
