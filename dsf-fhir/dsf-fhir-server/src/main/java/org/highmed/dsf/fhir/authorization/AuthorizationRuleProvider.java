package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

public interface AuthorizationRuleProvider
{
	ActivityDefinitionAuthorizationRule getActivityDefinitionAuthorizationRule();

	BinaryAuthorizationRule getBinaryAuthorizationRule();

	BundleAuthorizationRule getBundleAuthorizationRule();

	CodeSystemAuthorizationRule getCodeSystemAuthorizationRule();

	EndpointAuthorizationRule getEndpointAuthorizationRule();

	GroupAuthorizationRule getGroupAuthorizationRule();

	HealthcareServiceAuthorizationRule getHealthcareServiceAuthorizationRule();

	LocationAuthorizationRule getLocationAuthorizationRule();

	NamingSystemAuthorizationRule getNamingSystemAuthorizationRule();

	OrganizationAuthorizationRule getOrganizationAuthorizationRule();

	PatientAuthorizationRule getPatientAuthorizationRule();

	PractitionerAuthorizationRule getPractitionerAuthorizationRule();

	PractitionerRoleAuthorizationRule getPractitionerRoleAuthorizationRule();

	ProvenanceAuthorizationRule getProvenanceAuthorizationRule();

	ResearchStudyAuthorizationRule getResearchStudyAuthorizationRule();

	StructureDefinitionAuthorizationRule getStructureDefinitionAuthorizationRule();

	SubscriptionAuthorizationRule getSubscriptionAuthorizationRule();

	TaskAuthorizationRule getTaskAuthorizationRule();

	ValueSetAuthorizationRule getValueSetAuthorizationRule();

	Optional<AuthorizationRule<?>> getAuthorizationRule(Class<?> resourceClass);

	Optional<AuthorizationRule<?>> getAuthorizationRule(String resourceTypeName);
}
