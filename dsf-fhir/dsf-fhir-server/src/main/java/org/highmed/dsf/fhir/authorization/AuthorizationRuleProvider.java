package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.ValueSet;

public interface AuthorizationRuleProvider
{
	AuthorizationRule<ActivityDefinition> getActivityDefinitionAuthorizationRule();

	AuthorizationRule<Binary> getBinaryAuthorizationRule();

	AuthorizationRule<Bundle> getBundleAuthorizationRule();

	AuthorizationRule<CodeSystem> getCodeSystemAuthorizationRule();

	AuthorizationRule<Endpoint> getEndpointAuthorizationRule();

	AuthorizationRule<Group> getGroupAuthorizationRule();

	AuthorizationRule<HealthcareService> getHealthcareServiceAuthorizationRule();

	AuthorizationRule<Library> getLibraryAuthorizationRule();

	AuthorizationRule<Location> getLocationAuthorizationRule();

	AuthorizationRule<Measure> getMeasureAuthorizationRule();

	AuthorizationRule<MeasureReport> getMeasureReportAuthorizationRule();

	AuthorizationRule<NamingSystem> getNamingSystemAuthorizationRule();

	AuthorizationRule<Organization> getOrganizationAuthorizationRule();

	AuthorizationRule<Patient> getPatientAuthorizationRule();

	AuthorizationRule<Practitioner> getPractitionerAuthorizationRule();

	AuthorizationRule<PractitionerRole> getPractitionerRoleAuthorizationRule();

	AuthorizationRule<Provenance> getProvenanceAuthorizationRule();

	AuthorizationRule<ResearchStudy> getResearchStudyAuthorizationRule();

	AuthorizationRule<StructureDefinition> getStructureDefinitionAuthorizationRule();

	AuthorizationRule<Subscription> getSubscriptionAuthorizationRule();

	AuthorizationRule<Task> getTaskAuthorizationRule();

	AuthorizationRule<ValueSet> getValueSetAuthorizationRule();

	Optional<AuthorizationRule<?>> getAuthorizationRule(Class<?> resourceClass);

	Optional<AuthorizationRule<?>> getAuthorizationRule(String resourceTypeName);
}
