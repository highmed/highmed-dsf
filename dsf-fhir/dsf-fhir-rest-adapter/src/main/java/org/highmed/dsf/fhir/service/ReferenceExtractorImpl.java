package org.highmed.dsf.fhir.service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.BackboneElement;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceRelatesToComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.ListResource;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupComponent;
import org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupPopulationComponent;
import org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupStratifierComponent;
import org.hl7.fhir.r4.model.MeasureReport.StratifierGroupComponent;
import org.hl7.fhir.r4.model.MeasureReport.StratifierGroupPopulationComponent;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.ObservationDefinition;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Patient.ContactComponent;
import org.hl7.fhir.r4.model.Patient.PatientLinkComponent;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Practitioner.PractitionerQualificationComponent;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.r4.model.Provenance.ProvenanceEntityComponent;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.SpecimenDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Substance;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceExtractorImpl implements ReferenceExtractor
{
	private static final Logger logger = LoggerFactory.getLogger(ReferenceExtractorImpl.class);

	@SafeVarargs
	private Function<Reference, ResourceReference> toResourceReferenceFromReference(String referenceLocation,
			Class<? extends Resource>... referenceTypes)
	{
		return ref -> new ResourceReference(referenceLocation, ref, referenceTypes);
	}

	private Function<RelatedArtifact, ResourceReference> toResourceReferenceFromRelatedArtifact(
			String relatedArtifactLocation)
	{
		return rel -> new ResourceReference(relatedArtifactLocation, rel);
	}

	private Function<Attachment, ResourceReference> toResourceReferenceFromAttachment(String attachmentLocation)
	{
		return rel -> new ResourceReference(attachmentLocation, rel);
	}

	@SafeVarargs
	private <R extends Resource> Stream<ResourceReference> getReference(R resource, Predicate<R> hasReference,
			Function<R, Reference> getReference, String referenceLocation,
			Class<? extends DomainResource>... referenceTypes)
	{
		return hasReference.test(resource) ? Stream.of(getReference.apply(resource))
				.map(toResourceReferenceFromReference(referenceLocation, referenceTypes)) : Stream.empty();
	}

	@SafeVarargs
	private <R extends Resource> Stream<ResourceReference> getReferences(R resource, Predicate<R> hasReference,
			Function<R, List<Reference>> getReference, String referenceLocation,
			Class<? extends Resource>... referenceTypes)
	{
		return hasReference.test(resource) ? Stream.of(getReference.apply(resource)).flatMap(List::stream)
				.map(toResourceReferenceFromReference(referenceLocation, referenceTypes)) : Stream.empty();
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

	private <R extends Resource, E extends BackboneElement> Stream<ResourceReference> getBackboneElementsAttachment(
			R resource, Predicate<R> hasBackboneElements, Function<R, List<E>> getBackboneElements,
			Predicate<E> hasAttachment, Function<E, Attachment> getAttachment, String attachmentLocation)
	{
		if (hasBackboneElements.test(resource))
		{
			List<E> backboneElements = getBackboneElements.apply(resource);
			return backboneElements.stream()
					.map(e -> getAttachment(e, hasAttachment, getAttachment, attachmentLocation))
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
				.map(toResourceReferenceFromReference(referenceLocation, referenceTypes)) : Stream.empty();
	}

	private <E extends BackboneElement> Stream<ResourceReference> getAttachment(E backboneElement,
			Predicate<E> hasAttachment, Function<E, Attachment> getAttachment, String attachmentLocation)
	{
		return hasAttachment.test(backboneElement) ? Stream.of(getAttachment.apply(backboneElement))
				.map(toResourceReferenceFromAttachment(attachmentLocation)) : Stream.empty();
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

	@SafeVarargs
	private <R extends DomainResource, E extends BackboneElement> Stream<ResourceReference> getBackboneElementReference(
			R resource, Predicate<R> hasBackboneElement, Function<R, E> getBackboneElement, Predicate<E> hasReference,
			Function<E, Reference> getReference, String referenceLocation,
			Class<? extends DomainResource>... referenceTypes)
	{
		if (hasBackboneElement.test(resource))
		{
			E backboneElement = getBackboneElement.apply(resource);
			return getReference(backboneElement, hasReference, getReference, referenceLocation, referenceTypes);
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
	private <R extends DomainResource, E1 extends BackboneElement, E2 extends BackboneElement> Stream<ResourceReference> getBackboneElements2Reference(
			R resource, Predicate<R> hasBackboneElements1, Function<R, List<E1>> getBackboneElements1,
			Predicate<E1> hasBackboneElements2, Function<E1, List<E2>> getBackboneElements2, Predicate<E2> hasReference,
			Function<E2, Reference> getReference, String referenceLocation,
			Class<? extends DomainResource>... referenceTypes)
	{
		if (hasBackboneElements1.test(resource))
		{
			List<E1> backboneElements1 = getBackboneElements1.apply(resource);
			return backboneElements1.stream().filter(e1 -> hasBackboneElements2.test(e1))
					.flatMap(e1 -> getBackboneElements2.apply(e1).stream())
					.map(e2 -> getReference(e2, hasReference, getReference, referenceLocation, referenceTypes))
					.flatMap(Function.identity());
		}
		else
			return Stream.empty();
	}

	@SafeVarargs
	private <R extends DomainResource, E1 extends BackboneElement, E2 extends BackboneElement, E3 extends BackboneElement, E4 extends BackboneElement> Stream<ResourceReference> getBackboneElements4Reference(
			R resource, Predicate<R> hasBackboneElements1, Function<R, List<E1>> getBackboneElements1,
			Predicate<E1> hasBackboneElements2, Function<E1, List<E2>> getBackboneElements2,
			Predicate<E2> hasBackboneElements3, Function<E2, List<E3>> getBackboneElements3,
			Predicate<E3> hasBackboneElements4, Function<E3, List<E4>> getBackboneElements4, Predicate<E4> hasReference,
			Function<E4, Reference> getReference, String referenceLocation,
			Class<? extends DomainResource>... referenceTypes)
	{
		if (hasBackboneElements1.test(resource))
		{
			List<E1> backboneElements1 = getBackboneElements1.apply(resource);
			return backboneElements1.stream().filter(e1 -> hasBackboneElements2.test(e1))
					.flatMap(e1 -> getBackboneElements2.apply(e1).stream()).filter(e2 -> hasBackboneElements3.test(e2))
					.flatMap(e2 -> getBackboneElements3.apply(e2).stream()).filter(e3 -> hasBackboneElements4.test(e3))
					.flatMap(e3 -> getBackboneElements4.apply(e3).stream())
					.map(e4 -> getReference(e4, hasReference, getReference, referenceLocation, referenceTypes))
					.flatMap(Function.identity());
		}
		else
			return Stream.empty();
	}

	@SafeVarargs
	private <E extends BackboneElement> Stream<ResourceReference> getReferences(E backboneElement,
			Predicate<E> hasReference, Function<E, List<Reference>> getReference, String referenceLocation,
			Class<? extends DomainResource>... referenceTypes)
	{
		return hasReference.test(backboneElement) ? Stream.of(getReference.apply(backboneElement)).flatMap(List::stream)
				.map(toResourceReferenceFromReference(referenceLocation, referenceTypes)) : Stream.empty();
	}

	private Stream<ResourceReference> getExtensionReferences(DomainResource resource)
	{
		var extensions = resource.getExtension().stream().filter(e -> e.getValue() instanceof Reference)
				.map(e -> (Reference) e.getValue())
				.map(toResourceReferenceFromReference(resource.getResourceType().name() + ".extension"));

		var extensionExtensions = resource.getExtension().stream()
				.flatMap(e -> getExtensionReferences(resource.getResourceType().name() + ".extension", e));

		return Stream.concat(extensions, extensionExtensions);
	}

	private Stream<ResourceReference> getExtensionReferences(String baseElementName, BackboneElement resource)
	{
		var extensions = resource.getExtension().stream().filter(e -> e.getValue() instanceof Reference)
				.map(e -> (Reference) e.getValue())
				.map(toResourceReferenceFromReference(baseElementName + ".extension"));

		var extensionExtensions = resource.getExtension().stream()
				.flatMap(e -> getExtensionReferences(baseElementName + ".extension", e));

		return Stream.concat(extensions, extensionExtensions);
	}

	private Stream<ResourceReference> getExtensionReferences(String baseElementName, Extension resource)
	{
		var extensions = resource.getExtension().stream().filter(e -> e.getValue() instanceof Reference)
				.map(e -> (Reference) e.getValue())
				.map(toResourceReferenceFromReference(baseElementName + ".extension"));

		var extensionExtensions = resource.getExtension().stream()
				.flatMap(e -> getExtensionReferences(baseElementName + ".extension", e));

		return Stream.concat(extensions, extensionExtensions);
	}

	private <R extends Resource> Stream<ResourceReference> getRelatedArtifacts(R resource,
			Predicate<R> hasRelatedArtifact, Function<R, List<RelatedArtifact>> getRelatedArtifact,
			String relatedArtifactLocation)
	{
		return hasRelatedArtifact.test(resource) ? Stream.of(getRelatedArtifact.apply(resource)).flatMap(List::stream)
				.map(toResourceReferenceFromRelatedArtifact(relatedArtifactLocation)) : Stream.empty();
	}

	private <R extends Resource> Stream<ResourceReference> getAttachments(R resource, Predicate<R> hasAttachment,
			Function<R, List<Attachment>> getAttachment, String attachmentLocation)
	{
		return hasAttachment.test(resource) ? Stream.of(getAttachment.apply(resource)).flatMap(List::stream)
				.map(toResourceReferenceFromAttachment(attachmentLocation)) : Stream.empty();
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

		if (resource instanceof ActivityDefinition)
			return getReferences((ActivityDefinition) resource);
		// not implemented yet, special rules apply for tmp ids
		// else if (resource instanceof Bundle)
		// return getReferences((Bundle) resource);
		else if (resource instanceof Binary)
			return getReferences((Binary) resource);
		else if (resource instanceof CodeSystem)
			return getReferences((CodeSystem) resource);
		else if (resource instanceof DocumentReference)
			return getReferences((DocumentReference) resource);
		else if (resource instanceof Endpoint)
			return getReferences((Endpoint) resource);
		else if (resource instanceof Group)
			return getReferences((Group) resource);
		else if (resource instanceof HealthcareService)
			return getReferences((HealthcareService) resource);
		else if (resource instanceof Library)
			return getReferences((Library) resource);
		else if (resource instanceof Location)
			return getReferences((Location) resource);
		else if (resource instanceof Measure)
			return getReferences((Measure) resource);
		else if (resource instanceof MeasureReport)
			return getReferences((MeasureReport) resource);
		else if (resource instanceof NamingSystem)
			return getReferences((NamingSystem) resource);
		else if (resource instanceof OperationOutcome)
			return getReferences((OperationOutcome) resource);
		else if (resource instanceof Organization)
			return getReferences((Organization) resource);
		else if (resource instanceof OrganizationAffiliation)
			return getReferences((OrganizationAffiliation) resource);
		else if (resource instanceof Patient)
			return getReferences((Patient) resource);
		else if (resource instanceof Practitioner)
			return getReferences((Practitioner) resource);
		else if (resource instanceof PractitionerRole)
			return getReferences((PractitionerRole) resource);
		else if (resource instanceof Provenance)
			return getReferences((Provenance) resource);
		else if (resource instanceof Questionnaire)
			return getReferences((Questionnaire) resource);
		else if (resource instanceof QuestionnaireResponse)
			return getReferences((QuestionnaireResponse) resource);
		else if (resource instanceof ResearchStudy)
			return getReferences((ResearchStudy) resource);
		else if (resource instanceof StructureDefinition)
			return getReferences((StructureDefinition) resource);
		else if (resource instanceof Subscription)
			return getReferences((Subscription) resource);
		else if (resource instanceof Task)
			return getReferences((Task) resource);
		else if (resource instanceof ValueSet)
			return getReferences((ValueSet) resource);
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
	public Stream<ResourceReference> getReferences(ActivityDefinition resource)
	{
		if (resource == null)
			return Stream.empty();

		var subjectReference = getReference(resource, ActivityDefinition::hasSubjectReference,
				ActivityDefinition::getSubjectReference, "ActivityDefinition.subjectReference", Group.class);
		var location = getReference(resource, ActivityDefinition::hasLocation, ActivityDefinition::getLocation,
				"ActivityDefinition.location", Location.class);
		var productReference = getReference(resource, ActivityDefinition::hasProductReference,
				ActivityDefinition::getProductReference, "ActivityDefinition.productReference", Medication.class,
				Substance.class);
		var specimenRequirement = getReferences(resource, ActivityDefinition::hasSpecimenRequirement,
				ActivityDefinition::getSpecimenRequirement, "ActivityDefinition.specimenRequirement",
				SpecimenDefinition.class);
		var observationRequirement = getReferences(resource, ActivityDefinition::hasObservationRequirement,
				ActivityDefinition::getObservationRequirement, "ActivityDefinition.observationRequirement",
				ObservationDefinition.class);
		var observationResultRequirement = getReferences(resource, ActivityDefinition::hasObservationResultRequirement,
				ActivityDefinition::getObservationResultRequirement, "ActivityDefinition.observationResultRequirement",
				ObservationDefinition.class);
		var relatedArtifacts = getRelatedArtifacts(resource, ActivityDefinition::hasRelatedArtifact,
				ActivityDefinition::getRelatedArtifact, "ActivityDefinition.relatedArtifact");

		var extensionReferences = getExtensionReferences(resource);

		return concat(subjectReference, location, productReference, specimenRequirement, observationRequirement,
				observationResultRequirement, relatedArtifacts, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(Binary resource)
	{
		if (resource == null)
			return Stream.empty();

		var securityContext = getReference(resource, Binary::hasSecurityContext, Binary::getSecurityContext,
				"Binary.securityContext");

		return securityContext;
	}

	@Override
	public Stream<ResourceReference> getReferences(CodeSystem resource)
	{
		if (resource == null)
			return Stream.empty();

		var extensionReferences = getExtensionReferences(resource);

		return extensionReferences;
	}

	@Override
	public Stream<ResourceReference> getReferences(DocumentReference resource)
	{
		if (resource == null)
			return null;

		var subject = getReference(resource, DocumentReference::hasSubject, DocumentReference::getSubject,
				"DocumentReference.subject", Patient.class, Practitioner.class, Group.class, Device.class);
		var author = getReferences(resource, DocumentReference::hasAuthor, DocumentReference::getAuthor,
				"DocumentReference.author", Practitioner.class, PractitionerRole.class, Organization.class,
				Device.class, Patient.class, RelatedPerson.class);
		var authenticator = getReference(resource, DocumentReference::hasAuthenticator,
				DocumentReference::getAuthenticator, "DocumentReference.authenticator", Practitioner.class,
				PractitionerRole.class, Organization.class);
		var custodian = getReference(resource, DocumentReference::hasCustodian, DocumentReference::getCustodian,
				"DocumentReference.custodian", Organization.class);
		var relatesToTarget = getBackboneElementsReference(resource, DocumentReference::hasRelatesTo,
				DocumentReference::getRelatesTo, DocumentReferenceRelatesToComponent::hasTarget,
				DocumentReferenceRelatesToComponent::getTarget, "DocumentReference.relatesTo.target",
				DocumentReference.class);
		var contextEncounters = getBackboneElementReferences(resource, DocumentReference::hasContent,
				DocumentReference::getContext, DocumentReferenceContextComponent::hasEncounter,
				DocumentReferenceContextComponent::getEncounter, "DocumentReference.context.encounter", Encounter.class,
				EpisodeOfCare.class);
		var contextSourcePatientInfo = getBackboneElementReference(resource, DocumentReference::hasContent,
				DocumentReference::getContext, DocumentReferenceContextComponent::hasSourcePatientInfo,
				DocumentReferenceContextComponent::getSourcePatientInfo, "DocumentReference.context.sourcePatientInfo",
				Patient.class);
		var contextRelated = getBackboneElementReferences(resource, DocumentReference::hasContent,
				DocumentReference::getContext, DocumentReferenceContextComponent::hasRelated,
				DocumentReferenceContextComponent::getRelated, "DocumentReference.context.related");
		var contentAttachment = getBackboneElementsAttachment(resource, DocumentReference::hasContent,
				DocumentReference::getContent, DocumentReferenceContentComponent::hasAttachment,
				DocumentReferenceContentComponent::getAttachment, "DocumentReference.content.attachment");

		var extensionReferences = getExtensionReferences(resource);

		return concat(subject, author, authenticator, custodian, relatesToTarget, contextEncounters,
				contextSourcePatientInfo, contextRelated, contentAttachment, extensionReferences);
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
	public Stream<ResourceReference> getReferences(Library resource)
	{
		if (resource == null)
			return Stream.empty();

		var subject = getReference(resource, Library::hasSubjectReference, Library::getSubjectReference,
				"Library.subject", Group.class);
		var relatedArtifact = getRelatedArtifacts(resource, Library::hasRelatedArtifact, Library::getRelatedArtifact,
				"Library.relatedArtifact");
		var content = getAttachments(resource, Library::hasContent, Library::getContent, "Library.content");

		var extensionReferences = getExtensionReferences(resource);

		return concat(subject, relatedArtifact, content, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(Location resource)
	{
		if (resource == null)
			return Stream.empty();

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
	public Stream<ResourceReference> getReferences(Measure resource)
	{
		if (resource == null)
			return Stream.empty();

		var subject = getReference(resource, Measure::hasSubjectReference, Measure::getSubjectReference,
				"Measure.subject", Group.class);
		var relatedArtifacts = getRelatedArtifacts(resource, Measure::hasRelatedArtifact, Measure::getRelatedArtifact,
				"Measure.relatedArtifact");

		var extensionReferences = getExtensionReferences(resource);

		return concat(subject, relatedArtifacts, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(MeasureReport resource)
	{
		if (resource == null)
			return Stream.empty();

		var subject = getReference(resource, MeasureReport::hasSubject, MeasureReport::getSubject,
				"MeasureReport.subject", Patient.class, Practitioner.class, PractitionerRole.class, Location.class,
				Device.class, RelatedPerson.class, Group.class);
		var reporter = getReference(resource, MeasureReport::hasReporter, MeasureReport::getReporter,
				"MeasureReport.reporter", Practitioner.class, PractitionerRole.class, Location.class,
				Organization.class);
		var subjectResults1 = getBackboneElements2Reference(resource, MeasureReport::hasGroup, MeasureReport::getGroup,
				MeasureReportGroupComponent::hasPopulation, MeasureReportGroupComponent::getPopulation,
				MeasureReportGroupPopulationComponent::hasSubjectResults,
				MeasureReportGroupPopulationComponent::getSubjectResults,
				"MeasureReport.group.population.subjectResults", ListResource.class);
		var subjectResults2 = getBackboneElements4Reference(resource, MeasureReport::hasGroup, MeasureReport::getGroup,
				MeasureReportGroupComponent::hasStratifier, MeasureReportGroupComponent::getStratifier,
				MeasureReportGroupStratifierComponent::hasStratum, MeasureReportGroupStratifierComponent::getStratum,
				StratifierGroupComponent::hasPopulation, StratifierGroupComponent::getPopulation,
				StratifierGroupPopulationComponent::hasSubjectResults,
				StratifierGroupPopulationComponent::getSubjectResults,
				"Measure.group.stratifier.stratum.population.subjectResults", ListResource.class);
		var evaluatedResource = getReferences(resource, MeasureReport::hasEvaluatedResource,
				MeasureReport::getEvaluatedResource, "Measure.evaluatedResource");

		var extensionReferences = getExtensionReferences(resource);

		return concat(subject, reporter, subjectResults1, subjectResults2, evaluatedResource, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(NamingSystem resource)
	{
		if (resource == null)
			return Stream.empty();

		var extensionReferences = getExtensionReferences(resource);

		return extensionReferences;
	}

	@Override
	public Stream<ResourceReference> getReferences(OperationOutcome resource)
	{
		if (resource == null)
			return Stream.empty();

		return getExtensionReferences(resource);
	}

	@Override
	public Stream<ResourceReference> getReferences(Organization resource)
	{
		if (resource == null)
			return Stream.empty();

		var partOf = getReference(resource, Organization::hasPartOf, Organization::getPartOf, "Organization.partOf",
				Organization.class);
		var endpoints = getReferences(resource, Organization::hasEndpoint, Organization::getEndpoint,
				"Organization.endpoint", Endpoint.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(partOf, endpoints, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(OrganizationAffiliation resource)
	{
		if (resource == null)
			return Stream.empty();

		var organization = getReference(resource, OrganizationAffiliation::hasOrganization,
				OrganizationAffiliation::getOrganization, "OrganizationAffiliation.organization", Organization.class);
		var participatingOrganization = getReference(resource, OrganizationAffiliation::hasParticipatingOrganization,
				OrganizationAffiliation::getParticipatingOrganization,
				"OrganizationAffiliation.participatingOrganization", Organization.class);
		var network = getReferences(resource, OrganizationAffiliation::hasNetwork, OrganizationAffiliation::getNetwork,
				"OrganizationAffiliation.network", Organization.class);
		var location = getReferences(resource, OrganizationAffiliation::hasLocation,
				OrganizationAffiliation::getLocation, "OrganizationAffiliation.location", Location.class);
		var healthcareService = getReferences(resource, OrganizationAffiliation::hasHealthcareService,
				OrganizationAffiliation::getHealthcareService, "OrganizationAffiliation.healthcareService",
				HealthcareService.class);
		var endpoint = getReferences(resource, OrganizationAffiliation::hasEndpoint,
				OrganizationAffiliation::getEndpoint, "OrganizationAffiliation.endpoint", Endpoint.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(organization, participatingOrganization, network, location, healthcareService, endpoint,
				extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(Patient resource)
	{
		if (resource == null)
			return Stream.empty();

		var contactsOrganization = getBackboneElementsReference(resource, Patient::hasContact, Patient::getContact,
				ContactComponent::hasOrganization, ContactComponent::getOrganization, "Patient.contact.organization",
				Organization.class);
		var generalPractitioners = getReferences(resource, Patient::hasGeneralPractitioner,
				Patient::getGeneralPractitioner, "Patient.generalPractitioner", Organization.class, Practitioner.class,
				PractitionerRole.class);
		var managingOrganization = getReference(resource, Patient::hasManagingOrganization,
				Patient::getManagingOrganization, "Patient.managingOrganization", Organization.class);
		var linksOther = getBackboneElementsReference(resource, Patient::hasLink, Patient::getLink,
				PatientLinkComponent::hasOther, PatientLinkComponent::getOther, "Patient.link.other", Patient.class,
				RelatedPerson.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(contactsOrganization, generalPractitioners, managingOrganization, linksOther,
				extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(Practitioner resource)
	{
		if (resource == null)
			return Stream.empty();

		var qualificationsIssuer = getBackboneElementsReference(resource, Practitioner::hasQualification,
				Practitioner::getQualification, PractitionerQualificationComponent::hasIssuer,
				PractitionerQualificationComponent::getIssuer, "Practitioner.qualification.issuer", Organization.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(qualificationsIssuer, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(PractitionerRole resource)
	{
		if (resource == null)
			return Stream.empty();

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
		if (resource == null)
			return Stream.empty();

		var targets = getReferences(resource, Provenance::hasTarget, Provenance::getTarget, "Provenance.target");
		var location = getReference(resource, Provenance::hasLocation, Provenance::getLocation, "Provenance.location",
				Location.class);
		var agentsWho = getBackboneElementsReference(resource, Provenance::hasAgent, Provenance::getAgent,
				ProvenanceAgentComponent::hasWho, ProvenanceAgentComponent::getWho, "Provenance.agent.who",
				Practitioner.class, PractitionerRole.class, RelatedPerson.class, Patient.class, Device.class,
				Organization.class);
		var agentsOnBehalfOf = getBackboneElementsReference(resource, Provenance::hasAgent, Provenance::getAgent,
				ProvenanceAgentComponent::hasOnBehalfOf, ProvenanceAgentComponent::getOnBehalfOf,
				"Provenance.agent.onBehalfOf", Practitioner.class, PractitionerRole.class, RelatedPerson.class,
				Patient.class, Device.class, Organization.class);
		var entitiesWhat = getBackboneElementsReference(resource, Provenance::hasEntity, Provenance::getEntity,
				ProvenanceEntityComponent::hasWhat, ProvenanceEntityComponent::getWhat, "Provenance.entity.what");

		var extensionReferences = getExtensionReferences(resource);

		return concat(targets, location, agentsWho, agentsOnBehalfOf, entitiesWhat, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(Questionnaire resource)
	{
		if (resource == null)
			return Stream.empty();

		var enableWhen = getBackboneElements2Reference(resource, Questionnaire::hasItem, Questionnaire::getItem,
				Questionnaire.QuestionnaireItemComponent::hasEnableWhen,
				Questionnaire.QuestionnaireItemComponent::getEnableWhen,
				Questionnaire.QuestionnaireItemEnableWhenComponent::hasAnswerReference,
				Questionnaire.QuestionnaireItemEnableWhenComponent::getAnswerReference,
				"Questionnaire.item.enableWhen.answerReference");

		var answerOption = getBackboneElements2Reference(resource, Questionnaire::hasItem, Questionnaire::getItem,
				Questionnaire.QuestionnaireItemComponent::hasAnswerOption,
				Questionnaire.QuestionnaireItemComponent::getAnswerOption,
				Questionnaire.QuestionnaireItemAnswerOptionComponent::hasValueReference,
				Questionnaire.QuestionnaireItemAnswerOptionComponent::getValueReference,
				"Questionnaire.item.answerOption.valueReference");

		var initial = getBackboneElements2Reference(resource, Questionnaire::hasItem, Questionnaire::getItem,
				Questionnaire.QuestionnaireItemComponent::hasInitial,
				Questionnaire.QuestionnaireItemComponent::getInitial,
				Questionnaire.QuestionnaireItemInitialComponent::hasValueReference,
				Questionnaire.QuestionnaireItemInitialComponent::getValueReference,
				"Questionnaire.item.initial.valueReference");

		var extensionReferences = getExtensionReferences(resource);

		return concat(enableWhen, answerOption, initial, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(QuestionnaireResponse resource)
	{
		if (resource == null)
			return Stream.empty();

		var author = getReference(resource, QuestionnaireResponse::hasAuthor, QuestionnaireResponse::getAuthor,
				"QuestionnaireResponse.author", Device.class, Organization.class, Patient.class, Practitioner.class,
				PractitionerRole.class, RelatedPerson.class);

		var basedOn = getReferences(resource, QuestionnaireResponse::hasBasedOn, QuestionnaireResponse::getBasedOn,
				"QuestionnaireResponse.basedOn", CarePlan.class, ServiceRequest.class);

		var encounter = getReference(resource, QuestionnaireResponse::hasEncounter, QuestionnaireResponse::getEncounter,
				"QuestionnaireResponse.encounter", Encounter.class);

		var partOf = getReferences(resource, QuestionnaireResponse::hasPartOf, QuestionnaireResponse::getPartOf,
				"QuestionnaireResponse.partOf", Observation.class, Procedure.class);

		var source = getReference(resource, QuestionnaireResponse::hasSource, QuestionnaireResponse::getSource,
				"QuestionnaireResponse.source", Patient.class, Practitioner.class, PractitionerRole.class,
				RelatedPerson.class);

		var subject = getReference(resource, QuestionnaireResponse::hasSubject, QuestionnaireResponse::getSubject,
				"QuestionnaireResponse.subject");

		var extensionReferences = getExtensionReferences(resource);

		return concat(author, basedOn, encounter, partOf, source, subject, extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(ResearchStudy resource)
	{
		if (resource == null)
			return Stream.empty();

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
		var relatedArtifacts = getRelatedArtifacts(resource, ResearchStudy::hasRelatedArtifact,
				ResearchStudy::getRelatedArtifact, "ResearchStudy.relatedArtifact");

		var extensionReferences = getExtensionReferences(resource);

		return concat(protocols, partOfs, enrollments, sponsor, principalInvestigator, sites, relatedArtifacts,
				extensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(StructureDefinition resource)
	{
		if (resource == null)
			return Stream.empty();

		var extensionReferences = getExtensionReferences(resource);

		return extensionReferences;
	}

	@Override
	public Stream<ResourceReference> getReferences(Subscription resource)
	{
		if (resource == null)
			return Stream.empty();

		var extensionReferences = getExtensionReferences(resource);

		return extensionReferences;
	}

	@Override
	public Stream<ResourceReference> getReferences(Task resource)
	{
		if (resource == null)
			return Stream.empty();

		var basedOns = getReferences(resource, Task::hasBasedOn, Task::getBasedOn, "Task.basedOn");
		var partOfs = getReferences(resource, Task::hasPartOf, Task::getPartOf, "Task.partOf", Task.class);
		var focus = getReference(resource, Task::hasFocus, Task::getFocus, "Task.focus");
		var forRef = getReference(resource, Task::hasFor, Task::getFor, "Task.for");
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
		var restrictionRecipiets = getBackboneElementReferences(resource, Task::hasRestriction, Task::getRestriction,
				Task.TaskRestrictionComponent::hasRecipient, Task.TaskRestrictionComponent::getRecipient,
				"Task.restriction.recipient", Patient.class, Practitioner.class, PractitionerRole.class,
				RelatedPerson.class, Group.class, Organization.class);

		var inputReferences = getInputReferences(resource);
		var outputReferences = getOutputReferences(resource);
		var extensionReferences = getExtensionReferences(resource);

		return concat(basedOns, partOfs, focus, forRef, encounter, requester, owner, location, reasonReference,
				insurance, relevanteHistories, restrictionRecipiets, inputReferences, outputReferences,
				extensionReferences);
	}

	private Stream<ResourceReference> getInputReferences(Task resource)
	{
		if (resource == null)
			return Stream.empty();

		var inputReferences = resource.getInput().stream().filter(in -> in.getValue() instanceof Reference)
				.map(in -> (Reference) in.getValue())
				.map(toResourceReferenceFromReference(resource.getResourceType().name() + ".input"));

		var inputExtensionReferences = resource.getInput().stream()
				.flatMap(in -> getExtensionReferences(resource.getResourceType().name() + ".input", in));

		return Stream.concat(inputReferences, inputExtensionReferences);
	}

	private Stream<ResourceReference> getOutputReferences(Task resource)
	{
		if (resource == null)
			return Stream.empty();

		var outputReferences = resource.getOutput().stream().filter(out -> out.getValue() instanceof Reference)
				.map(in -> (Reference) in.getValue())
				.map(toResourceReferenceFromReference(resource.getResourceType().name() + ".output"));

		var outputExtensionReferences = resource.getOutput().stream()
				.flatMap(out -> getExtensionReferences(resource.getResourceType().name() + ".output", out));

		return Stream.concat(outputReferences, outputExtensionReferences);
	}

	@Override
	public Stream<ResourceReference> getReferences(ValueSet resource)
	{
		if (resource == null)
			return Stream.empty();

		var extensionReferences = getExtensionReferences(resource);

		return extensionReferences;
	}
}
