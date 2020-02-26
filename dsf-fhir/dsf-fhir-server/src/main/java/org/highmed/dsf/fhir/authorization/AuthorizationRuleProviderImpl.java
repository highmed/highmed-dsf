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
import org.hl7.fhir.r4.model.Location;
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
	private final ActivityDefinitionAuthorizationRule activityDefinitionAuthorizationRule;
	private final BinaryAuthorizationRule binaryAuthorizationRule;
	private final BundleAuthorizationRule bundleAuthorizationRule;
	private final CodeSystemAuthorizationRule codeSystemAuthorizationRule;
	private final EndpointAuthorizationRule endpointAuthorizationRule;
	private final GroupAuthorizationRule groupAuthorizationRule;
	private final HealthcareServiceAuthorizationRule healthcareServiceAuthorizationRule;
	private final LocationAuthorizationRule locationAuthorizationRule;
	private final NamingSystemAuthorizationRule namingSystemAuthorizationRule;
	private final OrganizationAuthorizationRule organizationAuthorizationRule;
	private final PatientAuthorizationRule patientAuthorizationRule;
	private final PractitionerAuthorizationRule practitionerAuthorizationRule;
	private final PractitionerRoleAuthorizationRule practitionerRoleAuthorizationRule;
	private final ProvenanceAuthorizationRule provenanceAuthorizationRule;
	private final ResearchStudyAuthorizationRule researchStudyAuthorizationRule;
	private final StructureDefinitionAuthorizationRule structureDefinitionAuthorizationRule;
	private final SubscriptionAuthorizationRule subscriptionAuthorizationRule;
	private final TaskAuthorizationRule taskAuthorizationRule;
	private final ValueSetAuthorizationRule valueSetAuthorizationRule;

	private final Map<Class<? extends Resource>, AuthorizationRule<?>> authorizationRulesByResourecClass = new HashMap<>();
	private final Map<String, AuthorizationRule<?>> authorizationRulesByResourceTypeName = new HashMap<>();

	public AuthorizationRuleProviderImpl(ActivityDefinitionAuthorizationRule activityDefinitionAuthorizationRule,
			BinaryAuthorizationRule binaryAuthorizationRule, BundleAuthorizationRule bundleAuthorizationRule,
			CodeSystemAuthorizationRule codeSystemAuthorizationRule,
			EndpointAuthorizationRule endpointAuthorizationRule, GroupAuthorizationRule groupAuthorizationRule,
			HealthcareServiceAuthorizationRule healthcareServiceAuthorizationRule,
			LocationAuthorizationRule locationAuthorizationRule,
			NamingSystemAuthorizationRule namingSystemAuthorizationRule,
			OrganizationAuthorizationRule organizationAuthorizationRule,
			PatientAuthorizationRule patientAuthorizationRule,
			PractitionerAuthorizationRule practitionerAuthorizationRule,
			PractitionerRoleAuthorizationRule practitionerRoleAuthorizationRule,
			ProvenanceAuthorizationRule provenanceAuthorizationRule,
			ResearchStudyAuthorizationRule researchStudyAuthorizationRule,
			StructureDefinitionAuthorizationRule structureDefinitionAuthorizationRule,
			SubscriptionAuthorizationRule subscriptionAuthorizationRule, TaskAuthorizationRule taskAuthorizationRule,
			ValueSetAuthorizationRule valueSetAuthorizationRule)
	{
		this.activityDefinitionAuthorizationRule = activityDefinitionAuthorizationRule;
		this.binaryAuthorizationRule = binaryAuthorizationRule;
		this.bundleAuthorizationRule = bundleAuthorizationRule;
		this.codeSystemAuthorizationRule = codeSystemAuthorizationRule;
		this.endpointAuthorizationRule = endpointAuthorizationRule;
		this.groupAuthorizationRule = groupAuthorizationRule;
		this.healthcareServiceAuthorizationRule = healthcareServiceAuthorizationRule;
		this.locationAuthorizationRule = locationAuthorizationRule;
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
		authorizationRulesByResourecClass.put(Location.class, locationAuthorizationRule);
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
	public ActivityDefinitionAuthorizationRule getActivityDefinitionAuthorizationRule()
	{
		return activityDefinitionAuthorizationRule;
	}

	@Override
	public BinaryAuthorizationRule getBinaryAuthorizationRule()
	{
		return binaryAuthorizationRule;
	}

	@Override
	public BundleAuthorizationRule getBundleAuthorizationRule()
	{
		return bundleAuthorizationRule;
	}

	@Override
	public CodeSystemAuthorizationRule getCodeSystemAuthorizationRule()
	{
		return codeSystemAuthorizationRule;
	}

	@Override
	public EndpointAuthorizationRule getEndpointAuthorizationRule()
	{
		return endpointAuthorizationRule;
	}

	@Override
	public GroupAuthorizationRule getGroupAuthorizationRule()
	{
		return groupAuthorizationRule;
	}

	@Override
	public HealthcareServiceAuthorizationRule getHealthcareServiceAuthorizationRule()
	{
		return healthcareServiceAuthorizationRule;
	}

	@Override
	public LocationAuthorizationRule getLocationAuthorizationRule()
	{
		return locationAuthorizationRule;
	}

	@Override
	public NamingSystemAuthorizationRule getNamingSystemAuthorizationRule()
	{
		return namingSystemAuthorizationRule;
	}

	@Override
	public OrganizationAuthorizationRule getOrganizationAuthorizationRule()
	{
		return organizationAuthorizationRule;
	}

	@Override
	public PatientAuthorizationRule getPatientAuthorizationRule()
	{
		return patientAuthorizationRule;
	}

	@Override
	public PractitionerAuthorizationRule getPractitionerAuthorizationRule()
	{
		return practitionerAuthorizationRule;
	}

	@Override
	public PractitionerRoleAuthorizationRule getPractitionerRoleAuthorizationRule()
	{
		return practitionerRoleAuthorizationRule;
	}

	@Override
	public ProvenanceAuthorizationRule getProvenanceAuthorizationRule()
	{
		return provenanceAuthorizationRule;
	}

	@Override
	public ResearchStudyAuthorizationRule getResearchStudyAuthorizationRule()
	{
		return researchStudyAuthorizationRule;
	}

	@Override
	public StructureDefinitionAuthorizationRule getStructureDefinitionAuthorizationRule()
	{
		return structureDefinitionAuthorizationRule;
	}

	@Override
	public SubscriptionAuthorizationRule getSubscriptionAuthorizationRule()
	{
		return subscriptionAuthorizationRule;
	}

	@Override
	public TaskAuthorizationRule getTaskAuthorizationRule()
	{
		return taskAuthorizationRule;
	}

	@Override
	public ValueSetAuthorizationRule getValueSetAuthorizationRule()
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
