package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.bpe.variables.MultiInstanceResults;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetsValues;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

public class SelectRequestMedics extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(SelectRequestMedics.class);

	private final OrganizationProvider organizationProvider;

	public SelectRequestMedics(OrganizationProvider organizationProvider, FhirWebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
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

		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULTS, new MultiInstanceResults());
	}
}
