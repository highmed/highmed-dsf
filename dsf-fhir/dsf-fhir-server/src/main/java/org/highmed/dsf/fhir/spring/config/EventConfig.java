package org.highmed.dsf.fhir.spring.config;

import java.util.HashMap;
import java.util.Map;

import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventManager;
import org.highmed.dsf.fhir.event.EventManagerImpl;
import org.highmed.dsf.fhir.event.MatcherFactory;
import org.hl7.fhir.r4.model.DomainResource;
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

	@Bean
	public MatcherFactory matcherFactory()
	{
		Map<String, ResourceDao<? extends DomainResource>> daosByResourceName = new HashMap<>();

		put(daosByResourceName, daoConfig.codeSystemDao());
		put(daosByResourceName, daoConfig.healthcareServiceDao());
		put(daosByResourceName, daoConfig.locationDao());
		put(daosByResourceName, daoConfig.organizationDao());
		put(daosByResourceName, daoConfig.patientDao());
		put(daosByResourceName, daoConfig.practitionerDao());
		put(daosByResourceName, daoConfig.practitionerRoleDao());
		put(daosByResourceName, daoConfig.provenanceDao());
		put(daosByResourceName, daoConfig.researchStudyDao());
		put(daosByResourceName, daoConfig.structureDefinitionDao());
		put(daosByResourceName, daoConfig.subscriptionDao());
		put(daosByResourceName, daoConfig.taskDao());
		put(daosByResourceName, daoConfig.valueSetDao());

		return new MatcherFactory(daosByResourceName);
	}

	private void put(Map<String, ? super ResourceDao<? extends DomainResource>> daosByResourceName,
			ResourceDao<? extends DomainResource> dao)
	{
		daosByResourceName.put(dao.getResourceTypeName(), dao);
	}

	@Bean
	public EventManager eventManager()
	{
		return new EventManagerImpl(daoConfig.daoProvider(), helperConfig.exceptionHandler(), matcherFactory(),
				fhirConfig.fhirContext());
	}

	@Bean
	public EventGenerator eventGenerator()
	{
		return new EventGenerator();
	}
}
