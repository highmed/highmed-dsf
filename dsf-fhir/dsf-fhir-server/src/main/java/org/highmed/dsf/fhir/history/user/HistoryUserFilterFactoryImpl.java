package org.highmed.dsf.fhir.history.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.User;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.ValueSet;

public class HistoryUserFilterFactoryImpl implements HistoryUserFilterFactory
{
	private final Map<Class<? extends Resource>, Function<User, HistoryUserFilter>> filtersByResource = new HashMap<>();

	public HistoryUserFilterFactoryImpl()
	{
		filtersByResource.put(ActivityDefinition.class, ActivityDefinitionHistoryUserFilter::new);
		filtersByResource.put(Binary.class, BinaryHistoryUserFilter::new);
		filtersByResource.put(Bundle.class, BundleHistoryUserFilter::new);
		filtersByResource.put(CodeSystem.class, CodeSystemHistoryUserFilter::new);
		filtersByResource.put(DocumentReference.class, DocumentReferenceHistoryUserFilter::new);
		filtersByResource.put(Endpoint.class, EndpointHistoryUserFilter::new);
		filtersByResource.put(Group.class, GroupHistoryUserFilter::new);
		filtersByResource.put(HealthcareService.class, HealthcareServiceHistoryUserFilter::new);
		filtersByResource.put(Library.class, LibraryHistoryUserFilter::new);
		filtersByResource.put(Location.class, LocationHistoryUserFilter::new);
		filtersByResource.put(Measure.class, MeasureHistoryUserFilter::new);
		filtersByResource.put(MeasureReport.class, MeasureReportHistoryUserFilter::new);
		filtersByResource.put(NamingSystem.class, NamingSystemHistoryUserFilter::new);
		filtersByResource.put(OrganizationAffiliation.class, OrganizationAffiliationHistoryUserFilter::new);
		filtersByResource.put(Organization.class, OrganizationHistoryUserFilter::new);
		filtersByResource.put(Patient.class, PatientHistoryUserFilter::new);
		filtersByResource.put(Practitioner.class, PractitionerHistoryUserFilter::new);
		filtersByResource.put(PractitionerRole.class, PractitionerRoleHistoryUserFilter::new);
		filtersByResource.put(Provenance.class, ProvenanceHistoryUserFilter::new);
		filtersByResource.put(Questionnaire.class, QuestionnaireHistoryUserFilter::new);
		filtersByResource.put(QuestionnaireResponse.class, QuestionnaireResponseHistoryUserFilter::new);
		filtersByResource.put(ResearchStudy.class, ResearchStudyHistoryUserFilter::new);
		filtersByResource.put(StructureDefinition.class, StructureDefinitionHistoryUserFilter::new);
		filtersByResource.put(Subscription.class, SubscriptionHistoryUserFilter::new);
		filtersByResource.put(Task.class, TaskHistoryUserFilter::new);
		filtersByResource.put(ValueSet.class, ValueSetHistoryUserFilter::new);
	}

	@Override
	public HistoryUserFilter getUserFilter(User user, Class<? extends Resource> resourceType)
	{
		Function<User, HistoryUserFilter> factory = filtersByResource.get(resourceType);
		if (factory == null)
			throw new IllegalArgumentException(HistoryUserFilter.class.getSimpleName() + " for "
					+ resourceType.getClass().getName() + " not found");
		else
			return factory.apply(user);
	}

	@Override
	public List<HistoryUserFilter> getUserFilters(User user)
	{
		return filtersByResource.values().stream().map(f -> f.apply(user)).collect(Collectors.toList());
	}
}
