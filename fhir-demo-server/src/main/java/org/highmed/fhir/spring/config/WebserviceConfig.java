package org.highmed.fhir.spring.config;

import org.highmed.fhir.webservice.ConformanceService;
import org.highmed.fhir.webservice.HealthcareServiceService;
import org.highmed.fhir.webservice.LocationService;
import org.highmed.fhir.webservice.OrganizationService;
import org.highmed.fhir.webservice.PatientService;
import org.highmed.fhir.webservice.PractitionerRoleService;
import org.highmed.fhir.webservice.PractitionerService;
import org.highmed.fhir.webservice.ProvenanceService;
import org.highmed.fhir.webservice.ResearchStudyService;
import org.highmed.fhir.webservice.StructureDefinitionService;
import org.highmed.fhir.webservice.SubscriptionService;
import org.highmed.fhir.webservice.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebserviceConfig
{
	@Value("${org.highmed.fhir.serverBase}")
	private String serverBase;

	@Value("${org.highmed.fhir.defaultPageCount}")
	private int defaultPageCount;

	@Autowired
	private DaoConfig daoConfig;

	@Bean
	public ConformanceService conformanceService()
	{
		return new ConformanceService(serverBase);
	}

	@Bean
	public HealthcareServiceService healthcareServiceService()
	{
		return new HealthcareServiceService(serverBase, defaultPageCount, daoConfig.healthcareServiceDao());
	}

	@Bean
	public LocationService locationService()
	{
		return new LocationService(serverBase, defaultPageCount, daoConfig.locationDao());
	}

	@Bean
	public OrganizationService organizationService()
	{
		return new OrganizationService(serverBase, defaultPageCount, daoConfig.organizationDao());
	}

	@Bean
	public PatientService patientService()
	{
		return new PatientService(serverBase, defaultPageCount, daoConfig.patientDao());
	}

	@Bean
	public PractitionerRoleService practitionerRoleService()
	{
		return new PractitionerRoleService(serverBase, defaultPageCount, daoConfig.practitionerRoleDao());
	}

	@Bean
	public PractitionerService practitionerService()
	{
		return new PractitionerService(serverBase, defaultPageCount, daoConfig.practitionerDao());
	}

	@Bean
	public ProvenanceService provenanceService()
	{
		return new ProvenanceService(serverBase, defaultPageCount, daoConfig.provenanceDao());
	}

	@Bean
	public ResearchStudyService researchStudyService()
	{
		return new ResearchStudyService(serverBase, defaultPageCount, daoConfig.researchStudyDao());
	}

	@Bean
	public StructureDefinitionService structureDefinitionService()
	{
		return new StructureDefinitionService(serverBase, defaultPageCount, daoConfig.structureDefinitionDao());
	}

	@Bean
	public SubscriptionService subscriptionService()
	{
		return new SubscriptionService(serverBase, defaultPageCount, daoConfig.subscriptionDao());
	}

	@Bean
	public TaskService taskService()
	{
		return new TaskService(serverBase, defaultPageCount, daoConfig.taskDao());
	}
}
