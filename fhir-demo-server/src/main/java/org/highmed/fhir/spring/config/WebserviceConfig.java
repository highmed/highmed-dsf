package org.highmed.fhir.spring.config;

import org.highmed.fhir.webservice.impl.CodeSystemServiceImpl;
import org.highmed.fhir.webservice.impl.ConformanceServiceImpl;
import org.highmed.fhir.webservice.impl.HealthcareServiceServiceImpl;
import org.highmed.fhir.webservice.impl.LocationServiceImpl;
import org.highmed.fhir.webservice.impl.OrganizationServiceImpl;
import org.highmed.fhir.webservice.impl.PatientServiceImpl;
import org.highmed.fhir.webservice.impl.PractitionerRoleServiceImpl;
import org.highmed.fhir.webservice.impl.PractitionerServiceImpl;
import org.highmed.fhir.webservice.impl.ProvenanceServiceImpl;
import org.highmed.fhir.webservice.impl.ResearchStudyServiceImpl;
import org.highmed.fhir.webservice.impl.StructureDefinitionServiceImpl;
import org.highmed.fhir.webservice.impl.SubscriptionServiceImpl;
import org.highmed.fhir.webservice.impl.TaskServiceImpl;
import org.highmed.fhir.webservice.impl.ValueSetServiceImpl;
import org.highmed.fhir.webservice.jaxrs.CodeSystemServiceJaxrs;
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
import org.highmed.fhir.webservice.jaxrs.ValueSetServiceJaxrs;
import org.highmed.fhir.webservice.secure.CodeSystemServiceSecure;
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
import org.highmed.fhir.webservice.secure.ValueSetServiceSecure;
import org.highmed.fhir.webservice.specification.CodeSystemService;
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
import org.highmed.fhir.webservice.specification.ValueSetService;
import org.hl7.fhir.r4.model.CodeSystem;
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
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

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

	@Autowired
	private HelperConfig helperConfig;

	@Bean
	public ConformanceService conformanceService()
	{
		return new ConformanceServiceJaxrs(new ConformanceServiceSecure(
				new ConformanceServiceImpl(serverBase, defaultPageCount, helperConfig.parameterConverter())));
	}

	private String resourceTypeName(Class<? extends DomainResource> r)
	{
		return r.getAnnotation(ResourceDef.class).name();
	}

	@Bean
	public CodeSystemService codeSystemService()
	{
		return new CodeSystemServiceJaxrs(new CodeSystemServiceSecure(codeSystemServiceImpl()));
	}

	private CodeSystemServiceImpl codeSystemServiceImpl()
	{
		return new CodeSystemServiceImpl(resourceTypeName(CodeSystem.class), serverBase, defaultPageCount,
				daoConfig.codeSystemDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(CodeSystem.class),
				helperConfig.responseGenerator(), helperConfig.parameterConverter());
	}

	@Bean
	public HealthcareServiceService healthcareServiceService()
	{
		return new HealthcareServiceServiceJaxrs(new HealthcareServiceServiceSecure(healthcareServiceServiceImpl()));
	}

	private HealthcareServiceServiceImpl healthcareServiceServiceImpl()
	{
		return new HealthcareServiceServiceImpl(resourceTypeName(HealthcareService.class), serverBase, defaultPageCount,
				daoConfig.healthcareServiceDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(HealthcareService.class),
				helperConfig.responseGenerator(), helperConfig.parameterConverter());
	}

	@Bean
	public LocationService locationService()
	{
		return new LocationServiceJaxrs(new LocationServiceSecure(locationServiceImpl()));
	}

	private LocationServiceImpl locationServiceImpl()
	{
		return new LocationServiceImpl(resourceTypeName(Location.class), serverBase, defaultPageCount,
				daoConfig.locationDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(Location.class),
				helperConfig.responseGenerator(), helperConfig.parameterConverter());
	}

	@Bean
	public OrganizationService organizationService()
	{
		return new OrganizationServiceJaxrs(new OrganizationServiceSecure(organizationServiceImpl()));
	}

	private OrganizationServiceImpl organizationServiceImpl()
	{
		return new OrganizationServiceImpl(resourceTypeName(Organization.class), serverBase, defaultPageCount,
				daoConfig.organizationDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(Organization.class),
				helperConfig.responseGenerator(), helperConfig.parameterConverter());
	}

	@Bean
	public PatientService patientService()
	{
		return new PatientServiceJaxrs(new PatientServiceSecure(patientServiceImpl()));
	}

	private PatientServiceImpl patientServiceImpl()
	{
		return new PatientServiceImpl(resourceTypeName(Patient.class), serverBase, defaultPageCount,
				daoConfig.patientDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(Patient.class),
				helperConfig.responseGenerator(), helperConfig.parameterConverter());
	}

	@Bean
	public PractitionerRoleService practitionerRoleService()
	{
		return new PractitionerRoleServiceJaxrs(new PractitionerRoleServiceSecure(practitionerRoleServiceImpl()));
	}

	private PractitionerRoleServiceImpl practitionerRoleServiceImpl()
	{
		return new PractitionerRoleServiceImpl(resourceTypeName(PractitionerRole.class), serverBase, defaultPageCount,
				daoConfig.practitionerRoleDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(PractitionerRole.class),
				helperConfig.responseGenerator(), helperConfig.parameterConverter());
	}

	@Bean
	public PractitionerService practitionerService()
	{
		return new PractitionerServiceJaxrs(new PractitionerServiceSecure(practitionerServiceImpl()));
	}

	private PractitionerServiceImpl practitionerServiceImpl()
	{
		return new PractitionerServiceImpl(resourceTypeName(Practitioner.class), serverBase, defaultPageCount,
				daoConfig.practitionerDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(Practitioner.class),
				helperConfig.responseGenerator(), helperConfig.parameterConverter());
	}

	@Bean
	public ProvenanceService provenanceService()
	{
		return new ProvenanceServiceJaxrs(new ProvenanceServiceSecure(provenanceServiceImpl()));
	}

	private ProvenanceServiceImpl provenanceServiceImpl()
	{
		return new ProvenanceServiceImpl(resourceTypeName(Provenance.class), serverBase, defaultPageCount,
				daoConfig.provenanceDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(Provenance.class),
				helperConfig.responseGenerator(), helperConfig.parameterConverter());
	}

	@Bean
	public ResearchStudyService researchStudyService()
	{
		return new ResearchStudyServiceJaxrs(new ResearchStudyServiceSecure(researchStudyServiceImpl()));
	}

	private ResearchStudyServiceImpl researchStudyServiceImpl()
	{
		return new ResearchStudyServiceImpl(resourceTypeName(ResearchStudy.class), serverBase, defaultPageCount,
				daoConfig.researchStudyDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(ResearchStudy.class),
				helperConfig.responseGenerator(), helperConfig.parameterConverter());
	}

	@Bean
	public StructureDefinitionService structureDefinitionService()
	{
		return new StructureDefinitionServiceJaxrs(
				new StructureDefinitionServiceSecure(structureDefinitionServiceImpl()));
	}

	private StructureDefinitionServiceImpl structureDefinitionServiceImpl()
	{
		return new StructureDefinitionServiceImpl(resourceTypeName(StructureDefinition.class), serverBase,
				defaultPageCount, daoConfig.structureDefinitionDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(StructureDefinition.class), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), daoConfig.structureDefinitionSnapshotDao(),
				snapshotConfig.snapshotGenerator(), snapshotConfig.snapshotDependencyAnalyzer());
	}

	@Bean
	public SubscriptionService subscriptionService()
	{
		return new SubscriptionServiceJaxrs(new SubscriptionServiceSecure(subscriptionServiceImpl()));
	}

	private SubscriptionServiceImpl subscriptionServiceImpl()
	{
		return new SubscriptionServiceImpl(resourceTypeName(Subscription.class), serverBase, defaultPageCount,
				daoConfig.subscriptionDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(Subscription.class),
				helperConfig.responseGenerator(), helperConfig.parameterConverter());
	}

	@Bean
	public TaskService taskService()
	{
		return new TaskServiceJaxrs(new TaskServiceSecure(taskServiceImpl()));
	}

	private TaskServiceImpl taskServiceImpl()
	{
		return new TaskServiceImpl(resourceTypeName(Task.class), serverBase, defaultPageCount, daoConfig.taskDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(Task.class), helperConfig.responseGenerator(),
				helperConfig.parameterConverter());
	}

	@Bean
	public ValueSetService valueSetService()
	{
		return new ValueSetServiceJaxrs(new ValueSetServiceSecure(valueSetServiceImpl()));
	}

	private ValueSetServiceImpl valueSetServiceImpl()
	{
		return new ValueSetServiceImpl(resourceTypeName(ValueSet.class), serverBase, defaultPageCount,
				daoConfig.valueSetDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(ValueSet.class),
				helperConfig.responseGenerator(), helperConfig.parameterConverter());
	}
}
