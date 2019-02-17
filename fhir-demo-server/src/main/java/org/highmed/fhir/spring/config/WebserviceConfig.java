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

	@Autowired
	private ValidationConfig validationConfig;

	@Autowired
	private SnapshotConfig snapshotConfig;

	@Autowired
	private EventConfig eventConfig;

	@Bean
	public ConformanceService conformanceService()
	{
		return new ConformanceService(serverBase, defaultPageCount);
	}

	@Bean
	public HealthcareServiceService healthcareServiceService()
	{
		return new HealthcareServiceService(serverBase, defaultPageCount, daoConfig.healthcareServiceDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager());
	}

	@Bean
	public LocationService locationService()
	{
		return new LocationService(serverBase, defaultPageCount, daoConfig.locationDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager());
	}

	@Bean
	public OrganizationService organizationService()
	{
		return new OrganizationService(serverBase, defaultPageCount, daoConfig.organizationDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager());
	}

	@Bean
	public PatientService patientService()
	{
		return new PatientService(serverBase, defaultPageCount, daoConfig.patientDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager());
	}

	@Bean
	public PractitionerRoleService practitionerRoleService()
	{
		return new PractitionerRoleService(serverBase, defaultPageCount, daoConfig.practitionerRoleDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager());
	}

	@Bean
	public PractitionerService practitionerService()
	{
		return new PractitionerService(serverBase, defaultPageCount, daoConfig.practitionerDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager());
	}

	@Bean
	public ProvenanceService provenanceService()
	{
		return new ProvenanceService(serverBase, defaultPageCount, daoConfig.provenanceDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager());
	}

	@Bean
	public ResearchStudyService researchStudyService()
	{
		return new ResearchStudyService(serverBase, defaultPageCount, daoConfig.researchStudyDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager());
	}

	@Bean
	public StructureDefinitionService structureDefinitionService()
	{
		return new StructureDefinitionService(serverBase, defaultPageCount, daoConfig.structureDefinitionDao(),
				daoConfig.structureDefinitionSnapshotDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), snapshotConfig.snapshotGenerator());
	}

	@Bean
	public SubscriptionService subscriptionService()
	{
		return new SubscriptionService(serverBase, defaultPageCount, daoConfig.subscriptionDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager());
	}

	@Bean
	public TaskService taskService()
	{
		return new TaskService(serverBase, defaultPageCount, daoConfig.taskDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager());
	}
}
