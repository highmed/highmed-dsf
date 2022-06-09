package org.highmed.dsf.fhir.spring.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventHandler;
import org.highmed.dsf.fhir.event.EventManager;
import org.highmed.dsf.fhir.event.EventManagerImpl;
import org.highmed.dsf.fhir.subscription.MatcherFactory;
import org.highmed.dsf.fhir.subscription.WebSocketSubscriptionManager;
import org.highmed.dsf.fhir.subscription.WebSocketSubscriptionManagerImpl;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventConfig
{
	@Autowired
	private DaoConfig daoConfig;

	@Autowired
	private HelperConfig helperConfig;

	@Autowired
	private FhirConfig fhirConfig;

	@Autowired
	private AuthorizationConfig authorizationConfig;

	@Autowired
	private ValidationConfig validationConfig;

	@Bean
	public MatcherFactory matcherFactory()
	{
		Map<String, ResourceDao<? extends Resource>> daosByResourceName = new HashMap<>();

		put(daosByResourceName, daoConfig.binaryDao());
		put(daosByResourceName, daoConfig.bundleDao());
		put(daosByResourceName, daoConfig.codeSystemDao());
		put(daosByResourceName, daoConfig.documentReferenceDao());
		put(daosByResourceName, daoConfig.endpointDao());
		put(daosByResourceName, daoConfig.groupDao());
		put(daosByResourceName, daoConfig.healthcareServiceDao());
		put(daosByResourceName, daoConfig.libraryDao());
		put(daosByResourceName, daoConfig.locationDao());
		put(daosByResourceName, daoConfig.measureDao());
		put(daosByResourceName, daoConfig.measureReportDao());
		put(daosByResourceName, daoConfig.namingSystemDao());
		put(daosByResourceName, daoConfig.organizationAffiliationDao());
		put(daosByResourceName, daoConfig.organizationDao());
		put(daosByResourceName, daoConfig.patientDao());
		put(daosByResourceName, daoConfig.practitionerDao());
		put(daosByResourceName, daoConfig.practitionerRoleDao());
		put(daosByResourceName, daoConfig.provenanceDao());
		put(daosByResourceName, daoConfig.questionnaireDao());
		put(daosByResourceName, daoConfig.questionnaireResponseDao());
		put(daosByResourceName, daoConfig.researchStudyDao());
		put(daosByResourceName, daoConfig.structureDefinitionDao());
		put(daosByResourceName, daoConfig.subscriptionDao());
		put(daosByResourceName, daoConfig.taskDao());
		put(daosByResourceName, daoConfig.valueSetDao());

		return new MatcherFactory(daosByResourceName);
	}

	private void put(Map<String, ? super ResourceDao<? extends Resource>> daosByResourceName,
			ResourceDao<? extends Resource> dao)
	{
		daosByResourceName.put(dao.getResourceTypeName(), dao);
	}

	@Bean
	public EventManager eventManager()
	{
		List<EventHandler> eventHandlers = Stream
				.of(validationConfig.validationSupport(), webSocketSubscriptionManager())
				.filter(o -> o instanceof EventHandler).map(o -> (EventHandler) o).collect(Collectors.toList());

		return new EventManagerImpl(eventHandlers);
	}

	@Bean
	public WebSocketSubscriptionManager webSocketSubscriptionManager()
	{
		return new WebSocketSubscriptionManagerImpl(daoConfig.daoProvider(), helperConfig.exceptionHandler(),
				matcherFactory(), fhirConfig.fhirContext(), authorizationConfig.authorizationRuleProvider());
	}

	@Bean
	public EventGenerator eventGenerator()
	{
		return new EventGenerator();
	}
}
