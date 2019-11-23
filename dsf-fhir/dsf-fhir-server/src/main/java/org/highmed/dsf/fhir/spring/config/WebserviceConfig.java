package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.webservice.impl.BinaryServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.BundleServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.CodeSystemServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.ConformanceServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.EndpointServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.GroupServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.HealthcareServiceServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.LocationServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.NamingSystemServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.OrganizationServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.PatientServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.PractitionerRoleServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.PractitionerServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.ProvenanceServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.ResearchStudyServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.RootServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.StructureDefinitionServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.SubscriptionServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.TaskServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.ValueSetServiceImpl;
import org.highmed.dsf.fhir.webservice.jaxrs.BinaryServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.BundleServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.CodeSystemServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.ConformanceServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.EndpointServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.GroupServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.HealthcareServiceServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.LocationServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.NamingSystemServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.OrganizationServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.PatientServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.PractitionerRoleServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.PractitionerServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.ProvenanceServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.ResearchStudyServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.RootServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.StructureDefinitionServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.SubscriptionServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.TaskServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.ValueSetServiceJaxrs;
import org.highmed.dsf.fhir.webservice.secure.BinaryServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.BundleServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.CodeSystemServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.ConformanceServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.EndpointServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.GroupServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.HealthcareServiceServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.LocationServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.NamingSystemServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.OrganizationServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.PatientServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.PractitionerRoleServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.PractitionerServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.ProvenanceServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.ResearchStudyServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.RootServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.StructureDefinitionServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.SubscriptionServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.TaskServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.ValueSetServiceSecure;
import org.highmed.dsf.fhir.webservice.specification.BinaryService;
import org.highmed.dsf.fhir.webservice.specification.BundleService;
import org.highmed.dsf.fhir.webservice.specification.CodeSystemService;
import org.highmed.dsf.fhir.webservice.specification.ConformanceService;
import org.highmed.dsf.fhir.webservice.specification.EndpointService;
import org.highmed.dsf.fhir.webservice.specification.GroupService;
import org.highmed.dsf.fhir.webservice.specification.HealthcareServiceService;
import org.highmed.dsf.fhir.webservice.specification.LocationService;
import org.highmed.dsf.fhir.webservice.specification.NamingSystemService;
import org.highmed.dsf.fhir.webservice.specification.OrganizationService;
import org.highmed.dsf.fhir.webservice.specification.PatientService;
import org.highmed.dsf.fhir.webservice.specification.PractitionerRoleService;
import org.highmed.dsf.fhir.webservice.specification.PractitionerService;
import org.highmed.dsf.fhir.webservice.specification.ProvenanceService;
import org.highmed.dsf.fhir.webservice.specification.ResearchStudyService;
import org.highmed.dsf.fhir.webservice.specification.RootService;
import org.highmed.dsf.fhir.webservice.specification.StructureDefinitionService;
import org.highmed.dsf.fhir.webservice.specification.SubscriptionService;
import org.highmed.dsf.fhir.webservice.specification.TaskService;
import org.highmed.dsf.fhir.webservice.specification.ValueSetService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

@Configuration
public class WebserviceConfig
{
	@Value("${org.highmed.dsf.fhir.serverBase}")
	private String serverBase;

	@Value("${org.highmed.dsf.fhir.defaultPageCount}")
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

	@Autowired
	private CommandConfig commandConfig;

	@Autowired
	private BuildInfoReaderConfig buildInfoReaderConfig;

	@Bean
	public ConformanceService conformanceService()
	{
		return new ConformanceServiceJaxrs(new ConformanceServiceSecure(new ConformanceServiceImpl(serverBase,
				defaultPageCount, buildInfoReaderConfig.buildInfoReader(), helperConfig.parameterConverter())));
	}

	private String resourceTypeName(Class<? extends Resource> r)
	{
		return r.getAnnotation(ResourceDef.class).name();
	}

	@Bean
	public CodeSystemService codeSystemService()
	{
		return new CodeSystemServiceJaxrs(
				new CodeSystemServiceSecure(codeSystemServiceImpl(), helperConfig.responseGenerator()));
	}

	private CodeSystemServiceImpl codeSystemServiceImpl()
	{
		return new CodeSystemServiceImpl(resourceTypeName(CodeSystem.class), serverBase, CodeSystemServiceJaxrs.PATH,
				defaultPageCount, daoConfig.codeSystemDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), helperConfig.exceptionHandler(), eventConfig.eventGenerator(),
				helperConfig.responseGenerator(), helperConfig.parameterConverter(), commandConfig.referenceExtractor(),
				commandConfig.referenceResolver());
	}

	@Bean
	public BinaryService binaryService()
	{
		return new BinaryServiceJaxrs(new BinaryServiceSecure(binaryServiceImpl(), helperConfig.responseGenerator()));
	}

	private BinaryService binaryServiceImpl()
	{
		return new BinaryServiceImpl(resourceTypeName(Binary.class), serverBase, BinaryServiceJaxrs.PATH,
				defaultPageCount, daoConfig.binaryDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), helperConfig.exceptionHandler(), eventConfig.eventGenerator(),
				helperConfig.responseGenerator(), helperConfig.parameterConverter(), commandConfig.referenceExtractor(),
				commandConfig.referenceResolver());
	}

	@Bean
	public BundleService bundleService()
	{
		return new BundleServiceJaxrs(new BundleServiceSecure(bundleServiceImpl(), helperConfig.responseGenerator()));
	}

	private BundleService bundleServiceImpl()
	{
		return new BundleServiceImpl(resourceTypeName(Bundle.class), serverBase, BundleServiceJaxrs.PATH,
				defaultPageCount, daoConfig.bundleDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), helperConfig.exceptionHandler(), eventConfig.eventGenerator(),
				helperConfig.responseGenerator(), helperConfig.parameterConverter(), commandConfig.referenceExtractor(),
				commandConfig.referenceResolver());
	}

	@Bean
	public EndpointService endpointService()
	{
		return new EndpointServiceJaxrs(
				new EndpointServiceSecure(endpointServiceImpl(), helperConfig.responseGenerator()));
	}

	private EndpointServiceImpl endpointServiceImpl()
	{
		return new EndpointServiceImpl(resourceTypeName(Endpoint.class), serverBase, EndpointServiceJaxrs.PATH,
				defaultPageCount, daoConfig.endpointDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), helperConfig.exceptionHandler(), eventConfig.eventGenerator(),
				helperConfig.responseGenerator(), helperConfig.parameterConverter(), commandConfig.referenceExtractor(),
				commandConfig.referenceResolver());
	}

	@Bean
	public GroupService groupService()
	{
		return new GroupServiceJaxrs(new GroupServiceSecure(groupServiceImpl(), helperConfig.responseGenerator()));
	}

	private GroupServiceImpl groupServiceImpl()
	{
		return new GroupServiceImpl(resourceTypeName(Group.class), serverBase, GroupServiceJaxrs.PATH, defaultPageCount,
				daoConfig.groupDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), commandConfig.referenceExtractor(),
				commandConfig.referenceResolver());
	}

	@Bean
	public HealthcareServiceService healthcareServiceService()
	{
		return new HealthcareServiceServiceJaxrs(
				new HealthcareServiceServiceSecure(healthcareServiceServiceImpl(), helperConfig.responseGenerator()));
	}

	private HealthcareServiceServiceImpl healthcareServiceServiceImpl()
	{
		return new HealthcareServiceServiceImpl(resourceTypeName(HealthcareService.class), serverBase,
				HealthcareServiceServiceJaxrs.PATH, defaultPageCount, daoConfig.healthcareServiceDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				commandConfig.referenceExtractor(), commandConfig.referenceResolver());
	}

	@Bean
	public LocationService locationService()
	{
		return new LocationServiceJaxrs(
				new LocationServiceSecure(locationServiceImpl(), helperConfig.responseGenerator()));
	}

	private LocationServiceImpl locationServiceImpl()
	{
		return new LocationServiceImpl(resourceTypeName(Location.class), serverBase, LocationServiceJaxrs.PATH,
				defaultPageCount, daoConfig.locationDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), helperConfig.exceptionHandler(), eventConfig.eventGenerator(),
				helperConfig.responseGenerator(), helperConfig.parameterConverter(), commandConfig.referenceExtractor(),
				commandConfig.referenceResolver());
	}

	@Bean
	public NamingSystemService namingSystemService()
	{
		return new NamingSystemServiceJaxrs(
				new NamingSystemServiceSecure(namingSystemServiceImpl(), helperConfig.responseGenerator()));
	}

	private NamingSystemService namingSystemServiceImpl()
	{
		return new NamingSystemServiceImpl(resourceTypeName(NamingSystem.class), serverBase, LocationServiceJaxrs.PATH,
				defaultPageCount, daoConfig.namingSystemDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), helperConfig.exceptionHandler(), eventConfig.eventGenerator(),
				helperConfig.responseGenerator(), helperConfig.parameterConverter(), commandConfig.referenceExtractor(),
				commandConfig.referenceResolver());
	}

	@Bean
	public OrganizationService organizationService()
	{
		return new OrganizationServiceJaxrs(
				new OrganizationServiceSecure(organizationServiceImpl(), helperConfig.responseGenerator()));
	}

	private OrganizationServiceImpl organizationServiceImpl()
	{
		return new OrganizationServiceImpl(resourceTypeName(Organization.class), serverBase,
				OrganizationServiceJaxrs.PATH, defaultPageCount, daoConfig.organizationDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				commandConfig.referenceExtractor(), commandConfig.referenceResolver());
	}

	@Bean
	public PatientService patientService()
	{
		return new PatientServiceJaxrs(
				new PatientServiceSecure(patientServiceImpl(), helperConfig.responseGenerator()));
	}

	private PatientServiceImpl patientServiceImpl()
	{
		return new PatientServiceImpl(resourceTypeName(Patient.class), serverBase, PatientServiceJaxrs.PATH,
				defaultPageCount, daoConfig.patientDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), helperConfig.exceptionHandler(), eventConfig.eventGenerator(),
				helperConfig.responseGenerator(), helperConfig.parameterConverter(), commandConfig.referenceExtractor(),
				commandConfig.referenceResolver());
	}

	@Bean
	public PractitionerRoleService practitionerRoleService()
	{
		return new PractitionerRoleServiceJaxrs(
				new PractitionerRoleServiceSecure(practitionerRoleServiceImpl(), helperConfig.responseGenerator()));
	}

	private PractitionerRoleServiceImpl practitionerRoleServiceImpl()
	{
		return new PractitionerRoleServiceImpl(resourceTypeName(PractitionerRole.class), serverBase,
				PractitionerRoleServiceJaxrs.PATH, defaultPageCount, daoConfig.practitionerRoleDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				commandConfig.referenceExtractor(), commandConfig.referenceResolver());
	}

	@Bean
	public PractitionerService practitionerService()
	{
		return new PractitionerServiceJaxrs(
				new PractitionerServiceSecure(practitionerServiceImpl(), helperConfig.responseGenerator()));
	}

	private PractitionerServiceImpl practitionerServiceImpl()
	{
		return new PractitionerServiceImpl(resourceTypeName(Practitioner.class), serverBase,
				PractitionerServiceJaxrs.PATH, defaultPageCount, daoConfig.practitionerDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				commandConfig.referenceExtractor(), commandConfig.referenceResolver());
	}

	@Bean
	public ProvenanceService provenanceService()
	{
		return new ProvenanceServiceJaxrs(
				new ProvenanceServiceSecure(provenanceServiceImpl(), helperConfig.responseGenerator()));
	}

	private ProvenanceServiceImpl provenanceServiceImpl()
	{
		return new ProvenanceServiceImpl(resourceTypeName(Provenance.class), serverBase, ProvenanceServiceJaxrs.PATH,
				defaultPageCount, daoConfig.provenanceDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), helperConfig.exceptionHandler(), eventConfig.eventGenerator(),
				helperConfig.responseGenerator(), helperConfig.parameterConverter(), commandConfig.referenceExtractor(),
				commandConfig.referenceResolver());
	}

	@Bean
	public ResearchStudyService researchStudyService()
	{
		return new ResearchStudyServiceJaxrs(
				new ResearchStudyServiceSecure(researchStudyServiceImpl(), helperConfig.responseGenerator()));
	}

	private ResearchStudyServiceImpl researchStudyServiceImpl()
	{
		return new ResearchStudyServiceImpl(resourceTypeName(ResearchStudy.class), serverBase,
				ResearchStudyServiceJaxrs.PATH, defaultPageCount, daoConfig.researchStudyDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				commandConfig.referenceExtractor(), commandConfig.referenceResolver());
	}

	@Bean
	public StructureDefinitionService structureDefinitionService()
	{
		return new StructureDefinitionServiceJaxrs(new StructureDefinitionServiceSecure(
				structureDefinitionServiceImpl(), helperConfig.responseGenerator()));
	}

	private StructureDefinitionServiceImpl structureDefinitionServiceImpl()
	{
		return new StructureDefinitionServiceImpl(resourceTypeName(StructureDefinition.class), serverBase,
				StructureDefinitionServiceJaxrs.PATH, defaultPageCount, daoConfig.structureDefinitionDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				daoConfig.structureDefinitionSnapshotDao(), snapshotConfig.snapshotGenerator(),
				snapshotConfig.snapshotDependencyAnalyzer(), commandConfig.referenceExtractor(),
				commandConfig.referenceResolver());
	}

	@Bean
	public SubscriptionService subscriptionService()
	{
		return new SubscriptionServiceJaxrs(
				new SubscriptionServiceSecure(subscriptionServiceImpl(), helperConfig.responseGenerator()));
	}

	private SubscriptionServiceImpl subscriptionServiceImpl()
	{
		return new SubscriptionServiceImpl(resourceTypeName(Subscription.class), serverBase,
				SubscriptionServiceJaxrs.PATH, defaultPageCount, daoConfig.subscriptionDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				commandConfig.referenceExtractor(), commandConfig.referenceResolver());
	}

	@Bean
	public TaskService taskService()
	{
		return new TaskServiceJaxrs(new TaskServiceSecure(taskServiceImpl(), helperConfig.responseGenerator()));
	}

	private TaskServiceImpl taskServiceImpl()
	{
		return new TaskServiceImpl(resourceTypeName(Task.class), serverBase, TaskServiceJaxrs.PATH, defaultPageCount,
				daoConfig.taskDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), commandConfig.referenceExtractor(),
				commandConfig.referenceResolver());
	}

	@Bean
	public ValueSetService valueSetService()
	{
		return new ValueSetServiceJaxrs(
				new ValueSetServiceSecure(valueSetServiceImpl(), helperConfig.responseGenerator()));
	}

	private ValueSetServiceImpl valueSetServiceImpl()
	{
		return new ValueSetServiceImpl(resourceTypeName(ValueSet.class), serverBase, ValueSetServiceJaxrs.PATH,
				defaultPageCount, daoConfig.valueSetDao(), validationConfig.resourceValidator(),
				eventConfig.eventManager(), helperConfig.exceptionHandler(), eventConfig.eventGenerator(),
				helperConfig.responseGenerator(), helperConfig.parameterConverter(), commandConfig.referenceExtractor(),
				commandConfig.referenceResolver());
	}

	@Bean
	public RootService rootService()
	{
		return new RootServiceJaxrs(new RootServiceSecure(rootServiceImpl()));
	}

	private RootServiceImpl rootServiceImpl()
	{
		return new RootServiceImpl(commandConfig.commandFactory(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), helperConfig.exceptionHandler());
	}
}
