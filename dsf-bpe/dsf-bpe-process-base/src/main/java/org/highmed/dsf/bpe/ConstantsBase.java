package org.highmed.dsf.bpe;

import org.hl7.fhir.r4.model.CodeType;

public interface ConstantsBase
{
	String BPMN_EXECUTION_VARIABLE_INSTANTIATES_URI = "instantiatesUri";
	String BPMN_EXECUTION_VARIABLE_MESSAGE_NAME = "messageName";
	String BPMN_EXECUTION_VARIABLE_PROFILE = "profile";
	String BPMN_EXECUTION_VARIABLE_TARGET = "target";
	String BPMN_EXECUTION_VARIABLE_TARGETS = "targets";
	String BPMN_EXECUTION_VARIABLE_TASK = "task";
	String BPMN_EXECUTION_VARIABLE_LEADING_TASK = "leadingTask";
	String BPMN_EXECUTION_VARIABLE_BUNDLE_ID = "bundleId";
	String BPMN_EXECUTION_VARIABLE_QUERY_PARAMETERS = "queryParameters";
	String BPMN_EXECUTION_VARIABLE_TTP_IDENTIFIER = "ttpIdentifier";
	String BPMN_EXECUTION_VARIABLE_LEADING_MEDIC_IDENTIFIER = "leadingMedicIdentifier";
	String BPMN_EXECUTION_VARIABLE_ALTERNATIVE_BUSINESS_KEY = "alternativeBusinessKey";
	String BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_RESPONSE_ID = "questionnaireResponseId";
	String BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_RESPONSE_COMPLETED = "questionnaireResponseCompleted";

	/**
	 * Used to distinguish if I am at the moment in a process called by another process by a CallActivity or not
	 */
	String BPMN_EXECUTION_VARIABLE_IN_CALLED_PROCESS = "inCalledProcess";

	String PROCESS_HIGHMED_URI_BASE = "http://highmed.org/bpe/Process/";

	String EXTENSION_HIGHMED_PARTICIPATING_MEDIC = "http://highmed.org/fhir/StructureDefinition/extension-participating-medic";
	String EXTENSION_HIGHMED_PARTICIPATING_TTP = "http://highmed.org/fhir/StructureDefinition/extension-participating-ttp";
	String EXTENSION_HIGHMED_GROUP_ID = "http://highmed.org/fhir/StructureDefinition/extension-group-id";
	String EXTENSION_HIGHMED_QUERY = "http://highmed.org/fhir/StructureDefinition/extension-query";

	String PROFILE_HIGHMED_GROUP = "http://highmed.org/fhir/StructureDefinition/group";
	String PROFILE_HIGHEMD_RESEARCH_STUDY = "http://highmed.org/fhir/StructureDefinition/research-study";

	String CODESYSTEM_HIGHMED_BPMN = "http://highmed.org/fhir/CodeSystem/bpmn-message";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME = "message-name";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY = "business-key";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY = "correlation-key";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR = "error";

	String CODESYSTEM_HIGHMED_BPMN_USER_TASK = "http://highmed.org/fhir/CodeSystem/bpmn-user-task";
	String CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY = "business-key";
	String CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID = "user-task-id";

	/**
	 * @deprecated as of release 0.6.0, use {@link #CODESYSTEM_HIGHMED_ORGANIZATION_ROLE} instead
	 */
	@Deprecated
	String CODESYSTEM_HIGHMED_ORGANIZATION_TYPE = "http://highmed.org/fhir/CodeSystem/organization-type";
	/**
	 * @deprecated as of release 0.6.0, use {@link #CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_TTP} instead
	 */
	@Deprecated
	String CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_TTP = "TTP";
	/**
	 * @deprecated as of release 0.6.0, use {@link #CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_MEDIC} instead
	 */
	@Deprecated
	String CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_MEDIC = "MeDIC";
	/**
	 * @deprecated as of release 0.6.0, use {@link #CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_DTS} instead
	 */
	@Deprecated
	String CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_DTS = "DTS";
	/**
	 * @deprecated as of release 0.6.0, use {@link #CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_COS} instead
	 */
	@Deprecated
	String CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_COS = "COS";
	/**
	 * @deprecated as of release 0.6.0, use {@link #CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_CRR} instead
	 */
	@Deprecated
	String CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_CRR = "CRR";
	/**
	 * @deprecated as of release 0.6.0, use {@link #CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_HRP} instead
	 */
	@Deprecated
	String CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_HRP = "HRP";

	String CODESYSTEM_HIGHMED_ORGANIZATION_ROLE = "http://highmed.org/fhir/CodeSystem/organization-role";
	String CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_TTP = "TTP";
	String CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_MEDIC = "MeDIC";
	String CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_DTS = "DTS";
	String CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_COS = "COS";
	String CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_CRR = "CRR";
	String CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_HRP = "HRP";

	String CODESYSTEM_HIGHMED_QUERY_TYPE = "http://highmed.org/fhir/CodeSystem/query-type";
	String CODESYSTEM_HIGMED_QUERY_TYPE_VALUE_AQL = "application/x-aql-query";

	String NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER = "http://highmed.org/sid/organization-identifier";
	String NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER = "http://highmed.org/sid/endpoint-identifier";
	String NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER = "http://highmed.org/sid/research-study-identifier";

	CodeType CODE_TYPE_AQL_QUERY = new CodeType(CODESYSTEM_HIGMED_QUERY_TYPE_VALUE_AQL)
			.setSystem(CODESYSTEM_HIGHMED_QUERY_TYPE);
	String OPENEHR_MIMETYPE_JSON = "application/json";

	String NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_HIGHMED_CONSORTIUM = "highmed.org";
	String NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_NUM_CODEX_CONSORTIUM = "netzwerk-universitaetsmedizin.de";
	String NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_MEDICAL_INFORMATICS_INITIATIVE_CONSORTIUM = "medizininformatik-initiative.de";
}
