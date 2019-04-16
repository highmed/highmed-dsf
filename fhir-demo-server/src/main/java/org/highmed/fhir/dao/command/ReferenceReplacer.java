package org.highmed.fhir.dao.command;

import java.util.List;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Patient.ContactComponent;
import org.hl7.fhir.r4.model.Patient.PatientLinkComponent;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Practitioner.PractitionerQualificationComponent;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.r4.model.Provenance.ProvenanceEntityComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;

public class ReferenceReplacer
{
	public void setReference(DomainResource resource, Class<? extends DomainResource> referenceTarget,
			String oldReferenceValue, String newReferenceValue)
	{
		if (resource instanceof Endpoint)
			setReference((Endpoint) resource, referenceTarget, oldReferenceValue, newReferenceValue);
		else if (resource instanceof HealthcareService)
			setReference((HealthcareService) resource, referenceTarget, oldReferenceValue, newReferenceValue);
		else if (resource instanceof Location)
			setReference((Location) resource, referenceTarget, oldReferenceValue, newReferenceValue);
		else if (resource instanceof Organization)
			setReference((Organization) resource, referenceTarget, oldReferenceValue, newReferenceValue);
		else if (resource instanceof Patient)
			setReference((Patient) resource, referenceTarget, oldReferenceValue, newReferenceValue);
		else if (resource instanceof Practitioner)
			setReference((Practitioner) resource, referenceTarget, oldReferenceValue, newReferenceValue);
		else if (resource instanceof PractitionerRole)
			setReference((PractitionerRole) resource, referenceTarget, oldReferenceValue, newReferenceValue);
		else if (resource instanceof Provenance)
			setReference((Provenance) resource, referenceTarget, oldReferenceValue, newReferenceValue);
		else if (resource instanceof ResearchStudy)
			setReference((ResearchStudy) resource, referenceTarget, oldReferenceValue, newReferenceValue);
		else if (resource instanceof Task)
			setReference((Task) resource, referenceTarget, oldReferenceValue, newReferenceValue);

		// TODO check referenceTarget for "reference"-extensions
		setReferences(resource.getExtension().stream().filter(e -> e.getValue() instanceof Reference)
				.map(e -> (Reference) e.getValue()), oldReferenceValue, newReferenceValue);
	}

	private void setReference(Reference reference, String oldReferenceValue, String newReferenceValue)
	{
		if (oldReferenceValue.equals(reference.getReference()))
			reference.setReference(newReferenceValue);
	}

	private void setReferences(List<Reference> references, String oldReferenceValue, String newReferenceValue)
	{
		setReferences(references.stream(), oldReferenceValue, newReferenceValue);
	}

	private void setReferences(Stream<Reference> references, String oldReferenceValue, String newReferenceValue)
	{
		references.filter(ref -> oldReferenceValue.equals(ref.getReference()))
				.forEach(ref -> ref.setReference(newReferenceValue));
	}

	private void setReference(Endpoint resource, Class<? extends DomainResource> referenceTarget,
			String oldReferenceValue, String newReferenceValue)
	{
		if (Organization.class.equals(referenceTarget))
		{
			setReference(resource.getManagingOrganization(), oldReferenceValue, newReferenceValue);
		}
	}

	private void setReference(HealthcareService resource, Class<? extends DomainResource> referenceTarget,
			String oldReferenceValue, String newReferenceValue)
	{
		if (Organization.class.equals(referenceTarget))
		{
			setReference(resource.getProvidedBy(), oldReferenceValue, newReferenceValue);
		}
		else if (Location.class.equals(referenceTarget))
		{
			setReferences(resource.getLocation(), oldReferenceValue, newReferenceValue);
			setReferences(resource.getCoverageArea(), oldReferenceValue, newReferenceValue);
		}
		else if (Endpoint.class.equals(referenceTarget))
		{
			setReferences(resource.getEndpoint(), oldReferenceValue, newReferenceValue);
		}
	}

	private void setReference(Location resource, Class<? extends DomainResource> referenceTarget,
			String oldReferenceValue, String newReferenceValue)
	{
		if (Organization.class.equals(referenceTarget))
		{
			setReference(resource.getManagingOrganization(), oldReferenceValue, newReferenceValue);
		}
		else if (Location.class.equals(referenceTarget))
		{
			setReference(resource.getPartOf(), oldReferenceValue, newReferenceValue);
		}
		else if (Endpoint.class.equals(referenceTarget))
		{
			setReferences(resource.getEndpoint(), oldReferenceValue, newReferenceValue);
		}
	}

	private void setReference(Organization resource, Class<? extends DomainResource> referenceTarget,
			String oldReferenceValue, String newReferenceValue)
	{
		if (Organization.class.equals(referenceTarget))
		{
			setReference(resource.getPartOf(), oldReferenceValue, newReferenceValue);
		}
		else if (Endpoint.class.equals(referenceTarget))
		{
			setReferences(resource.getEndpoint(), oldReferenceValue, newReferenceValue);
		}
	}

	private void setReference(Patient resource, Class<? extends DomainResource> referenceTarget,
			String oldReferenceValue, String newReferenceValue)
	{
		if (Organization.class.equals(referenceTarget))
		{
			setReferences(resource.getContact().stream().map(ContactComponent::getOrganization), oldReferenceValue,
					newReferenceValue);
			setReferences(resource.getGeneralPractitioner(), oldReferenceValue, newReferenceValue);
			setReference(resource.getManagingOrganization(), oldReferenceValue, newReferenceValue);
		}
		else if (Practitioner.class.equals(referenceTarget))
		{
			setReferences(resource.getGeneralPractitioner(), oldReferenceValue, newReferenceValue);
		}
		else if (PractitionerRole.class.equals(referenceTarget))
		{
			setReferences(resource.getGeneralPractitioner(), oldReferenceValue, newReferenceValue);
		}
		else if (Patient.class.equals(referenceTarget))
		{
			setReferences(resource.getLink().stream().map(PatientLinkComponent::getOther), oldReferenceValue,
					newReferenceValue);
		}
		else if (RelatedPerson.class.equals(referenceTarget))
		{
			setReferences(resource.getLink().stream().map(PatientLinkComponent::getOther), oldReferenceValue,
					newReferenceValue);
		}
	}

	private void setReference(Practitioner resource, Class<? extends DomainResource> referenceTarget,
			String oldReferenceValue, String newReferenceValue)
	{
		if (Organization.class.equals(referenceTarget))
			setReferences(resource.getQualification().stream().map(PractitionerQualificationComponent::getIssuer),
					oldReferenceValue, newReferenceValue);
	}

	private void setReference(PractitionerRole resource, Class<? extends DomainResource> referenceTarget,
			String oldReferenceValue, String newReferenceValue)
	{
		if (Practitioner.class.equals(referenceTarget))
		{
			setReference(resource.getPractitioner(), oldReferenceValue, newReferenceValue);
		}
		else if (Organization.class.equals(referenceTarget))
		{
			setReference(resource.getOrganization(), oldReferenceValue, newReferenceValue);
		}
		else if (Location.class.equals(referenceTarget))
		{
			setReferences(resource.getLocation(), oldReferenceValue, newReferenceValue);
		}
		else if (HealthcareService.class.equals(referenceTarget))
		{
			setReferences(resource.getHealthcareService(), oldReferenceValue, newReferenceValue);
		}
		else if (Endpoint.class.equals(referenceTarget))
		{
			setReferences(resource.getEndpoint(), oldReferenceValue, newReferenceValue);
		}
	}

	private void setReference(Provenance resource, Class<? extends DomainResource> referenceTarget,
			String oldReferenceValue, String newReferenceValue)
	{
		if (Location.class.equals(referenceTarget))
		{
			setReference(resource.getLocation(), oldReferenceValue, newReferenceValue);
		}

		else if (Practitioner.class.equals(referenceTarget))
		{
			setReferences(resource.getAgent().stream().map(ProvenanceAgentComponent::getWho), oldReferenceValue,
					newReferenceValue);
			setReferences(resource.getAgent().stream().map(ProvenanceAgentComponent::getOnBehalfOf), oldReferenceValue,
					newReferenceValue);
		}
		else if (PractitionerRole.class.equals(referenceTarget))
		{
			setReferences(resource.getAgent().stream().map(ProvenanceAgentComponent::getWho), oldReferenceValue,
					newReferenceValue);
			setReferences(resource.getAgent().stream().map(ProvenanceAgentComponent::getOnBehalfOf), oldReferenceValue,
					newReferenceValue);
		}
		else if (RelatedPerson.class.equals(referenceTarget))
		{
			setReferences(resource.getAgent().stream().map(ProvenanceAgentComponent::getWho), oldReferenceValue,
					newReferenceValue);
			setReferences(resource.getAgent().stream().map(ProvenanceAgentComponent::getOnBehalfOf), oldReferenceValue,
					newReferenceValue);
		}
		else if (Patient.class.equals(referenceTarget))
		{
			setReferences(resource.getAgent().stream().map(ProvenanceAgentComponent::getWho), oldReferenceValue,
					newReferenceValue);
			setReferences(resource.getAgent().stream().map(ProvenanceAgentComponent::getOnBehalfOf), oldReferenceValue,
					newReferenceValue);
		}
		else if (Device.class.equals(referenceTarget))
		{
			setReferences(resource.getAgent().stream().map(ProvenanceAgentComponent::getWho), oldReferenceValue,
					newReferenceValue);
			setReferences(resource.getAgent().stream().map(ProvenanceAgentComponent::getOnBehalfOf), oldReferenceValue,
					newReferenceValue);
		}
		else if (Organization.class.equals(referenceTarget))
		{
			setReferences(resource.getAgent().stream().map(ProvenanceAgentComponent::getWho), oldReferenceValue,
					newReferenceValue);
			setReferences(resource.getAgent().stream().map(ProvenanceAgentComponent::getOnBehalfOf), oldReferenceValue,
					newReferenceValue);
		}

		setReferences(resource.getTarget(), oldReferenceValue, newReferenceValue);
		setReferences(resource.getEntity().stream().map(ProvenanceEntityComponent::getWhat), oldReferenceValue,
				newReferenceValue);
	}

	private void setReference(ResearchStudy resource, Class<? extends DomainResource> referenceTarget,
			String oldReferenceValue, String newReferenceValue)
	{
		if (PlanDefinition.class.equals(referenceTarget))
		{
			setReferences(resource.getProtocol(), oldReferenceValue, newReferenceValue);
		}
		else if (ResearchStudy.class.equals(referenceTarget))
		{
			setReferences(resource.getPartOf(), oldReferenceValue, newReferenceValue);
		}
		else if (Group.class.equals(referenceTarget))
		{
			setReferences(resource.getEnrollment(), oldReferenceValue, newReferenceValue);
		}
		else if (Organization.class.equals(referenceTarget))
		{
			setReference(resource.getSponsor(), oldReferenceValue, newReferenceValue);
		}
		else if (Practitioner.class.equals(referenceTarget))
		{
			setReference(resource.getPrincipalInvestigator(), oldReferenceValue, newReferenceValue);
		}
		else if (PractitionerRole.class.equals(referenceTarget))
		{
			setReference(resource.getPrincipalInvestigator(), oldReferenceValue, newReferenceValue);
		}
		else if (Location.class.equals(referenceTarget))
		{
			setReferences(resource.getSite(), oldReferenceValue, newReferenceValue);
		}
	}

	private void setReference(Task resource, Class<? extends DomainResource> referenceTarget, String oldReferenceValue,
			String newReferenceValue)
	{
		if (Task.class.equals(referenceTarget))
		{
			setReferences(resource.getPartOf(), oldReferenceValue, newReferenceValue);
		}
		else if (Encounter.class.equals(referenceTarget))
		{
			setReference(resource.getEncounter(), oldReferenceValue, newReferenceValue);
		}
		else if (Device.class.equals(referenceTarget))
		{
			setReference(resource.getRequester(), oldReferenceValue, newReferenceValue);
			setReference(resource.getOwner(), oldReferenceValue, newReferenceValue);
		}
		else if (Organization.class.equals(referenceTarget))
		{
			setReference(resource.getRequester(), oldReferenceValue, newReferenceValue);
			setReference(resource.getOwner(), oldReferenceValue, newReferenceValue);
			setReferences(resource.getRestriction().getRecipient(), oldReferenceValue, newReferenceValue);
		}
		else if (Patient.class.equals(referenceTarget))
		{
			setReference(resource.getRequester(), oldReferenceValue, newReferenceValue);
		}
		else if (Practitioner.class.equals(referenceTarget))
		{
			setReference(resource.getRequester(), oldReferenceValue, newReferenceValue);
			setReference(resource.getOwner(), oldReferenceValue, newReferenceValue);
			setReferences(resource.getRestriction().getRecipient(), oldReferenceValue, newReferenceValue);
		}
		else if (PractitionerRole.class.equals(referenceTarget))
		{
			setReference(resource.getRequester(), oldReferenceValue, newReferenceValue);
			setReference(resource.getOwner(), oldReferenceValue, newReferenceValue);
			setReferences(resource.getRestriction().getRecipient(), oldReferenceValue, newReferenceValue);
		}
		else if (RelatedPerson.class.equals(referenceTarget))
		{
			setReference(resource.getRequester(), oldReferenceValue, newReferenceValue);
			setReference(resource.getOwner(), oldReferenceValue, newReferenceValue);
			setReferences(resource.getRestriction().getRecipient(), oldReferenceValue, newReferenceValue);
		}
		else if (CareTeam.class.equals(referenceTarget))
		{
			setReference(resource.getOwner(), oldReferenceValue, newReferenceValue);
		}
		else if (HealthcareService.class.equals(referenceTarget))
		{
			setReference(resource.getOwner(), oldReferenceValue, newReferenceValue);
		}
		else if (Patient.class.equals(referenceTarget))
		{
			setReference(resource.getOwner(), oldReferenceValue, newReferenceValue);
			setReferences(resource.getRestriction().getRecipient(), oldReferenceValue, newReferenceValue);
		}
		else if (Location.class.equals(referenceTarget))
		{
			setReference(resource.getLocation(), oldReferenceValue, newReferenceValue);
		}
		else if (Coverage.class.equals(referenceTarget))
		{
			setReferences(resource.getInsurance(), oldReferenceValue, newReferenceValue);
		}
		else if (ClaimResponse.class.equals(referenceTarget))
		{
			setReferences(resource.getInsurance(), oldReferenceValue, newReferenceValue);
		}
		else if (Provenance.class.equals(referenceTarget))
		{
			setReferences(resource.getRelevantHistory(), oldReferenceValue, newReferenceValue);
		}
		else if (Group.class.equals(referenceTarget))
		{
			setReferences(resource.getRestriction().getRecipient(), oldReferenceValue, newReferenceValue);
		}

		setReferences(resource.getPartOf(), oldReferenceValue, newReferenceValue);
		setReference(resource.getFocus(), oldReferenceValue, newReferenceValue);
		setReference(resource.getFor(), oldReferenceValue, newReferenceValue);
		setReference(resource.getReasonReference(), oldReferenceValue, newReferenceValue);

		setReferences(resource.getInput().stream().filter(p -> p.getValue() instanceof Reference)
				.map(p -> (Reference) p.getValue()), oldReferenceValue, newReferenceValue);
		setReferences(resource.getOutput().stream().filter(p -> p.getValue() instanceof Reference)
				.map(p -> (Reference) p.getValue()), oldReferenceValue, newReferenceValue);
	}
}
