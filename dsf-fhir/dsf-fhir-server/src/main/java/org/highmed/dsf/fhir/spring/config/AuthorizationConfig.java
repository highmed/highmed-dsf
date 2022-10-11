package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.OrganizationProviderWithDbBackend;
import org.highmed.dsf.fhir.authorization.ActivityDefinitionAuthorizationRule;
import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.authorization.AuthorizationRuleProvider;
import org.highmed.dsf.fhir.authorization.AuthorizationRuleProviderImpl;
import org.highmed.dsf.fhir.authorization.BinaryAuthorizationRule;
import org.highmed.dsf.fhir.authorization.BundleAuthorizationRule;
import org.highmed.dsf.fhir.authorization.CodeSystemAuthorizationRule;
import org.highmed.dsf.fhir.authorization.DocumentReferenceAuthorizationRule;
import org.highmed.dsf.fhir.authorization.EndpointAuthorizationRule;
import org.highmed.dsf.fhir.authorization.GroupAuthorizationRule;
import org.highmed.dsf.fhir.authorization.HealthcareServiceAuthorizationRule;
import org.highmed.dsf.fhir.authorization.LibraryAuthorizationRule;
import org.highmed.dsf.fhir.authorization.LocationAuthorizationRule;
import org.highmed.dsf.fhir.authorization.MeasureAuthorizationRule;
import org.highmed.dsf.fhir.authorization.MeasureReportAuthorizationRule;
import org.highmed.dsf.fhir.authorization.NamingSystemAuthorizationRule;
import org.highmed.dsf.fhir.authorization.OrganizationAffiliationAuthorizationRule;
import org.highmed.dsf.fhir.authorization.OrganizationAuthorizationRule;
import org.highmed.dsf.fhir.authorization.PatientAuthorizationRule;
import org.highmed.dsf.fhir.authorization.PractitionerAuthorizationRule;
import org.highmed.dsf.fhir.authorization.PractitionerRoleAuthorizationRule;
import org.highmed.dsf.fhir.authorization.ProvenanceAuthorizationRule;
import org.highmed.dsf.fhir.authorization.QuestionnaireAuthorizationRule;
import org.highmed.dsf.fhir.authorization.QuestionnaireResponseAuthorizationRule;
import org.highmed.dsf.fhir.authorization.ResearchStudyAuthorizationRule;
import org.highmed.dsf.fhir.authorization.RootAuthorizationRule;
import org.highmed.dsf.fhir.authorization.StructureDefinitionAuthorizationRule;
import org.highmed.dsf.fhir.authorization.SubscriptionAuthorizationRule;
import org.highmed.dsf.fhir.authorization.TaskAuthorizationRule;
import org.highmed.dsf.fhir.authorization.ValueSetAuthorizationRule;
import org.highmed.dsf.fhir.authorization.process.ProcessAuthorizationHelper;
import org.highmed.dsf.fhir.authorization.process.ProcessAuthorizationHelperImpl;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelperImpl;
import org.highmed.dsf.fhir.dao.command.AuthorizationHelper;
import org.highmed.dsf.fhir.dao.command.AuthorizationHelperImpl;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthorizationConfig
{
	@Autowired
	private PropertiesConfig propertiesConfig;

	@Autowired
	private DaoConfig daoConfig;

	@Autowired
	private ReferenceConfig referenceConfig;

	@Autowired
	private HelperConfig helperConfig;

	@Bean
	public ReadAccessHelper readAccessHelper()
	{
		return new ReadAccessHelperImpl();
	}

	@Bean
	public ProcessAuthorizationHelper processAuthorizationHelper()
	{
		return new ProcessAuthorizationHelperImpl();
	}

	@Bean
	public OrganizationProvider organizationProvider()
	{
		return new OrganizationProviderWithDbBackend(daoConfig.organizationDao(), helperConfig.exceptionHandler(),
				propertiesConfig.getUserThumbprints(), propertiesConfig.getUserPermanentDeleteThumbprints(),
				propertiesConfig.getOrganizationIdentifierValue());
	}

	@Bean
	public AuthorizationRule<ActivityDefinition> activityDefinitionAuthorizationRule()
	{
		return new ActivityDefinitionAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter(), processAuthorizationHelper());
	}

	@Bean
	public AuthorizationRule<Binary> binaryAuthorizationRule()
	{
		return new BinaryAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter(),

				// Binary and Task not supported as securityContext rule
				activityDefinitionAuthorizationRule(), bundleAuthorizationRule(), codeSystemAuthorizationRule(),
				documentReferenceAuthorizationRule(), endpointAuthorizationRule(), groupAuthorizationRule(),
				healthcareServiceAuthorizationRule(), libraryAuthorizationRule(), locationAuthorizationRule(),
				measureAuthorizationRule(), measureReportAuthorizationRule(), namingSystemAuthorizationRule(),
				organizationAuthorizationRule(), organizationAffiliationAuthorizationRule(), patientAuthorizationRule(),
				practitionerAuthorizationRule(), practitionerRoleAuthorizationRule(), provenanceAuthorizationRule(),
				questionnaireAuthorizationRule(), questionnaireResponseAuthorizationRule(),
				researchStudyAuthorizationRule(), structureDefinitionAuthorizationRule(),
				subscriptionAuthorizationRule(), valueSetAuthorizationRule());
	}

	@Bean
	public AuthorizationRule<Bundle> bundleAuthorizationRule()
	{
		return new BundleAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<CodeSystem> codeSystemAuthorizationRule()
	{
		return new CodeSystemAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<DocumentReference> documentReferenceAuthorizationRule()
	{
		return new DocumentReferenceAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<Endpoint> endpointAuthorizationRule()
	{
		return new EndpointAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<Group> groupAuthorizationRule()
	{
		return new GroupAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<HealthcareService> healthcareServiceAuthorizationRule()
	{
		return new HealthcareServiceAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<Library> libraryAuthorizationRule()
	{
		return new LibraryAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<Location> locationAuthorizationRule()
	{
		return new LocationAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<Measure> measureAuthorizationRule()
	{
		return new MeasureAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<MeasureReport> measureReportAuthorizationRule()
	{
		return new MeasureReportAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<NamingSystem> namingSystemAuthorizationRule()
	{
		return new NamingSystemAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<Organization> organizationAuthorizationRule()
	{
		return new OrganizationAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<OrganizationAffiliation> organizationAffiliationAuthorizationRule()
	{
		return new OrganizationAffiliationAuthorizationRule(daoConfig.daoProvider(),
				propertiesConfig.getServerBaseUrl(), referenceConfig.referenceResolver(), organizationProvider(),
				readAccessHelper(), helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<Patient> patientAuthorizationRule()
	{
		return new PatientAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<Practitioner> practitionerAuthorizationRule()
	{
		return new PractitionerAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<PractitionerRole> practitionerRoleAuthorizationRule()
	{
		return new PractitionerRoleAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<Provenance> provenanceAuthorizationRule()
	{
		return new ProvenanceAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<Questionnaire> questionnaireAuthorizationRule()
	{
		return new QuestionnaireAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<QuestionnaireResponse> questionnaireResponseAuthorizationRule()
	{
		return new QuestionnaireResponseAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<ResearchStudy> researchStudyAuthorizationRule()
	{
		return new ResearchStudyAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<StructureDefinition> structureDefinitionAuthorizationRule()
	{
		return new StructureDefinitionAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<Subscription> subscriptionAuthorizationRule()
	{
		return new SubscriptionAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRule<Task> taskAuthorizationRule()
	{
		return new TaskAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				processAuthorizationHelper());
	}

	@Bean
	public AuthorizationRule<ValueSet> valueSetAuthorizationRule()
	{
		return new ValueSetAuthorizationRule(daoConfig.daoProvider(), propertiesConfig.getServerBaseUrl(),
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				helperConfig.parameterConverter());
	}

	@Bean
	public AuthorizationRuleProvider authorizationRuleProvider()
	{
		return new AuthorizationRuleProviderImpl(activityDefinitionAuthorizationRule(), binaryAuthorizationRule(),
				bundleAuthorizationRule(), codeSystemAuthorizationRule(), documentReferenceAuthorizationRule(),
				endpointAuthorizationRule(), groupAuthorizationRule(), healthcareServiceAuthorizationRule(),
				libraryAuthorizationRule(), locationAuthorizationRule(), measureAuthorizationRule(),
				measureReportAuthorizationRule(), namingSystemAuthorizationRule(), organizationAuthorizationRule(),
				organizationAffiliationAuthorizationRule(), patientAuthorizationRule(), practitionerAuthorizationRule(),
				practitionerRoleAuthorizationRule(), provenanceAuthorizationRule(), questionnaireAuthorizationRule(),
				questionnaireResponseAuthorizationRule(), researchStudyAuthorizationRule(),
				structureDefinitionAuthorizationRule(), subscriptionAuthorizationRule(), taskAuthorizationRule(),
				valueSetAuthorizationRule());
	}

	@Bean
	public AuthorizationHelper authorizationHelper()
	{
		return new AuthorizationHelperImpl(authorizationRuleProvider(), helperConfig.responseGenerator());
	}

	@Bean
	public AuthorizationRule<Resource> rootAuthorizationRule()
	{
		return new RootAuthorizationRule();
	}
}
