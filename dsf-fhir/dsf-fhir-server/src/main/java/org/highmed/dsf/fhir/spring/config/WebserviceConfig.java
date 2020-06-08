package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.adapter.HtmlFhirAdapter.ServerBaseProvider;
import org.highmed.dsf.fhir.exception.DataFormatExceptionHandler;
import org.highmed.dsf.fhir.webservice.impl.ActivityDefinitionServiceImpl;
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
import org.highmed.dsf.fhir.webservice.impl.StaticResourcesServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.StructureDefinitionServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.SubscriptionServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.TaskServiceImpl;
import org.highmed.dsf.fhir.webservice.impl.ValueSetServiceImpl;
import org.highmed.dsf.fhir.webservice.jaxrs.ActivityDefinitionServiceJaxrs;
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
import org.highmed.dsf.fhir.webservice.jaxrs.StaticResourcesServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.StructureDefinitionServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.SubscriptionServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.TaskServiceJaxrs;
import org.highmed.dsf.fhir.webservice.jaxrs.ValueSetServiceJaxrs;
import org.highmed.dsf.fhir.webservice.secure.ActivityDefinitionServiceSecure;
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
import org.highmed.dsf.fhir.webservice.secure.StaticResourcesServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.StructureDefinitionServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.SubscriptionServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.TaskServiceSecure;
import org.highmed.dsf.fhir.webservice.secure.ValueSetServiceSecure;
import org.highmed.dsf.fhir.webservice.specification.ActivityDefinitionService;
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
import org.highmed.dsf.fhir.webservice.specification.StaticResourcesService;
import org.highmed.dsf.fhir.webservice.specification.StructureDefinitionService;
import org.highmed.dsf.fhir.webservice.specification.SubscriptionService;
import org.highmed.dsf.fhir.webservice.specification.TaskService;
import org.highmed.dsf.fhir.webservice.specification.ValueSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

	@Autowired
	private AuthorizationConfig authorizationConfig;

	@Autowired
	private ReferenceConfig referenceConfig;

	@Bean
	public ServerBaseProvider serverBaseProvider()
	{
		return () -> serverBase;
	}

	@Bean
	public DataFormatExceptionHandler dataFormatExceptionHandler()
	{
		return new DataFormatExceptionHandler(helperConfig.responseGenerator());
	}

	@Bean
	public ActivityDefinitionService activityDefinitionService()
	{
		return new ActivityDefinitionServiceJaxrs(activityDefinitionServiceSecure());
	}

	private ActivityDefinitionServiceSecure activityDefinitionServiceSecure()
	{
		return new ActivityDefinitionServiceSecure(activityDefinitionServiceImpl(), serverBase,
				helperConfig.responseGenerator(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), daoConfig.activityDefinitionDao(), helperConfig.exceptionHandler(),
				helperConfig.parameterConverter(), authorizationConfig.activityDefinitionAuthorizationRule(),
				validationConfig.resourceValidator());
	}

	private ActivityDefinitionServiceImpl activityDefinitionServiceImpl()
	{
		return new ActivityDefinitionServiceImpl(ActivityDefinitionServiceJaxrs.PATH, serverBase, defaultPageCount,
				daoConfig.activityDefinitionDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), referenceConfig.referenceExtractor(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(),
				authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public BinaryService binaryService()
	{
		return new BinaryServiceJaxrs(binaryServiceSecure());
	}

	private BinaryServiceSecure binaryServiceSecure()
	{
		return new BinaryServiceSecure(binaryServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.binaryDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.binaryAuthorizationRule(), validationConfig.resourceValidator());
	}

	private BinaryService binaryServiceImpl()
	{
		return new BinaryServiceImpl(BinaryServiceJaxrs.PATH, serverBase, defaultPageCount, daoConfig.binaryDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				referenceConfig.referenceExtractor(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public BundleService bundleService()
	{
		return new BundleServiceJaxrs(bundleServiceSecure());
	}

	private BundleServiceSecure bundleServiceSecure()
	{
		return new BundleServiceSecure(bundleServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.bundleDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.bundleAuthorizationRule(), validationConfig.resourceValidator());
	}

	private BundleService bundleServiceImpl()
	{
		return new BundleServiceImpl(BundleServiceJaxrs.PATH, serverBase, defaultPageCount, daoConfig.bundleDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				referenceConfig.referenceExtractor(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public CodeSystemService codeSystemService()
	{
		return new CodeSystemServiceJaxrs(codeSystemServiceSecure());
	}

	private CodeSystemServiceSecure codeSystemServiceSecure()
	{
		return new CodeSystemServiceSecure(codeSystemServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.codeSystemDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.codeSystemAuthorizationRule(), validationConfig.resourceValidator());
	}

	private CodeSystemServiceImpl codeSystemServiceImpl()
	{
		return new CodeSystemServiceImpl(CodeSystemServiceJaxrs.PATH, serverBase, defaultPageCount,
				daoConfig.codeSystemDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), referenceConfig.referenceExtractor(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(),
				authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public EndpointService endpointService()
	{
		return new EndpointServiceJaxrs(endpointServiceSecure());
	}

	private EndpointServiceSecure endpointServiceSecure()
	{
		return new EndpointServiceSecure(endpointServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.endpointDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.endpointAuthorizationRule(), validationConfig.resourceValidator());
	}

	private EndpointServiceImpl endpointServiceImpl()
	{
		return new EndpointServiceImpl(EndpointServiceJaxrs.PATH, serverBase, defaultPageCount, daoConfig.endpointDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				referenceConfig.referenceExtractor(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public GroupService groupService()
	{
		return new GroupServiceJaxrs(groupServiceSecure());
	}

	private GroupServiceSecure groupServiceSecure()
	{
		return new GroupServiceSecure(groupServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.groupDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.groupAuthorizationRule(), validationConfig.resourceValidator());
	}

	private GroupServiceImpl groupServiceImpl()
	{
		return new GroupServiceImpl(GroupServiceJaxrs.PATH, serverBase, defaultPageCount, daoConfig.groupDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				referenceConfig.referenceExtractor(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public HealthcareServiceService healthcareServiceService()
	{
		return new HealthcareServiceServiceJaxrs(healthcareServiceServiceSecure());
	}

	private HealthcareServiceServiceSecure healthcareServiceServiceSecure()
	{
		return new HealthcareServiceServiceSecure(healthcareServiceServiceImpl(), serverBase,
				helperConfig.responseGenerator(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), daoConfig.healthcareServiceDao(), helperConfig.exceptionHandler(),
				helperConfig.parameterConverter(), authorizationConfig.healthcareServiceAuthorizationRule(),
				validationConfig.resourceValidator());
	}

	private HealthcareServiceServiceImpl healthcareServiceServiceImpl()
	{
		return new HealthcareServiceServiceImpl(HealthcareServiceServiceJaxrs.PATH, serverBase, defaultPageCount,
				daoConfig.healthcareServiceDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), referenceConfig.referenceExtractor(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(),
				authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public LocationService locationService()
	{
		return new LocationServiceJaxrs(locationServiceSecure());
	}

	private LocationServiceSecure locationServiceSecure()
	{
		return new LocationServiceSecure(locationServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.locationDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.locationAuthorizationRule(), validationConfig.resourceValidator());
	}

	private LocationServiceImpl locationServiceImpl()
	{
		return new LocationServiceImpl(LocationServiceJaxrs.PATH, serverBase, defaultPageCount, daoConfig.locationDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				referenceConfig.referenceExtractor(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public NamingSystemService namingSystemService()
	{
		return new NamingSystemServiceJaxrs(namingSystemServiceSecure());
	}

	private NamingSystemServiceSecure namingSystemServiceSecure()
	{
		return new NamingSystemServiceSecure(namingSystemServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.namingSystemDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.namingSystemAuthorizationRule(), validationConfig.resourceValidator());
	}

	private NamingSystemService namingSystemServiceImpl()
	{
		return new NamingSystemServiceImpl(LocationServiceJaxrs.PATH, serverBase, defaultPageCount,
				daoConfig.namingSystemDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), referenceConfig.referenceExtractor(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(),
				authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public OrganizationService organizationService()
	{
		return new OrganizationServiceJaxrs(organizationServiceSecure());
	}

	private OrganizationServiceSecure organizationServiceSecure()
	{
		return new OrganizationServiceSecure(organizationServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.organizationDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.organizationAuthorizationRule(), validationConfig.resourceValidator());
	}

	private OrganizationServiceImpl organizationServiceImpl()
	{
		return new OrganizationServiceImpl(OrganizationServiceJaxrs.PATH, serverBase, defaultPageCount,
				daoConfig.organizationDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), referenceConfig.referenceExtractor(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(),
				authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public PatientService patientService()
	{
		return new PatientServiceJaxrs(patientServiceSecure());
	}

	private PatientServiceSecure patientServiceSecure()
	{
		return new PatientServiceSecure(patientServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.patientDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.patientAuthorizationRule(), validationConfig.resourceValidator());
	}

	private PatientServiceImpl patientServiceImpl()
	{
		return new PatientServiceImpl(PatientServiceJaxrs.PATH, serverBase, defaultPageCount, daoConfig.patientDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				referenceConfig.referenceExtractor(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public PractitionerRoleService practitionerRoleService()
	{
		return new PractitionerRoleServiceJaxrs(practitionerRoleServiceSecure());
	}

	private PractitionerRoleServiceSecure practitionerRoleServiceSecure()
	{
		return new PractitionerRoleServiceSecure(practitionerRoleServiceImpl(), serverBase,
				helperConfig.responseGenerator(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), daoConfig.practitionerRoleDao(), helperConfig.exceptionHandler(),
				helperConfig.parameterConverter(), authorizationConfig.practitionerRoleAuthorizationRule(),
				validationConfig.resourceValidator());
	}

	private PractitionerRoleServiceImpl practitionerRoleServiceImpl()
	{
		return new PractitionerRoleServiceImpl(PractitionerRoleServiceJaxrs.PATH, serverBase, defaultPageCount,
				daoConfig.practitionerRoleDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), referenceConfig.referenceExtractor(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(),
				authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public PractitionerService practitionerService()
	{
		return new PractitionerServiceJaxrs(practitionerServiceSecure());
	}

	private PractitionerServiceSecure practitionerServiceSecure()
	{
		return new PractitionerServiceSecure(practitionerServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.practitionerDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.practitionerAuthorizationRule(), validationConfig.resourceValidator());
	}

	private PractitionerServiceImpl practitionerServiceImpl()
	{
		return new PractitionerServiceImpl(PractitionerServiceJaxrs.PATH, serverBase, defaultPageCount,
				daoConfig.practitionerDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), referenceConfig.referenceExtractor(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(),
				authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public ProvenanceService provenanceService()
	{
		return new ProvenanceServiceJaxrs(provenanceServiceSecure());
	}

	private ProvenanceServiceSecure provenanceServiceSecure()
	{
		return new ProvenanceServiceSecure(provenanceServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.provenanceDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.provenanceAuthorizationRule(), validationConfig.resourceValidator());
	}

	private ProvenanceServiceImpl provenanceServiceImpl()
	{
		return new ProvenanceServiceImpl(ProvenanceServiceJaxrs.PATH, serverBase, defaultPageCount,
				daoConfig.provenanceDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), referenceConfig.referenceExtractor(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(),
				authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public ResearchStudyService researchStudyService()
	{
		return new ResearchStudyServiceJaxrs(researchStudyServiceSecure());
	}

	private ResearchStudyServiceSecure researchStudyServiceSecure()
	{
		return new ResearchStudyServiceSecure(researchStudyServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.researchStudyDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.researchStudyAuthorizationRule(), validationConfig.resourceValidator());
	}

	private ResearchStudyServiceImpl researchStudyServiceImpl()
	{
		return new ResearchStudyServiceImpl(ResearchStudyServiceJaxrs.PATH, serverBase, defaultPageCount,
				daoConfig.researchStudyDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), referenceConfig.referenceExtractor(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(),
				authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public StructureDefinitionService structureDefinitionService()
	{
		return new StructureDefinitionServiceJaxrs(structureDefinitionServiceSecure());
	}

	private StructureDefinitionServiceSecure structureDefinitionServiceSecure()
	{
		return new StructureDefinitionServiceSecure(structureDefinitionServiceImpl(), serverBase,
				helperConfig.responseGenerator(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), daoConfig.structureDefinitionDao(), helperConfig.exceptionHandler(),
				helperConfig.parameterConverter(), authorizationConfig.structureDefinitionAuthorizationRule(),
				validationConfig.resourceValidator());
	}

	private StructureDefinitionServiceImpl structureDefinitionServiceImpl()
	{
		return new StructureDefinitionServiceImpl(StructureDefinitionServiceJaxrs.PATH, serverBase, defaultPageCount,
				daoConfig.structureDefinitionDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), referenceConfig.referenceExtractor(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(),
				authorizationConfig.authorizationRuleProvider(), daoConfig.structureDefinitionSnapshotDao(),
				snapshotConfig.snapshotGenerator());
	}

	@Bean
	public SubscriptionService subscriptionService()
	{
		return new SubscriptionServiceJaxrs(subscriptionServiceSecure());
	}

	private SubscriptionServiceSecure subscriptionServiceSecure()
	{
		return new SubscriptionServiceSecure(subscriptionServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.subscriptionDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.subscriptionAuthorizationRule(), validationConfig.resourceValidator());
	}

	private SubscriptionServiceImpl subscriptionServiceImpl()
	{
		return new SubscriptionServiceImpl(SubscriptionServiceJaxrs.PATH, serverBase, defaultPageCount,
				daoConfig.subscriptionDao(), validationConfig.resourceValidator(), eventConfig.eventManager(),
				helperConfig.exceptionHandler(), eventConfig.eventGenerator(), helperConfig.responseGenerator(),
				helperConfig.parameterConverter(), referenceConfig.referenceExtractor(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(),
				authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public TaskService taskService()
	{
		return new TaskServiceJaxrs(taskServiceSecure());
	}

	private TaskServiceSecure taskServiceSecure()
	{
		return new TaskServiceSecure(taskServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.taskDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.taskAuthorizationRule(), validationConfig.resourceValidator());
	}

	private TaskServiceImpl taskServiceImpl()
	{
		return new TaskServiceImpl(TaskServiceJaxrs.PATH, serverBase, defaultPageCount, daoConfig.taskDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				referenceConfig.referenceExtractor(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public ValueSetService valueSetService()
	{
		return new ValueSetServiceJaxrs(valueSetServiceSecure());
	}

	private ValueSetServiceSecure valueSetServiceSecure()
	{
		return new ValueSetServiceSecure(valueSetServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(), daoConfig.valueSetDao(),
				helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				authorizationConfig.valueSetAuthorizationRule(), validationConfig.resourceValidator());
	}

	private ValueSetServiceImpl valueSetServiceImpl()
	{
		return new ValueSetServiceImpl(ValueSetServiceJaxrs.PATH, serverBase, defaultPageCount, daoConfig.valueSetDao(),
				validationConfig.resourceValidator(), eventConfig.eventManager(), helperConfig.exceptionHandler(),
				eventConfig.eventGenerator(), helperConfig.responseGenerator(), helperConfig.parameterConverter(),
				referenceConfig.referenceExtractor(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public RootService rootService()
	{
		return new RootServiceJaxrs(rootServiceSecure());
	}

	private RootServiceSecure rootServiceSecure()
	{
		return new RootServiceSecure(rootServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver());
	}

	private RootServiceImpl rootServiceImpl()
	{
		return new RootServiceImpl(RootServiceJaxrs.PATH, commandConfig.commandFactory(),
				helperConfig.responseGenerator(), helperConfig.parameterConverter(), helperConfig.exceptionHandler(),
				referenceConfig.referenceCleaner());
	}

	@Bean
	public ConformanceService conformanceService()
	{
		return new ConformanceServiceJaxrs(conformanceServiceSecure());
	}

	private ConformanceServiceSecure conformanceServiceSecure()
	{
		return new ConformanceServiceSecure(conformanceServiceImpl(), serverBase, helperConfig.responseGenerator(),
				referenceConfig.referenceResolver());
	}

	private ConformanceServiceImpl conformanceServiceImpl()
	{
		return new ConformanceServiceImpl(ConformanceServiceJaxrs.PATH, serverBase, defaultPageCount,
				buildInfoReaderConfig.buildInfoReader(), helperConfig.parameterConverter());
	}

	@Bean
	public StaticResourcesService staticResourcesService()
	{
		return new StaticResourcesServiceJaxrs(staticResourcesServiceSecure());
	}

	private StaticResourcesServiceSecure staticResourcesServiceSecure()
	{
		return new StaticResourcesServiceSecure(staticResourcesServiceImpl(), serverBase,
				helperConfig.responseGenerator(), referenceConfig.referenceResolver());
	}

	private StaticResourcesServiceImpl staticResourcesServiceImpl()
	{
		return new StaticResourcesServiceImpl(StaticResourcesServiceJaxrs.PATH);
	}
}
