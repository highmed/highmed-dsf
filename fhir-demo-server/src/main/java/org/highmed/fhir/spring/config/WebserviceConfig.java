package org.highmed.fhir.spring.config;

import org.highmed.fhir.webservice.impl.ConformanceServiceImpl;
import org.highmed.fhir.webservice.impl.HealthcareServiceServiceImpl;
import org.highmed.fhir.webservice.impl.LocationServiceImpl;
import org.highmed.fhir.webservice.impl.OrganizationServiceImpl;
import org.highmed.fhir.webservice.impl.PatientServiceImpl;
import org.highmed.fhir.webservice.impl.PractitionerRoleServiceImpl;
import org.highmed.fhir.webservice.impl.PractitionerServiceImpl;
import org.highmed.fhir.webservice.impl.ProvenanceServiceImpl;
import org.highmed.fhir.webservice.impl.ResearchStudyServiceImpl;
import org.highmed.fhir.webservice.impl.ServiceHelperImpl;
import org.highmed.fhir.webservice.impl.StructureDefinitionServiceImpl;
import org.highmed.fhir.webservice.impl.SubscriptionServiceImpl;
import org.highmed.fhir.webservice.impl.TaskServiceImpl;
import org.highmed.fhir.webservice.jaxrs.ConformanceServiceJaxrs;
import org.highmed.fhir.webservice.jaxrs.HealthcareServiceServiceJaxrs;
import org.highmed.fhir.webservice.jaxrs.LocationServiceJaxrs;
import org.highmed.fhir.webservice.jaxrs.OrganizationServiceJaxrs;
import org.highmed.fhir.webservice.jaxrs.PatientServiceJaxrs;
import org.highmed.fhir.webservice.jaxrs.PractitionerRoleServiceJaxrs;
import org.highmed.fhir.webservice.jaxrs.PractitionerServiceJaxrs;
import org.highmed.fhir.webservice.jaxrs.ProvenanceServiceJaxrs;
import org.highmed.fhir.webservice.jaxrs.ResearchStudyServiceJaxrs;
import org.highmed.fhir.webservice.jaxrs.StructureDefinitionServiceJaxrs;
import org.highmed.fhir.webservice.jaxrs.SubscriptionServiceJaxrs;
import org.highmed.fhir.webservice.jaxrs.TaskServiceJaxrs;
import org.highmed.fhir.webservice.secure.ConformanceServiceSecure;
import org.highmed.fhir.webservice.secure.HealthcareServiceServiceSecure;
import org.highmed.fhir.webservice.secure.LocationServiceSecure;
import org.highmed.fhir.webservice.secure.OrganizationServiceSecure;
import org.highmed.fhir.webservice.secure.PatientServiceSecure;
import org.highmed.fhir.webservice.secure.PractitionerRoleServiceSecure;
import org.highmed.fhir.webservice.secure.PractitionerServiceSecure;
import org.highmed.fhir.webservice.secure.ProvenanceServiceSecure;
import org.highmed.fhir.webservice.secure.ResearchStudyServiceSecure;
import org.highmed.fhir.webservice.secure.StructureDefinitionServiceSecure;
import org.highmed.fhir.webservice.secure.SubscriptionServiceSecure;
import org.highmed.fhir.webservice.secure.TaskServiceSecure;
import org.highmed.fhir.webservice.specification.ConformanceService;
import org.highmed.fhir.webservice.specification.HealthcareServiceService;
import org.highmed.fhir.webservice.specification.LocationService;
import org.highmed.fhir.webservice.specification.OrganizationService;
import org.highmed.fhir.webservice.specification.PatientService;
import org.highmed.fhir.webservice.specification.PractitionerRoleService;
import org.highmed.fhir.webservice.specification.PractitionerService;
import org.highmed.fhir.webservice.specification.ProvenanceService;
import org.highmed.fhir.webservice.specification.ResearchStudyService;
import org.highmed.fhir.webservice.specification.StructureDefinitionService;
import org.highmed.fhir.webservice.specification.SubscriptionService;
import org.highmed.fhir.webservice.specification.TaskService;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public <R extends DomainResource> ServiceHelperImpl<R> serviceHelper(Class<R> resourceType)
	{
		return new ServiceHelperImpl<R>(serverBase, resourceType);
	}

	@Bean
	public ConformanceService conformanceService()
	{
		return new ConformanceServiceJaxrs(
				new ConformanceServiceSecure(new ConformanceServiceImpl(serverBase, defaultPageCount)));
	}

	@Bean
	public HealthcareServiceService healthcareServiceService()
	{
		return new HealthcareServiceServiceJaxrs(new HealthcareServiceServiceSecure(new HealthcareServiceServiceImpl(
				serverBase, defaultPageCount, daoConfig.healthcareServiceDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), serviceHelper(HealthcareService.class))));
	}

	@Bean
	public LocationService locationService()
	{
		return new LocationServiceJaxrs(new LocationServiceSecure(new LocationServiceImpl(serverBase, defaultPageCount,
				daoConfig.locationDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				serviceHelper(Location.class))));
	}

	@Bean
	public OrganizationService organizationService()
	{
		return new OrganizationServiceJaxrs(new OrganizationServiceSecure(new OrganizationServiceImpl(serverBase,
				defaultPageCount, daoConfig.organizationDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), serviceHelper(Organization.class))));
	}

	@Bean
	public PatientService patientService()
	{
		return new PatientServiceJaxrs(new PatientServiceSecure(new PatientServiceImpl(serverBase, defaultPageCount,
				daoConfig.patientDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				serviceHelper(Patient.class))));
	}

	@Bean
	public PractitionerRoleService practitionerRoleService()
	{
		return new PractitionerRoleServiceJaxrs(new PractitionerRoleServiceSecure(new PractitionerRoleServiceImpl(
				serverBase, defaultPageCount, daoConfig.practitionerRoleDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), serviceHelper(PractitionerRole.class))));
	}

	@Bean
	public PractitionerService practitionerService()
	{
		return new PractitionerServiceJaxrs(new PractitionerServiceSecure(new PractitionerServiceImpl(serverBase,
				defaultPageCount, daoConfig.practitionerDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), serviceHelper(Practitioner.class))));
	}

	@Bean
	public ProvenanceService provenanceService()
	{
		return new ProvenanceServiceJaxrs(new ProvenanceServiceSecure(new ProvenanceServiceImpl(serverBase,
				defaultPageCount, daoConfig.provenanceDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), serviceHelper(Provenance.class))));
	}

	@Bean
	public ResearchStudyService researchStudyService()
	{
		return new ResearchStudyServiceJaxrs(new ResearchStudyServiceSecure(new ResearchStudyServiceImpl(serverBase,
				defaultPageCount, daoConfig.researchStudyDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), serviceHelper(ResearchStudy.class))));
	}

	@Bean
	public StructureDefinitionService structureDefinitionService()
	{
		return new StructureDefinitionServiceJaxrs(
				new StructureDefinitionServiceSecure(new StructureDefinitionServiceImpl(serverBase, defaultPageCount,
						daoConfig.structureDefinitionDao(), validationConfig.resourceValidator(),
						eventConfig.eventManager(), serviceHelper(StructureDefinition.class),
						daoConfig.structureDefinitionSnapshotDao(), snapshotConfig.snapshotGenerator())));
	}

	@Bean
	public SubscriptionService subscriptionService()
	{
		return new SubscriptionServiceJaxrs(new SubscriptionServiceSecure(new SubscriptionServiceImpl(serverBase,
				defaultPageCount, daoConfig.subscriptionDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), serviceHelper(Subscription.class))));
	}

	@Bean
	public TaskService taskService()
	{
		return new TaskServiceJaxrs(
				new TaskServiceSecure(new TaskServiceImpl(serverBase, defaultPageCount, daoConfig.taskDao(),
						validationConfig.resourceValidator(), eventConfig.eventManager(), serviceHelper(Task.class))));
	}
}
