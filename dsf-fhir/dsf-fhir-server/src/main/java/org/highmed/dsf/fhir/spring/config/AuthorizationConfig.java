package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.authorization.ActivityDefinitionAuthorizationRule;
import org.highmed.dsf.fhir.authorization.AuthorizationRuleProvider;
import org.highmed.dsf.fhir.authorization.AuthorizationRuleProviderImpl;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthorizationConfig
{
	@Value("${org.highmed.dsf.fhir.serverBase}")
	private String serverBase;

	@Autowired
	private DaoConfig daoConfig;

	@Autowired
	private ReferenceConfig referenceConfig;

	@Bean
	public ActivityDefinitionAuthorizationRule activityDefinitionAuthorizationRule()
	{
		return new ActivityDefinitionAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver());
	}

	@Bean
	public BinaryAuthorizationRule binaryAuthorizationRule()
	{
		return new BinaryAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver());
	}

	@Bean
	public BundleAuthorizationRule bundleAuthorizationRule()
	{
		return new BundleAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver());
	}

	@Bean
	public CodeSystemAuthorizationRule codeSystemAuthorizationRule()
	{
		return new CodeSystemAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver());
	}

	@Bean
	public EndpointAuthorizationRule endpointAuthorizationRule()
	{
		return new EndpointAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver());
	}

	@Bean
	public GroupAuthorizationRule groupAuthorizationRule()
	{
		return new GroupAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver());
	}

	@Bean
	public HealthcareServiceAuthorizationRule healthcareServiceAuthorizationRule()
	{
		return new HealthcareServiceAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver());
	}

	@Bean
	public LocationAuthorizationRule locationAuthorizationRule()
	{
		return new LocationAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver());
	}

	@Bean
	public NamingSystemAuthorizationRule namingSystemAuthorizationRule()
	{
		return new NamingSystemAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver());
	}

	@Bean
	public OrganizationAuthorizationRule organizationAuthorizationRule()
	{
		return new OrganizationAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver());
	}

	@Bean
	public PatientAuthorizationRule patientAuthorizationRule()
	{
		return new PatientAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver());
	}

	@Bean
	public PractitionerAuthorizationRule practitionerAuthorizationRule()
	{
		return new PractitionerAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver());
	}

	@Bean
	public PractitionerRoleAuthorizationRule practitionerRoleAuthorizationRule()
	{
		return new PractitionerRoleAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver());
	}

	@Bean
	public ProvenanceAuthorizationRule provenanceAuthorizationRule()
	{
		return new ProvenanceAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver());
	}

	@Bean
	public ResearchStudyAuthorizationRule researchStudyAuthorizationRule()
	{
		return new ResearchStudyAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver());
	}

	@Bean
	public StructureDefinitionAuthorizationRule structureDefinitionAuthorizationRule()
	{
		return new StructureDefinitionAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver());
	}

	@Bean
	public SubscriptionAuthorizationRule subscriptionAuthorizationRule()
	{
		return new SubscriptionAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver());
	}

	@Bean
	public TaskAuthorizationRule taskAuthorizationRule()
	{
		return new TaskAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver());
	}

	@Bean
	public ValueSetAuthorizationRule valueSetAuthorizationRule()
	{
		return new ValueSetAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver());
	}

	@Bean
	public AuthorizationRuleProvider authorizationRuleProvider()
	{
		return new AuthorizationRuleProviderImpl(activityDefinitionAuthorizationRule(), binaryAuthorizationRule(),
				bundleAuthorizationRule(), codeSystemAuthorizationRule(), endpointAuthorizationRule(),
				groupAuthorizationRule(), healthcareServiceAuthorizationRule(), locationAuthorizationRule(),
				namingSystemAuthorizationRule(), organizationAuthorizationRule(), patientAuthorizationRule(),
				practitionerAuthorizationRule(), practitionerRoleAuthorizationRule(), provenanceAuthorizationRule(),
				researchStudyAuthorizationRule(), structureDefinitionAuthorizationRule(),
				subscriptionAuthorizationRule(), taskAuthorizationRule(), valueSetAuthorizationRule());
	}
}
