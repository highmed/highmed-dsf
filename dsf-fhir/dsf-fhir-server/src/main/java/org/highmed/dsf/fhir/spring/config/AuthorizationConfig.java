package org.highmed.dsf.fhir.spring.config;

import java.util.List;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.OrganizationProviderWithDbBackend;
import org.highmed.dsf.fhir.authorization.ActivityDefinitionAuthorizationRule;
import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.authorization.AuthorizationRuleProvider;
import org.highmed.dsf.fhir.authorization.AuthorizationRuleProviderImpl;
import org.highmed.dsf.fhir.authorization.BinaryAuthorizationRule;
import org.highmed.dsf.fhir.authorization.BundleAuthorizationRule;
import org.highmed.dsf.fhir.authorization.CodeSystemAuthorizationRule;
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
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthorizationConfig
{
	@Value("${org.highmed.dsf.fhir.serverBase}")
	private String serverBase;

	@Value("${org.highmed.dsf.fhir.organizationType}")
	private String organizationType;

	@Value("#{'${org.highmed.dsf.fhir.local-user.thumbprints}'.split(',')}")
	private List<String> localUserThumbprints;

	@Value("#{'${org.highmed.dsf.fhir.local-deletion-user.thumbprints}'.split(',')}")
	private List<String> localDeletionUserThumbprints;

	@Value("${org.highmed.dsf.fhir.local-organization.identifier}")
	private String localIdentifierValue;

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
				localUserThumbprints, localDeletionUserThumbprints, localIdentifierValue);
	}

	@Bean
	public AuthorizationRule<ActivityDefinition> activityDefinitionAuthorizationRule()
	{
		return new ActivityDefinitionAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper(),
				processAuthorizationHelper());
	}

	@Bean
	public AuthorizationRule<Binary> binaryAuthorizationRule()
	{
		return new BinaryAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver(),
				organizationProvider(), readAccessHelper(),

				// Binary and Task not supported as securityContext rule
				activityDefinitionAuthorizationRule(), bundleAuthorizationRule(), codeSystemAuthorizationRule(),
				endpointAuthorizationRule(), groupAuthorizationRule(), healthcareServiceAuthorizationRule(),
				libraryAuthorizationRule(), locationAuthorizationRule(), measureAuthorizationRule(),
				measureReportAuthorizationRule(), namingSystemAuthorizationRule(), organizationAuthorizationRule(),
				organizationAffiliationAuthorizationRule(), patientAuthorizationRule(), practitionerAuthorizationRule(),
				practitionerRoleAuthorizationRule(), provenanceAuthorizationRule(), researchStudyAuthorizationRule(),
				structureDefinitionAuthorizationRule(), subscriptionAuthorizationRule(), valueSetAuthorizationRule());
	}

	@Bean
	public AuthorizationRule<Bundle> bundleAuthorizationRule()
	{
		return new BundleAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver(),
				organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<CodeSystem> codeSystemAuthorizationRule()
	{
		return new CodeSystemAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver(),
				organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<Endpoint> endpointAuthorizationRule()
	{
		return new EndpointAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver(),
				organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<Group> groupAuthorizationRule()
	{
		return new GroupAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver(),
				organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<HealthcareService> healthcareServiceAuthorizationRule()
	{
		return new HealthcareServiceAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<Library> libraryAuthorizationRule()
	{
		return new LibraryAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver(),
				organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<Location> locationAuthorizationRule()
	{
		return new LocationAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver(),
				organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<Measure> measureAuthorizationRule()
	{
		return new MeasureAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver(),
				organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<MeasureReport> measureReportAuthorizationRule()
	{
		return new MeasureReportAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<NamingSystem> namingSystemAuthorizationRule()
	{
		return new NamingSystemAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<Organization> organizationAuthorizationRule()
	{
		return new OrganizationAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<OrganizationAffiliation> organizationAffiliationAuthorizationRule()
	{
		return new OrganizationAffiliationAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<Patient> patientAuthorizationRule()
	{
		return new PatientAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver(),
				organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<Practitioner> practitionerAuthorizationRule()
	{
		return new PractitionerAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<PractitionerRole> practitionerRoleAuthorizationRule()
	{
		return new PractitionerRoleAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<Provenance> provenanceAuthorizationRule()
	{
		return new ProvenanceAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver(),
				organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<ResearchStudy> researchStudyAuthorizationRule()
	{
		return new ResearchStudyAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<StructureDefinition> structureDefinitionAuthorizationRule()
	{
		return new StructureDefinitionAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<Subscription> subscriptionAuthorizationRule()
	{
		return new SubscriptionAuthorizationRule(daoConfig.daoProvider(), serverBase,
				referenceConfig.referenceResolver(), organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRule<Task> taskAuthorizationRule()
	{
		return new TaskAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver(),
				organizationProvider(), readAccessHelper(), processAuthorizationHelper());
	}

	@Bean
	public AuthorizationRule<ValueSet> valueSetAuthorizationRule()
	{
		return new ValueSetAuthorizationRule(daoConfig.daoProvider(), serverBase, referenceConfig.referenceResolver(),
				organizationProvider(), readAccessHelper());
	}

	@Bean
	public AuthorizationRuleProvider authorizationRuleProvider()
	{
		return new AuthorizationRuleProviderImpl(activityDefinitionAuthorizationRule(), binaryAuthorizationRule(),
				bundleAuthorizationRule(), codeSystemAuthorizationRule(), endpointAuthorizationRule(),
				groupAuthorizationRule(), healthcareServiceAuthorizationRule(), libraryAuthorizationRule(),
				locationAuthorizationRule(), measureAuthorizationRule(), measureReportAuthorizationRule(),
				namingSystemAuthorizationRule(), organizationAuthorizationRule(),
				organizationAffiliationAuthorizationRule(), patientAuthorizationRule(), practitionerAuthorizationRule(),
				practitionerRoleAuthorizationRule(), provenanceAuthorizationRule(), researchStudyAuthorizationRule(),
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
