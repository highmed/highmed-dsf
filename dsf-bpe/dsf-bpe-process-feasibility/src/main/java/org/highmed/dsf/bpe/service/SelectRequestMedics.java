package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.MultiInstanceResults;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetsValues;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;

public class SelectRequestMedics extends AbstractServiceDelegate
{
	private final OrganizationProvider organizationProvider;

	public SelectRequestMedics(OrganizationProvider organizationProvider, FhirWebserviceClientProvider clientProvider,
			TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
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
				.filter(extension -> extension.getUrl().equals(Constants.EXTENSION_PARTICIPATING_MEDIC_URI))
				.map(extension -> ((Reference) extension.getValue()).getReference()).collect(Collectors.toList());

		List<MultiInstanceTarget> targets = targetReferences.stream()
				.flatMap(reference -> organizationProvider.getIdentifier(new IdType(reference)).stream())
				.map(identifier -> new MultiInstanceTarget(identifier.getValue(), UUID.randomUUID().toString()))
				.collect(Collectors.toList());

		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_TARGETS,
				MultiInstanceTargetsValues.create(new MultiInstanceTargets(targets)));

		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULTS, new MultiInstanceResults());
	}
}
