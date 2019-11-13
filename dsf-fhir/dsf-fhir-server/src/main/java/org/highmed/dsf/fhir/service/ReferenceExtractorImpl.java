package org.highmed.dsf.fhir.service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.dao.command.ResourceReference;
import org.hl7.fhir.r4.model.BackboneElement;
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
import org.hl7.fhir.r4.model.Medication;
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
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Substance;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceExtractorImpl implements ReferenceExtractor
{
	private static final Logger logger = LoggerFactory.getLogger(ReferenceExtractorImpl.class);

	@SafeVarargs
	private Function<Reference, ResourceReference> toResourceReference(String referenceLocation,
			Class<? extends Resource>... referenceTypes)
	{
		return ref -> new ResourceReference(referenceLocation, ref, Arrays.asList(referenceTypes));
	}

	@SafeVarargs
	private <R extends Resource> Stream<ResourceReference> getReference(R resource, Predicate<R> hasReference,
			Function<R, Reference> getReference, String referenceLocation,
			Class<? extends DomainResource>... referenceTypes)
	{
		return hasReference.test(resource)
				? Stream.of(getReference.apply(resource)).map(toResourceReference(referenceLocation, referenceTypes))
				: Stream.empty();
	}

	@SafeVarargs
	private <R extends Resource> Stream<ResourceReference> getReferences(R resource, Predicate<R> hasReference,
			Function<R, List<Reference>> getReference, String referenceLocation,
			Class<? extends Resource>... referenceTypes)
	{
		return hasReference.test(resource) ? Stream.of(getReference.apply(resource)).flatMap(List::stream)
				.map(toResourceReference(referenceLocation, referenceTypes)) : Stream.empty();
	}

	@SafeVarargs
	private <R extends Resource, E extends BackboneElement> Stream<ResourceReference> getBackboneElementsReference(
			R resource, Predicate<R> hasBackboneElements, Function<R, List<E>> getBackboneElements,
			Predicate<E> hasReference, Function<E, Reference> getReference, String referenceLocation,
			Class<? extends Resource>... referenceTypes)
	{
		if (hasBackboneElements.test(resource))
		{
			List<E> backboneElements = getBackboneElements.apply(resource);
			return backboneElements.stream()
					.map(e -> getReference(e, hasReference, getReference, referenceLocation, referenceTypes))
					.flatMap(Function.identity());
		}
		else
			return Stream.empty();
	}

	@SafeVarargs
	private <E extends BackboneElement> Stream<ResourceReference> getReference(E backboneElement,
			Predicate<E> hasReference, Function<E, Reference> getReference, String referenceLocation,
			Class<? extends Resource>... referenceTypes)
	{
		return hasReference.test(backboneElement) ? Stream.of(getReference.apply(backboneElement))
				.map(toResourceReference(referenceLocation, referenceTypes)) : Stream.empty();
	}

	@SafeVarargs
	private <R extends DomainResource, E extends BackboneElement> Stream<ResourceReference> getBackboneElementReferences(
			R resource, Predicate<R> hasBackboneElement, Function<R, E> getBackboneElement, Predicate<E> hasReference,
			Function<E, List<Reference>> getReference, String referenceLocation,
			Class<? extends DomainResource>... referenceTypes)
	{
		if (hasBackboneElement.test(resource))
		{
			E backboneElement = getBackboneElement.apply(resource);
			return getReferences(backboneElement, hasReference, getReference, referenceLocation, referenceTypes);
		}
		else
			return Stream.empty();
	}

	// not needed yet
	// @SafeVarargs
	// private <R extends DomainResource, E extends BackboneElement> Stream<ResourceReference>
	// getBackboneElementsReferences(
	// R resource, Predicate<R> hasBackboneElements, Function<R, List<E>> getBackboneElements,
	// Predicate<E> hasReference, Function<E, List<Reference>> getReference, String referenceLocation,
	// Class<? extends DomainResource>... referenceTypes)
	// {
	// if (hasBackboneElements.test(resource))
	// {
	// List<E> backboneElements = getBackboneElements.apply(resource);
	// return backboneElements.stream()
	// .map(e -> getReferences(e, hasReference, getReference, referenceLocation, referenceTypes))
	// .flatMap(Function.identity());
	// }
	// else
	// return Stream.empty();
	// }

	@SafeVarargs
	private <E extends BackboneElement> Stream<ResourceReference> getReferences(E backboneElement,
			Predicate<E> hasReference, Function<E, List<Reference>> getReference, String referenceLocation,
			Class<? extends DomainResource>... referenceTypes)
	{
		return hasReference.test(backboneElement) ? Stream.of(getReference.apply(backboneElement)).flatMap(List::stream)
				.map(toResourceReference(referenceLocation, referenceTypes)) : Stream.empty();
	}

	private Stream<ResourceReference> getExtensionReferences(DomainResource resource)
	{
		return resource.getExtension().stream().filter(e -> e.getValue() instanceof Reference)
				.map(e -> (Reference) e.getValue())
				.map(toResourceReference(resource.getResourceType().name() + ".extension"));
	}

	@SafeVarargs
	private Stream<ResourceReference> concat(Stream<ResourceReference>... streams)
	{
		if (streams.length == 0)
			return Stream.empty();
		else if (streams.length == 1)
			return streams[0];
		else if (streams.length == 2)
			return Stream.concat(streams[0], streams[1]);
		else
			return Arrays.stream(streams).flatMap(Function.identity());
	}

	@Override
	public Stream<ResourceReference> getReferences(Resource resource)
	{
		if (resource == null)
			return Stream.empty();

		if (resource instanceof Endpoint)
			return getReferences((Endpoint) resource);
		else if (resource instanceof Group)
			return getReferences((Group) resource);
		else if (resource instanceof HealthcareService)
			return getReferences((HealthcareService) resource);
		else if (resource instanceof Location)
			return getReferences((Location) resource);
		else if (resource instanceof Organization)
			return getReferences((Organization) resource);
		else if (resource instanceof Patient)
			return getReferences((Patient) resource);
		else if (resource instanceof Practitioner)
			return getReferences((Practitioner) resource);
		else if (resource instanceof PractitionerRole)
			return getReferences((PractitionerRole) resource);
		else if (resource instanceof Provenance)
			return getReferences((Provenance) resource);
		else if (resource instanceof ResearchStudy)
			return getReferences((ResearchStudy) resource);
		else if (resource instanceof Task)
			return getReferences((Task) resource);
		else if (resource instanceof DomainResource)
		{
			logger.debug("DomainResource of type {} not supported, returning extension references only",
					resource.getClass().getName());
			return getExtensionReferences((DomainResource) resource);
		}
		else
		{
			logger.debug("Resource of type {} not supported, returning no references", resource.getClass().getName());
			return Stream.empty();

		}
	}

	@Override
	public Stream<ResourceReference> getReferences(Endpoint resource)
	{
		if (resource == null)
			return Stream.empty();

		var managingOrganization = getReference(resource, Endpoint::hasManagingOrganization,
				Endpoint::getManagingOrganization, "Endpoint.managingOrganization", Organization.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(managingOrganization, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(Group resource)
	{
		if (resource == null)
			return Stream.empty();

		var managingEntity = getReference(resource, Group::hasManagingEntity, Group::getManagingEntity,
				"Group.managingEntity", Organization.class, RelatedPerson.class, Practitioner.class,
				PractitionerRole.class);

		var memberEntities = getBackboneElementsReference(resource, Group::hasMember, Group::getMember,
				Group.GroupMemberComponent::hasEntity, Group.GroupMemberComponent::getEntity, "Group.member.entity",
				Patient.class, Practitioner.class, PractitionerRole.class, Device.class, Medication.class,
				Substance.class, Group.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(managingEntity, memberEntities, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(HealthcareService resource)
	{
		if (resource == null)
			return Stream.empty();

		var providedBy = getReference(resource, HealthcareService::hasProvidedBy, HealthcareService::getProvidedBy,
				"HealthcareService.providedBy", Organization.class);
		var locations = getReferences(resource, HealthcareService::hasLocation, HealthcareService::getLocation,
				"HealthcareService.location", Location.class);
		var coverageAreas = getReferences(resource, HealthcareService::hasCoverageArea,
				HealthcareService::getCoverageArea, "HealthcareService.coverageArea", Location.class);
		var endpoints = getReferences(resource, HealthcareService::hasEndpoint, HealthcareService::getEndpoint,
				"HealthcareService.endpoint", Endpoint.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(providedBy, locations, coverageAreas, endpoints, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(Location resource)
	{
		var managingOrganization = getReference(resource, Location::hasManagingOrganization,
				Location::getManagingOrganization, "Location.managingOrganization", Organization.class);
		var partOf = getReference(resource, Location::hasPartOf, Location::getPartOf, "Location.partOf",
				Location.class);
		var endpoints = getReferences(resource, Location::hasEndpoint, Location::getEndpoint, "Location.endpoint",
				Endpoint.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(managingOrganization, partOf, endpoints, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(Organization resource)
	{
		var partOf = getReference(resource, Organization::hasPartOf, Organization::getPartOf, "Organization.partOf",
				Organization.class);
		var endpoints = getReferences(resource, Organization::hasEndpoint, Organization::getEndpoint,
				"Organization.endpoint", Endpoint.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(partOf, endpoints, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(Patient resource)
	{
		var contacts_organization = getBackboneElementsReference(resource, Patient::hasContact, Patient::getContact,
				ContactComponent::hasOrganization, ContactComponent::getOrganization, "Patient.contact.organization",
				Organization.class);
		var generalPractitioners = getReferences(resource, Patient::hasGeneralPractitioner,
				Patient::getGeneralPractitioner, "Patient.generalPractitioner", Organization.class, Practitioner.class,
				PractitionerRole.class);
		var managingOrganization = getReference(resource, Patient::hasManagingOrganization,
				Patient::getManagingOrganization, "Patient.managingOrganization", Organization.class);
		var links_other = getBackboneElementsReference(resource, Patient::hasLink, Patient::getLink,
				PatientLinkComponent::hasOther, PatientLinkComponent::getOther, "Patient.link.other", Patient.class,
				RelatedPerson.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(contacts_organization, generalPractitioners, managingOrganization, links_other,
				extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(Practitioner resource)
	{
		var qualifications_issuer = getBackboneElementsReference(resource, Practitioner::hasQualification,
				Practitioner::getQualification, PractitionerQualificationComponent::hasIssuer,
				PractitionerQualificationComponent::getIssuer, "Practitioner.qualification.issuer", Organization.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(qualifications_issuer, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(PractitionerRole resource)
	{
		var practitioner = getReference(resource, PractitionerRole::hasPractitioner, PractitionerRole::getPractitioner,
				"PractitionerRole.practitioner", Practitioner.class);
		var organization = getReference(resource, PractitionerRole::hasOrganization, PractitionerRole::getOrganization,
				"PractitionerRole.organization", Organization.class);
		var locations = getReferences(resource, PractitionerRole::hasLocation, PractitionerRole::getLocation,
				"PractitionerRole.location", Location.class);
		var healthcareServices = getReferences(resource, PractitionerRole::hasHealthcareService,
				PractitionerRole::getHealthcareService, "PractitionerRole.healthcareService", HealthcareService.class);
		var endpoints = getReferences(resource, PractitionerRole::hasEndpoint, PractitionerRole::getEndpoint,
				"PractitionerRole.endpoint", Endpoint.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(practitioner, organization, locations, healthcareServices, endpoints, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(Provenance resource)
	{
		var targets = getReferences(resource, Provenance::hasTarget, Provenance::getTarget, "Provenance.target");
		var location = getReference(resource, Provenance::hasLocation, Provenance::getLocation, "Provenance.location",
				Location.class);
		var agents_who = getBackboneElementsReference(resource, Provenance::hasAgent, Provenance::getAgent,
				ProvenanceAgentComponent::hasWho, ProvenanceAgentComponent::getWho, "Provenance.agent.who",
				Practitioner.class, PractitionerRole.class, RelatedPerson.class, Patient.class, Device.class,
				Organization.class);
		var agents_onBehalfOf = getBackboneElementsReference(resource, Provenance::hasAgent, Provenance::getAgent,
				ProvenanceAgentComponent::hasOnBehalfOf, ProvenanceAgentComponent::getOnBehalfOf,
				"Provenance.agent.onBehalfOf", Practitioner.class, PractitionerRole.class, RelatedPerson.class,
				Patient.class, Device.class, Organization.class);
		var entities_what = getBackboneElementsReference(resource, Provenance::hasEntity, Provenance::getEntity,
				ProvenanceEntityComponent::hasWhat, ProvenanceEntityComponent::getWhat, "Provenance.entity.what");

		var extensionReferences = getExtensionReferences(resource);

		return concat(targets, location, agents_who, agents_onBehalfOf, entities_what, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(ResearchStudy resource)
	{
		var protocols = getReferences(resource, ResearchStudy::hasProtocol, ResearchStudy::getProtocol,
				"ResearchStudy.protocol", PlanDefinition.class);
		var partOfs = getReferences(resource, ResearchStudy::hasPartOf, ResearchStudy::getPartOf,
				"ResearchStudy.partOf", ResearchStudy.class);
		var enrollments = getReferences(resource, ResearchStudy::hasEnrollment, ResearchStudy::getEnrollment,
				"ResearchStudy.enrollment", Group.class);
		var sponsor = getReference(resource, ResearchStudy::hasSponsor, ResearchStudy::getSponsor,
				"ResearchStudy.sponsor", Organization.class);
		var principalInvestigator = getReference(resource, ResearchStudy::hasPrincipalInvestigator,
				ResearchStudy::getPrincipalInvestigator, "ResearchStudy.principalInvestigator", Practitioner.class,
				PractitionerRole.class);
		var sites = getReferences(resource, ResearchStudy::hasSite, ResearchStudy::getSite, "ResearchStudy.site",
				Location.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(protocols, partOfs, enrollments, sponsor, principalInvestigator, sites, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(Task resource)
	{
		var basedOns = getReferences(resource, Task::hasBasedOn, Task::getBasedOn, "Task.basedOn");
		var partOfs = getReferences(resource, Task::hasPartOf, Task::getPartOf, "Task.partOf", Task.class);
		var focus = getReference(resource, Task::hasFocus, Task::getFocus, "Task.focus");
		var for_ = getReference(resource, Task::hasFor, Task::getFor, "Task.for");
		var encounter = getReference(resource, Task::hasEncounter, Task::getEncounter, "Task.encounter",
				Encounter.class);
		var requester = getReference(resource, Task::hasRequester, Task::getRequester, "Task.requester", Device.class,
				Organization.class, Patient.class, Practitioner.class, PractitionerRole.class, RelatedPerson.class);
		var owner = getReference(resource, Task::hasOwner, Task::getOwner, "Task.owner", Practitioner.class,
				PractitionerRole.class, Organization.class, CareTeam.class, HealthcareService.class, Patient.class,
				Device.class, RelatedPerson.class);
		var location = getReference(resource, Task::hasLocation, Task::getLocation, "Task.location", Location.class);
		var reasonReference = getReference(resource, Task::hasReasonReference, Task::getReasonReference,
				"Task.reasonReference");
		var insurance = getReferences(resource, Task::hasInsurance, Task::getInsurance, "Task.insurance",
				Coverage.class, ClaimResponse.class);
		var relevanteHistories = getReferences(resource, Task::hasRelevantHistory, Task::getRelevantHistory,
				"Task.relevantHistory", Provenance.class);
		var restriction_recipiets = getBackboneElementReferences(resource, Task::hasRestriction, Task::getRestriction,
				Task.TaskRestrictionComponent::hasRecipient, Task.TaskRestrictionComponent::getRecipient,
				"Task.restriction.recipient", Patient.class, Practitioner.class, PractitionerRole.class,
				RelatedPerson.class, Group.class, Organization.class);

		var inputReferences = getInputReferences(resource);
		var outputReferences = getOutputReferences(resource);
		var extensionReferences = getExtensionReferences(resource);

		return concat(basedOns, partOfs, focus, for_, encounter, requester, owner, location, reasonReference, insurance,
				relevanteHistories, restriction_recipiets, inputReferences, outputReferences, extensionReferences);
	}

	private Stream<ResourceReference> getInputReferences(Task resource)
	{
		return resource.getInput().stream().filter(in -> in.getValue() instanceof Reference)
				.map(in -> (Reference) in.getValue())
				.map(toResourceReference(resource.getResourceType().name() + ".input"));
	}

	private Stream<ResourceReference> getOutputReferences(Task resource)
	{
		return resource.getOutput().stream().filter(out -> out.getValue() instanceof Reference)
				.map(out -> (Reference) out.getValue())
				.map(toResourceReference(resource.getResourceType().name() + ".output"));
	}
}
