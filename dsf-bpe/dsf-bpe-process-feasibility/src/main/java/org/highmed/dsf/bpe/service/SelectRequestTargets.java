package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetValues;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetsValues;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;

public class SelectRequestTargets extends AbstractServiceDelegate
{
	private final OrganizationProvider organizationProvider;

	public SelectRequestTargets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider)
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
		setMedicTargets(execution);
		setTtpTarget(execution);
	}

	private void setMedicTargets(DelegateExecution execution)
	{
		Stream<Identifier> identifiers = organizationProvider.getOrganizationsByType("MeDIC")
				.map(medic -> medic.getIdentifier().stream().filter(identifier -> identifier.getSystem()
						.equals(organizationProvider.getDefaultIdentifierSystem())).findFirst().get());

		List<MultiInstanceTarget> targets = identifiers
				.map(identifier -> new MultiInstanceTarget(identifier.getValue(), UUID.randomUUID().toString()))
				.collect(Collectors.toList());

		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_TARGETS,
				MultiInstanceTargetsValues.create(new MultiInstanceTargets(targets)));
	}

	private void setTtpTarget(DelegateExecution execution)
	{
		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(Constants.VARIABLE_RESEARCH_STUDY);
		Reference ttpReference = (Reference) researchStudy.getExtension().stream()
				.filter(extension -> extension.getUrl()
						.equals("http://highmed.org/fhir/StructureDefinition/participating-ttp")).findFirst().get()
				.getValue();

		Identifier ttpIdentifier = organizationProvider.getIdentifier(new IdType(ttpReference.getReference())).get();

		MultiInstanceTarget ttpTarget = new MultiInstanceTarget(ttpIdentifier.getValue(), UUID.randomUUID().toString());
		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_TARGET, MultiInstanceTargetValues.create(ttpTarget));
	}
}
