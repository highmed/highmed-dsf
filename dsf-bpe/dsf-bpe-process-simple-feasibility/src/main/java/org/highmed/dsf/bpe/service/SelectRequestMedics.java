package org.highmed.dsf.bpe.service;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetsValues;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;
import java.util.stream.Collectors;

public class SelectRequestMedics extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectRequestMedics.class);

	private final OrganizationProvider organizationProvider;

	public SelectRequestMedics(OrganizationProvider organizationProvider, WebserviceClient webserviceClient)
	{
		super(webserviceClient);
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	public void executeService(DelegateExecution execution) throws Exception
	{
		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(Constants.VARIABLE_RESEARCH_STUDY);

		List<String> targetReferences = researchStudy.getExtension().stream()
				.filter(e -> e.getUrl().equals(Constants.EXTENSION_PARTICIPATING_MEDIC_URI))
				.map(e -> ((Reference) e.getValue()).getReference()).collect(Collectors.toList());

		List<MultiInstanceTarget> targets = targetReferences.stream()
				.map(r -> new MultiInstanceTarget(organizationProvider.getIdentifier(new IdType(r))
						// Is only called if organization is deleted after research study is generated.
						// Normally generating research studies with non existing references are caught by resource validation.
						.orElseThrow(() -> new ResourceNotFoundException("Could not find organization reference: " + r))
						.getValue(), UUID.randomUUID().toString())).collect(Collectors.toList());

		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_TARGETS,
				MultiInstanceTargetsValues.create(new MultiInstanceTargets(targets)));

		// TODO: change to multiinstance for multiple queries in research study
		execution.setVariable(Constants.VARIABLE_COHORT_SIZE_RESULTS, new ArrayList<Integer>());
	}
}
