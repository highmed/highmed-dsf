package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.hl7.fhir.r4.model.Resource;

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

	ProvenanceAuthorizationRule getpProvenanceAuthorizationRule();

	ResearchStudyAuthorizationRule getResearchStudyAuthorizationRule();

	StructureDefinitionAuthorizationRule getStructureDefinitionAuthorizationRule();

	SubscriptionAuthorizationRule getSubscriptionAuthorizationRule();

	TaskAuthorizationRule getTaskAuthorizationRule();

	ValueSetAuthorizationRule getValueSetAuthorizationRule();

	<R extends Resource> Optional<? extends AuthorizationRule<R>> getAuthorizationRule(Class<R> resourceClass);

	Optional<AuthorizationRule<?>> getAuthorizationRule(String resourceTypeName);
}
