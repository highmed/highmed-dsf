package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.authorization.ActivityDefinitionAuthorizationRule;
import org.highmed.dsf.fhir.authorization.BinaryAuthorizationRule;
import org.highmed.dsf.fhir.authorization.BundleAuthorizationRule;
import org.highmed.dsf.fhir.authorization.CodeSystemAuthorizationRule;
import org.highmed.dsf.fhir.authorization.EndpointAuthorizationRule;
import org.highmed.dsf.fhir.authorization.GroupAuthorizationRule;
import org.highmed.dsf.fhir.authorization.HealthcareServiceAuthorizationRule;
import org.highmed.dsf.fhir.authorization.LocationAuthorizationRule;
import org.highmed.dsf.fhir.authorization.NamingSystemAuthorizationRule;
import org.highmed.dsf.fhir.authorization.OrganizationAuthorizationRule;
import org.highmed.dsf.fhir.authorization.PatientAuthorizationRule;
import org.highmed.dsf.fhir.authorization.PractitionerAuthorizationRule;
import org.highmed.dsf.fhir.authorization.PractitionerRoleAuthorizationRule;
import org.highmed.dsf.fhir.authorization.ProvenanceAuthorizationRule;
import org.highmed.dsf.fhir.authorization.ResearchStudyAuthorizationRule;
import org.highmed.dsf.fhir.authorization.StructureDefinitionAuthorizationRule;
import org.highmed.dsf.fhir.authorization.SubscriptionAuthorizationRule;
import org.highmed.dsf.fhir.authorization.TaskAuthorizationRule;
import org.highmed.dsf.fhir.authorization.ValueSetAuthorizationRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthorizationConfig
{
	@Autowired
	private DaoConfig daoConfig;

	@Bean
	public ActivityDefinitionAuthorizationRule activityDefinitionAuthorizationRule()
	{
		return new ActivityDefinitionAuthorizationRule(daoConfig.activityDefinitionDao());
	}

	@Bean
	public BinaryAuthorizationRule binaryAuthorizationRule()
	{
		return new BinaryAuthorizationRule(daoConfig.binaryDao());
	}

	@Bean
	public BundleAuthorizationRule bundleAuthorizationRule()
	{
		return new BundleAuthorizationRule(daoConfig.bundleDao());
	}

	@Bean
	public CodeSystemAuthorizationRule codeSystemAuthorizationRule()
	{
		return new CodeSystemAuthorizationRule(daoConfig.codeSystemDao());
	}

	@Bean
	public EndpointAuthorizationRule endpointAuthorizationRule()
	{
		return new EndpointAuthorizationRule(daoConfig.endpointDao());
	}

	@Bean
	public GroupAuthorizationRule groupAuthorizationRule()
	{
		return new GroupAuthorizationRule(daoConfig.groupDao());
	}

	@Bean
	public HealthcareServiceAuthorizationRule healthcareServiceAuthorizationRule()
	{
		return new HealthcareServiceAuthorizationRule(daoConfig.healthcareServiceDao());
	}

	@Bean
	public LocationAuthorizationRule locationAuthorizationRule()
	{
		return new LocationAuthorizationRule(daoConfig.locationDao());
	}

	@Bean
	public NamingSystemAuthorizationRule namingSystemAuthorizationRule()
	{
		return new NamingSystemAuthorizationRule(daoConfig.namingSystemDao());
	}

	@Bean
	public OrganizationAuthorizationRule organizationAuthorizationRule()
	{
		return new OrganizationAuthorizationRule(daoConfig.organizationDao());
	}

	@Bean
	public PatientAuthorizationRule patientAuthorizationRule()
	{
		return new PatientAuthorizationRule(daoConfig.patientDao());
	}

	@Bean
	public PractitionerAuthorizationRule practitionerAuthorizationRule()
	{
		return new PractitionerAuthorizationRule(daoConfig.practitionerDao());
	}

	@Bean
	public PractitionerRoleAuthorizationRule practitionerRoleAuthorizationRule()
	{
		return new PractitionerRoleAuthorizationRule(daoConfig.practitionerRoleDao());
	}

	@Bean
	public ProvenanceAuthorizationRule provenanceAuthorizationRule()
	{
		return new ProvenanceAuthorizationRule(daoConfig.provenanceDao());
	}

	@Bean
	public ResearchStudyAuthorizationRule researchStudyAuthorizationRule()
	{
		return new ResearchStudyAuthorizationRule(daoConfig.researchStudyDao());
	}

	@Bean
	public StructureDefinitionAuthorizationRule structureDefinitionAuthorizationRule()
	{
		return new StructureDefinitionAuthorizationRule(daoConfig.structureDefinitionDao());
	}

	@Bean
	public SubscriptionAuthorizationRule subscriptionAuthorizationRule()
	{
		return new SubscriptionAuthorizationRule(daoConfig.subscriptionDao());
	}

	@Bean
	public TaskAuthorizationRule taskAuthorizationRule()
	{
		return new TaskAuthorizationRule(daoConfig.taskDao());
	}

	@Bean
	public ValueSetAuthorizationRule valueSetAuthorizationRule()
	{
		return new ValueSetAuthorizationRule(daoConfig.valueSetDao());
	}
}
