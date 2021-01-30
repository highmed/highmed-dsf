package org.highmed.dsf.fhir.authorization;

import java.util.HashMap;
import java.util.Map;
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
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class AuthorizationRuleProviderImpl implements AuthorizationRuleProvider
{
	private final AuthorizationRule<ActivityDefinition> activityDefinitionAuthorizationRule;
	private final AuthorizationRule<Binary> binaryAuthorizationRule;
	private final AuthorizationRule<Bundle> bundleAuthorizationRule;
	private final AuthorizationRule<CodeSystem> codeSystemAuthorizationRule;
	private final AuthorizationRule<Endpoint> endpointAuthorizationRule;
	private final AuthorizationRule<Group> groupAuthorizationRule;
	private final AuthorizationRule<HealthcareService> healthcareServiceAuthorizationRule;
	private final AuthorizationRule<Library> libraryAuthorizationRule;
	private final AuthorizationRule<Location> locationAuthorizationRule;
	private final AuthorizationRule<Measure> measureAuthorizationRule;
	private final AuthorizationRule<MeasureReport> measureReportAuthorizationRule;
	private final AuthorizationRule<NamingSystem> namingSystemAuthorizationRule;
	private final AuthorizationRule<Organization> organizationAuthorizationRule;
	private final AuthorizationRule<Patient> patientAuthorizationRule;
	private final AuthorizationRule<Practitioner> practitionerAuthorizationRule;
	private final AuthorizationRule<PractitionerRole> practitionerRoleAuthorizationRule;
	private final AuthorizationRule<Provenance> provenanceAuthorizationRule;
	private final AuthorizationRule<ResearchStudy> researchStudyAuthorizationRule;
	private final AuthorizationRule<StructureDefinition> structureDefinitionAuthorizationRule;
	private final AuthorizationRule<Subscription> subscriptionAuthorizationRule;
	private final AuthorizationRule<Task> taskAuthorizationRule;
	private final AuthorizationRule<ValueSet> valueSetAuthorizationRule;

	private final Map<Class<? extends Resource>, AuthorizationRule<?>> authorizationRulesByResourecClass = new HashMap<>();
	private final Map<String, AuthorizationRule<?>> authorizationRulesByResourceTypeName = new HashMap<>();

	public AuthorizationRuleProviderImpl(AuthorizationRule<ActivityDefinition> activityDefinitionAuthorizationRule,
			AuthorizationRule<Binary> binaryAuthorizationRule, AuthorizationRule<Bundle> bundleAuthorizationRule,
			AuthorizationRule<CodeSystem> codeSystemAuthorizationRule,
			AuthorizationRule<Endpoint> endpointAuthorizationRule, AuthorizationRule<Group> groupAuthorizationRule,
			AuthorizationRule<HealthcareService> healthcareServiceAuthorizationRule,
			AuthorizationRule<Library> libraryAuthorizationRule, AuthorizationRule<Location> locationAuthorizationRule,
			AuthorizationRule<Measure> measureAuthorizationRule,
			AuthorizationRule<MeasureReport> measureReportAuthorizationRule,
			AuthorizationRule<NamingSystem> namingSystemAuthorizationRule,
			AuthorizationRule<Organization> organizationAuthorizationRule,
			AuthorizationRule<Patient> patientAuthorizationRule,
			AuthorizationRule<Practitioner> practitionerAuthorizationRule,
			AuthorizationRule<PractitionerRole> practitionerRoleAuthorizationRule,
			AuthorizationRule<Provenance> provenanceAuthorizationRule,
			AuthorizationRule<ResearchStudy> researchStudyAuthorizationRule,
			AuthorizationRule<StructureDefinition> structureDefinitionAuthorizationRule,
			AuthorizationRule<Subscription> subscriptionAuthorizationRule,
			AuthorizationRule<Task> taskAuthorizationRule, AuthorizationRule<ValueSet> valueSetAuthorizationRule)
	{
		this.activityDefinitionAuthorizationRule = activityDefinitionAuthorizationRule;
		this.binaryAuthorizationRule = binaryAuthorizationRule;
		this.bundleAuthorizationRule = bundleAuthorizationRule;
		this.codeSystemAuthorizationRule = codeSystemAuthorizationRule;
		this.endpointAuthorizationRule = endpointAuthorizationRule;
		this.groupAuthorizationRule = groupAuthorizationRule;
		this.healthcareServiceAuthorizationRule = healthcareServiceAuthorizationRule;
		this.libraryAuthorizationRule = libraryAuthorizationRule;
		this.locationAuthorizationRule = locationAuthorizationRule;
		this.measureAuthorizationRule = measureAuthorizationRule;
		this.measureReportAuthorizationRule = measureReportAuthorizationRule;
		this.namingSystemAuthorizationRule = namingSystemAuthorizationRule;
		this.organizationAuthorizationRule = organizationAuthorizationRule;
		this.patientAuthorizationRule = patientAuthorizationRule;
		this.practitionerAuthorizationRule = practitionerAuthorizationRule;
		this.practitionerRoleAuthorizationRule = practitionerRoleAuthorizationRule;
		this.provenanceAuthorizationRule = provenanceAuthorizationRule;
		this.researchStudyAuthorizationRule = researchStudyAuthorizationRule;
		this.structureDefinitionAuthorizationRule = structureDefinitionAuthorizationRule;
		this.subscriptionAuthorizationRule = subscriptionAuthorizationRule;
		this.taskAuthorizationRule = taskAuthorizationRule;
		this.valueSetAuthorizationRule = valueSetAuthorizationRule;

		authorizationRulesByResourecClass.put(ActivityDefinition.class, activityDefinitionAuthorizationRule);
		authorizationRulesByResourecClass.put(Binary.class, binaryAuthorizationRule);
		authorizationRulesByResourecClass.put(Bundle.class, bundleAuthorizationRule);
		authorizationRulesByResourecClass.put(CodeSystem.class, codeSystemAuthorizationRule);
		authorizationRulesByResourecClass.put(Endpoint.class, endpointAuthorizationRule);
		authorizationRulesByResourecClass.put(Group.class, groupAuthorizationRule);
		authorizationRulesByResourecClass.put(HealthcareService.class, healthcareServiceAuthorizationRule);
		authorizationRulesByResourecClass.put(Library.class, libraryAuthorizationRule);
		authorizationRulesByResourecClass.put(Location.class, locationAuthorizationRule);
		authorizationRulesByResourecClass.put(Measure.class, measureAuthorizationRule);
		authorizationRulesByResourecClass.put(MeasureReport.class, measureReportAuthorizationRule);
		authorizationRulesByResourecClass.put(NamingSystem.class, namingSystemAuthorizationRule);
		authorizationRulesByResourecClass.put(Organization.class, organizationAuthorizationRule);
		authorizationRulesByResourecClass.put(Patient.class, patientAuthorizationRule);
		authorizationRulesByResourecClass.put(Practitioner.class, practitionerAuthorizationRule);
		authorizationRulesByResourecClass.put(PractitionerRole.class, practitionerRoleAuthorizationRule);
		authorizationRulesByResourecClass.put(Provenance.class, provenanceAuthorizationRule);
		authorizationRulesByResourecClass.put(ResearchStudy.class, researchStudyAuthorizationRule);
		authorizationRulesByResourecClass.put(StructureDefinition.class, structureDefinitionAuthorizationRule);
		authorizationRulesByResourecClass.put(Subscription.class, subscriptionAuthorizationRule);
		authorizationRulesByResourecClass.put(Task.class, taskAuthorizationRule);
		authorizationRulesByResourecClass.put(ValueSet.class, valueSetAuthorizationRule);

		authorizationRulesByResourecClass.forEach(
				(k, v) -> authorizationRulesByResourceTypeName.put(k.getAnnotation(ResourceDef.class).name(), v));
	}

	@Override
	public AuthorizationRule<ActivityDefinition> getActivityDefinitionAuthorizationRule()
	{
		return activityDefinitionAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Binary> getBinaryAuthorizationRule()
	{
		return binaryAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Bundle> getBundleAuthorizationRule()
	{
		return bundleAuthorizationRule;
	}

	@Override
	public AuthorizationRule<CodeSystem> getCodeSystemAuthorizationRule()
	{
		return codeSystemAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Endpoint> getEndpointAuthorizationRule()
	{
		return endpointAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Group> getGroupAuthorizationRule()
	{
		return groupAuthorizationRule;
	}

	@Override
	public AuthorizationRule<HealthcareService> getHealthcareServiceAuthorizationRule()
	{
		return healthcareServiceAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Library> getLibraryAuthorizationRule()
	{
		return libraryAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Location> getLocationAuthorizationRule()
	{
		return locationAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Measure> getMeasureAuthorizationRule()
	{
		return measureAuthorizationRule;
	}

	@Override
	public AuthorizationRule<MeasureReport> getMeasureReportAuthorizationRule()
	{
		return measureReportAuthorizationRule;
	}

	@Override
	public AuthorizationRule<NamingSystem> getNamingSystemAuthorizationRule()
	{
		return namingSystemAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Organization> getOrganizationAuthorizationRule()
	{
		return organizationAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Patient> getPatientAuthorizationRule()
	{
		return patientAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Practitioner> getPractitionerAuthorizationRule()
	{
		return practitionerAuthorizationRule;
	}

	@Override
	public AuthorizationRule<PractitionerRole> getPractitionerRoleAuthorizationRule()
	{
		return practitionerRoleAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Provenance> getProvenanceAuthorizationRule()
	{
		return provenanceAuthorizationRule;
	}

	@Override
	public AuthorizationRule<ResearchStudy> getResearchStudyAuthorizationRule()
	{
		return researchStudyAuthorizationRule;
	}

	@Override
	public AuthorizationRule<StructureDefinition> getStructureDefinitionAuthorizationRule()
	{
		return structureDefinitionAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Subscription> getSubscriptionAuthorizationRule()
	{
		return subscriptionAuthorizationRule;
	}

	@Override
	public AuthorizationRule<Task> getTaskAuthorizationRule()
	{
		return taskAuthorizationRule;
	}

	@Override
	public AuthorizationRule<ValueSet> getValueSetAuthorizationRule()
	{
		return valueSetAuthorizationRule;
	}

	@Override
	public Optional<AuthorizationRule<?>> getAuthorizationRule(Class<?> resourceClass)
	{
		AuthorizationRule<?> authorizationRule = authorizationRulesByResourecClass.get(resourceClass);
		return Optional.ofNullable(authorizationRule);
	}

	@Override
	public Optional<AuthorizationRule<?>> getAuthorizationRule(String resourceTypeName)
	{
		AuthorizationRule<?> authorizationRule = authorizationRulesByResourceTypeName.get(resourceTypeName);
		return Optional.ofNullable(authorizationRule);
	}
}
