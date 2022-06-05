package org.highmed.dsf.fhir.webservice.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.search.IncludeParameterDefinition;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.ActivityDefinitionDate;
import org.highmed.dsf.fhir.search.parameters.ActivityDefinitionIdentifier;
import org.highmed.dsf.fhir.search.parameters.ActivityDefinitionName;
import org.highmed.dsf.fhir.search.parameters.ActivityDefinitionStatus;
import org.highmed.dsf.fhir.search.parameters.ActivityDefinitionUrl;
import org.highmed.dsf.fhir.search.parameters.ActivityDefinitionVersion;
import org.highmed.dsf.fhir.search.parameters.BinaryContentType;
import org.highmed.dsf.fhir.search.parameters.BundleIdentifier;
import org.highmed.dsf.fhir.search.parameters.CodeSystemDate;
import org.highmed.dsf.fhir.search.parameters.CodeSystemIdentifier;
import org.highmed.dsf.fhir.search.parameters.CodeSystemStatus;
import org.highmed.dsf.fhir.search.parameters.CodeSystemUrl;
import org.highmed.dsf.fhir.search.parameters.CodeSystemVersion;
import org.highmed.dsf.fhir.search.parameters.DocumentReferenceIdentifier;
import org.highmed.dsf.fhir.search.parameters.EndpointAddress;
import org.highmed.dsf.fhir.search.parameters.EndpointIdentifier;
import org.highmed.dsf.fhir.search.parameters.EndpointName;
import org.highmed.dsf.fhir.search.parameters.EndpointOrganization;
import org.highmed.dsf.fhir.search.parameters.EndpointStatus;
import org.highmed.dsf.fhir.search.parameters.HealthcareServiceActive;
import org.highmed.dsf.fhir.search.parameters.HealthcareServiceIdentifier;
import org.highmed.dsf.fhir.search.parameters.LibraryDate;
import org.highmed.dsf.fhir.search.parameters.LibraryIdentifier;
import org.highmed.dsf.fhir.search.parameters.LibraryStatus;
import org.highmed.dsf.fhir.search.parameters.LibraryUrl;
import org.highmed.dsf.fhir.search.parameters.LibraryVersion;
import org.highmed.dsf.fhir.search.parameters.LocationIdentifier;
import org.highmed.dsf.fhir.search.parameters.MeasureDate;
import org.highmed.dsf.fhir.search.parameters.MeasureDependsOn;
import org.highmed.dsf.fhir.search.parameters.MeasureIdentifier;
import org.highmed.dsf.fhir.search.parameters.MeasureReportIdentifier;
import org.highmed.dsf.fhir.search.parameters.MeasureStatus;
import org.highmed.dsf.fhir.search.parameters.MeasureUrl;
import org.highmed.dsf.fhir.search.parameters.MeasureVersion;
import org.highmed.dsf.fhir.search.parameters.NamingSystemDate;
import org.highmed.dsf.fhir.search.parameters.NamingSystemName;
import org.highmed.dsf.fhir.search.parameters.NamingSystemStatus;
import org.highmed.dsf.fhir.search.parameters.OrganizationActive;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationActive;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationEndpoint;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationIdentifier;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationParticipatingOrganization;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationPrimaryOrganization;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationRole;
import org.highmed.dsf.fhir.search.parameters.OrganizationEndpoint;
import org.highmed.dsf.fhir.search.parameters.OrganizationIdentifier;
import org.highmed.dsf.fhir.search.parameters.OrganizationName;
import org.highmed.dsf.fhir.search.parameters.OrganizationType;
import org.highmed.dsf.fhir.search.parameters.PatientActive;
import org.highmed.dsf.fhir.search.parameters.PatientIdentifier;
import org.highmed.dsf.fhir.search.parameters.PractitionerActive;
import org.highmed.dsf.fhir.search.parameters.PractitionerIdentifier;
import org.highmed.dsf.fhir.search.parameters.PractitionerRoleActive;
import org.highmed.dsf.fhir.search.parameters.PractitionerRoleIdentifier;
import org.highmed.dsf.fhir.search.parameters.PractitionerRoleOrganization;
import org.highmed.dsf.fhir.search.parameters.PractitionerRolePractitioner;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireDate;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireIdentifier;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireResponseAuthored;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireResponseIdentifier;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireResponseStatus;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireStatus;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireUrl;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireVersion;
import org.highmed.dsf.fhir.search.parameters.ResearchStudyEnrollment;
import org.highmed.dsf.fhir.search.parameters.ResearchStudyIdentifier;
import org.highmed.dsf.fhir.search.parameters.ResearchStudyPrincipalInvestigator;
import org.highmed.dsf.fhir.search.parameters.ResourceId;
import org.highmed.dsf.fhir.search.parameters.ResourceLastUpdated;
import org.highmed.dsf.fhir.search.parameters.ResourceProfile;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionDate;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionIdentifier;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionStatus;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionUrl;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionVersion;
import org.highmed.dsf.fhir.search.parameters.SubscriptionCriteria;
import org.highmed.dsf.fhir.search.parameters.SubscriptionPayload;
import org.highmed.dsf.fhir.search.parameters.SubscriptionStatus;
import org.highmed.dsf.fhir.search.parameters.SubscriptionType;
import org.highmed.dsf.fhir.search.parameters.TaskAuthoredOn;
import org.highmed.dsf.fhir.search.parameters.TaskIdentifier;
import org.highmed.dsf.fhir.search.parameters.TaskModified;
import org.highmed.dsf.fhir.search.parameters.TaskRequester;
import org.highmed.dsf.fhir.search.parameters.TaskStatus;
import org.highmed.dsf.fhir.search.parameters.ValueSetDate;
import org.highmed.dsf.fhir.search.parameters.ValueSetIdentifier;
import org.highmed.dsf.fhir.search.parameters.ValueSetStatus;
import org.highmed.dsf.fhir.search.parameters.ValueSetUrl;
import org.highmed.dsf.fhir.search.parameters.ValueSetVersion;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractSearchParameter;
import org.highmed.dsf.fhir.search.parameters.rev.include.AbstractRevIncludeParameterFactory;
import org.highmed.dsf.fhir.search.parameters.rev.include.EndpointOrganizationRevInclude;
import org.highmed.dsf.fhir.search.parameters.rev.include.OrganizationAffiliationParticipatingOrganizationRevInclude;
import org.highmed.dsf.fhir.search.parameters.rev.include.OrganizationAffiliationPrimaryOrganizationRevInclude;
import org.highmed.dsf.fhir.search.parameters.rev.include.OrganizationEndpointRevInclude;
import org.highmed.dsf.fhir.search.parameters.rev.include.ResearchStudyEnrollmentRevInclude;
import org.highmed.dsf.fhir.webservice.base.AbstractBasicService;
import org.highmed.dsf.fhir.webservice.specification.ConformanceService;
import org.highmed.dsf.fhir.websocket.ServerEndpoint;
import org.highmed.dsf.tools.build.BuildInfoReader;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementImplementationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementKind;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceOperationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementSoftwareComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.ConditionalDeleteStatus;
import org.hl7.fhir.r4.model.CapabilityStatement.ConditionalReadStatus;
import org.hl7.fhir.r4.model.CapabilityStatement.ReferenceHandlingPolicy;
import org.hl7.fhir.r4.model.CapabilityStatement.ResourceVersionPolicy;
import org.hl7.fhir.r4.model.CapabilityStatement.RestfulCapabilityMode;
import org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteraction;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Enumerations.FHIRVersion;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Extension;
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
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UrlType;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.codesystems.RestfulSecurityService;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Streams;

import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.api.Constants;

public class ConformanceServiceImpl extends AbstractBasicService implements ConformanceService, InitializingBean
{
	private static final class StructureDefinitionDistinctByUrl implements Comparable<StructureDefinitionDistinctByUrl>
	{
		final StructureDefinition structureDefinition;
		final String url;

		public StructureDefinitionDistinctByUrl(StructureDefinition structureDefinition)
		{
			this.structureDefinition = structureDefinition;
			this.url = structureDefinition.getUrl();
		}

		public StructureDefinition get()
		{
			return structureDefinition;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((url == null) ? 0 : url.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StructureDefinitionDistinctByUrl other = (StructureDefinitionDistinctByUrl) obj;
			if (url == null)
			{
				if (other.url != null)
					return false;
			}
			else if (!url.equals(other.url))
				return false;
			return true;
		}

		@Override
		public int compareTo(StructureDefinitionDistinctByUrl o)
		{
			return url.compareTo(o.url);
		}
	}

	private final String serverBase;
	private final int defaultPageCount;
	private final BuildInfoReader buildInfoReader;
	private final ParameterConverter parameterConverter;
	private final IValidationSupport validationSupport;

	public ConformanceServiceImpl(String serverBase, int defaultPageCount, BuildInfoReader buildInfoReader,
			ParameterConverter parameterConverter, IValidationSupport validationSupport)
	{
		this.serverBase = serverBase;
		this.defaultPageCount = defaultPageCount;
		this.buildInfoReader = buildInfoReader;
		this.parameterConverter = parameterConverter;
		this.validationSupport = validationSupport;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(buildInfoReader, "buildInfoReader");
		Objects.requireNonNull(parameterConverter, "parameterConverter");
		Objects.requireNonNull(validationSupport, "validationSupport");
	}

	@Override
	public Response getMetadata(String mode, UriInfo uri, HttpHeaders headers)
	{
		return Response
				.ok(createCapabilityStatement(), parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers))
				.build();
	}

	private String getVersion(BuildInfoReader buildInfoReader)
	{
		String branch = buildInfoReader.getBuildBranch();
		String number = buildInfoReader.getBuildNumber();
		number = number.length() >= 7 ? number.substring(0, 7) : number;
		String version = buildInfoReader.getProjectVersion();

		return version + " (" + branch + "/" + number + ")";
	}

	private CapabilityStatement createCapabilityStatement()
	{
		CapabilityStatement statement = new CapabilityStatement();
		statement.setStatus(PublicationStatus.ACTIVE);
		statement.setDate(buildInfoReader.getBuildDateAsDate());
		statement.setPublisher("HiGHmed");
		statement.setKind(CapabilityStatementKind.INSTANCE);
		statement.setSoftware(new CapabilityStatementSoftwareComponent());
		statement.getSoftware().setName("HiGHmed DataSharing Framework");
		statement.getSoftware().setVersion(getVersion(buildInfoReader));
		statement.setImplementation(new CapabilityStatementImplementationComponent());
		// statement.getImplementation().setDescription("Implementation Description - TODO"); // TODO
		statement.getImplementation().setUrl(serverBase);
		statement.setFhirVersion(FHIRVersion._4_0_1);
		statement.setFormat(
				Arrays.asList(new CodeType(Constants.CT_FHIR_JSON_NEW), new CodeType(Constants.CT_FHIR_XML_NEW)));
		CapabilityStatementRestComponent rest = statement.addRest();
		rest.setMode(RestfulCapabilityMode.SERVER);
		rest.getSecurity()
				.setService(Collections.singletonList(new CodeableConcept().addCoding(new Coding(
						RestfulSecurityService.CERTIFICATES.getSystem(), RestfulSecurityService.CERTIFICATES.toCode(),
						RestfulSecurityService.CERTIFICATES.getDisplay()))));
		Extension websocketExtension = rest.addExtension();
		websocketExtension.setUrl("http://hl7.org/fhir/StructureDefinition/capabilitystatement-websocket");
		websocketExtension.setValue(new UrlType(serverBase.replace("http", "ws") + ServerEndpoint.PATH));

		var resources = Arrays.asList(ActivityDefinition.class, Binary.class, Bundle.class, CodeSystem.class,
				DocumentReference.class, Endpoint.class, Group.class, HealthcareService.class, Library.class,
				Location.class, Measure.class, MeasureReport.class, NamingSystem.class, Organization.class,
				OrganizationAffiliation.class, Patient.class, PractitionerRole.class, Practitioner.class,
				Provenance.class, Questionnaire.class, QuestionnaireResponse.class, ResearchStudy.class,
				StructureDefinition.class, Subscription.class, Task.class, ValueSet.class);

		var searchParameters = new HashMap<Class<? extends Resource>, List<Class<? extends AbstractSearchParameter<?>>>>();
		var revIncludeParameters = new HashMap<Class<? extends Resource>, List<Class<? extends AbstractRevIncludeParameterFactory>>>();

		searchParameters.put(ActivityDefinition.class,
				Arrays.asList(ActivityDefinitionDate.class, ActivityDefinitionUrl.class,
						ActivityDefinitionIdentifier.class, ActivityDefinitionVersion.class,
						ActivityDefinitionName.class, ActivityDefinitionStatus.class));

		searchParameters.put(Binary.class, Arrays.asList(BinaryContentType.class));

		searchParameters.put(Bundle.class, Arrays.asList(BundleIdentifier.class));

		searchParameters.put(CodeSystem.class, Arrays.asList(CodeSystemDate.class, CodeSystemIdentifier.class,
				CodeSystemUrl.class, CodeSystemVersion.class, CodeSystemStatus.class));

		searchParameters.put(DocumentReference.class, Arrays.asList(DocumentReferenceIdentifier.class));

		searchParameters.put(Endpoint.class, Arrays.asList(EndpointAddress.class, EndpointIdentifier.class,
				EndpointName.class, EndpointOrganization.class, EndpointStatus.class));
		revIncludeParameters.put(Endpoint.class, Arrays.asList(OrganizationEndpointRevInclude.class));

		// no Group search parameters
		revIncludeParameters.put(Group.class, Arrays.asList(ResearchStudyEnrollmentRevInclude.class));

		searchParameters.put(HealthcareService.class,
				Arrays.asList(HealthcareServiceActive.class, HealthcareServiceIdentifier.class));

		searchParameters.put(Library.class, Arrays.asList(LibraryDate.class, LibraryIdentifier.class,
				LibraryStatus.class, LibraryUrl.class, LibraryVersion.class));

		searchParameters.put(Location.class, Arrays.asList(LocationIdentifier.class));

		searchParameters.put(Measure.class, Arrays.asList(MeasureDate.class, MeasureDependsOn.class,
				MeasureIdentifier.class, MeasureStatus.class, MeasureUrl.class, MeasureVersion.class));

		searchParameters.put(MeasureReport.class, Arrays.asList(MeasureReportIdentifier.class));

		searchParameters.put(NamingSystem.class,
				Arrays.asList(NamingSystemDate.class, NamingSystemName.class, NamingSystemStatus.class));

		searchParameters.put(Organization.class, Arrays.asList(OrganizationActive.class, OrganizationEndpoint.class,
				OrganizationIdentifier.class, OrganizationName.class, OrganizationType.class));
		revIncludeParameters.put(Organization.class,
				Arrays.asList(EndpointOrganizationRevInclude.class,
						OrganizationAffiliationParticipatingOrganizationRevInclude.class,
						OrganizationAffiliationPrimaryOrganizationRevInclude.class));

		searchParameters.put(OrganizationAffiliation.class,
				Arrays.asList(OrganizationAffiliationActive.class, OrganizationAffiliationEndpoint.class,
						OrganizationAffiliationIdentifier.class, OrganizationAffiliationParticipatingOrganization.class,
						OrganizationAffiliationPrimaryOrganization.class, OrganizationAffiliationRole.class));

		searchParameters.put(Patient.class, Arrays.asList(PatientActive.class, PatientIdentifier.class));

		searchParameters.put(Practitioner.class, Arrays.asList(PractitionerActive.class, PractitionerIdentifier.class));

		searchParameters.put(PractitionerRole.class,
				Arrays.asList(PractitionerRoleActive.class, PractitionerRoleIdentifier.class,
						PractitionerRoleOrganization.class, PractitionerRolePractitioner.class));

		searchParameters.put(Questionnaire.class, Arrays.asList(QuestionnaireDate.class, QuestionnaireIdentifier.class,
				QuestionnaireStatus.class, QuestionnaireUrl.class, QuestionnaireVersion.class));

		searchParameters.put(QuestionnaireResponse.class, Arrays.asList(QuestionnaireResponseAuthored.class,
				QuestionnaireResponseIdentifier.class, QuestionnaireResponseStatus.class));

		searchParameters.put(ResearchStudy.class, Arrays.asList(ResearchStudyIdentifier.class,
				ResearchStudyEnrollment.class, ResearchStudyPrincipalInvestigator.class));

		searchParameters.put(StructureDefinition.class,
				Arrays.asList(StructureDefinitionDate.class, StructureDefinitionIdentifier.class,
						StructureDefinitionStatus.class, StructureDefinitionUrl.class,
						StructureDefinitionVersion.class));

		searchParameters.put(Subscription.class, Arrays.asList(SubscriptionCriteria.class, SubscriptionPayload.class,
				SubscriptionStatus.class, SubscriptionType.class));

		searchParameters.put(Task.class, Arrays.asList(TaskAuthoredOn.class, TaskIdentifier.class, TaskModified.class,
				TaskRequester.class, TaskStatus.class));

		searchParameters.put(ValueSet.class, Arrays.asList(ValueSetDate.class, ValueSetIdentifier.class,
				ValueSetUrl.class, ValueSetVersion.class, ValueSetStatus.class));

		var operations = new HashMap<Class<? extends DomainResource>, List<CapabilityStatementRestResourceOperationComponent>>();

		var snapshotOperation = createOperation("snapshot",
				"http://hl7.org/fhir/OperationDefinition/StructureDefinition-snapshot",
				"Generates a StructureDefinition instance with a snapshot, based on a differential in a specified StructureDefinition");
		operations.put(StructureDefinition.class, Arrays.asList(snapshotOperation));

		var standardSortableSearchParameters = Arrays.asList(ResourceId.class, ResourceLastUpdated.class,
				ResourceProfile.class);
		var standardOperations = Arrays.asList(createValidateOperation());

		Map<String, List<CanonicalType>> profileUrlsByResource = validationSupport.fetchAllStructureDefinitions()
				.stream().filter(r -> r instanceof StructureDefinition).map(r -> (StructureDefinition) r)
				.filter(s -> StructureDefinitionKind.RESOURCE.equals(s.getKind()) && !s.getAbstract()
						&& EnumSet.of(PublicationStatus.ACTIVE, PublicationStatus.DRAFT).contains(s.getStatus())
						&& !s.getUrl().contains("hl7.org"))
				.map(StructureDefinitionDistinctByUrl::new).distinct().sorted()
				.map(StructureDefinitionDistinctByUrl::get).collect(Collectors.groupingBy(StructureDefinition::getType,
						Collectors.mapping(s -> new CanonicalType(s.getUrl()), Collectors.toList())));

		for (Class<? extends Resource> resource : resources)
		{
			CapabilityStatementRestResourceComponent r = rest.addResource();
			r.setVersioning(ResourceVersionPolicy.VERSIONED);
			r.setReadHistory(true);
			r.setUpdateCreate(false);
			r.setConditionalCreate(true);
			r.setConditionalRead(ConditionalReadStatus.FULLSUPPORT);
			r.setConditionalUpdate(true);
			r.setConditionalDelete(ConditionalDeleteStatus.SINGLE);
			r.addReferencePolicy(ReferenceHandlingPolicy.LITERAL);
			r.addReferencePolicy(ReferenceHandlingPolicy.LOGICAL);

			ResourceDef resourceDefAnnotation = resource.getAnnotation(ResourceDef.class);
			r.setType(resourceDefAnnotation.name());
			r.setProfile(resourceDefAnnotation.profile());
			r.addInteraction().setCode(TypeRestfulInteraction.CREATE);
			r.addInteraction().setCode(TypeRestfulInteraction.READ);
			r.addInteraction().setCode(TypeRestfulInteraction.VREAD);
			r.addInteraction().setCode(TypeRestfulInteraction.UPDATE);
			r.addInteraction().setCode(TypeRestfulInteraction.DELETE);
			r.addInteraction().setCode(TypeRestfulInteraction.SEARCHTYPE);

			var resourceSearchParameters = searchParameters.getOrDefault(resource, Collections.emptyList());
			resourceSearchParameters.stream().map(this::createSearchParameter)
					.sorted(Comparator.comparing(CapabilityStatementRestResourceSearchParamComponent::getName))
					.forEach(r::addSearchParam);

			r.addSearchParam(createCountParameter(defaultPageCount));
			r.addSearchParam(createFormatParameter());
			r.addSearchParam(createIdParameter());

			var includes = resourceSearchParameters.stream().map(p -> p.getAnnotation(IncludeParameterDefinition.class))
					.filter(def -> def != null).collect(Collectors.toList());
			if (!includes.isEmpty())
			{
				r.addSearchParam(createIncludeParameter(includes));
				r.setSearchInclude(includes.stream().flatMap(this::toIncludeParameterNames).sorted()
						.map(StringType::new).collect(Collectors.toList()));
			}

			r.addSearchParam(createLastUpdatedParameter());
			r.addSearchParam(createPageParameter());
			r.addSearchParam(createPrettyParameter());
			r.addSearchParam(createProfileParameter());

			var resourceRevIncludeParameters = revIncludeParameters.getOrDefault(resource, Collections.emptyList());
			var revIncludes = resourceRevIncludeParameters.stream()
					.map(p -> p.getAnnotation(IncludeParameterDefinition.class)).filter(def -> def != null)
					.collect(Collectors.toList());
			if (!revIncludes.isEmpty())
			{
				r.addSearchParam(createRevIncludeParameter(revIncludes));
				r.setSearchRevInclude(revIncludes.stream().flatMap(this::toIncludeParameterNames).sorted()
						.map(StringType::new).collect(Collectors.toList()));
			}

			r.addSearchParam(createSortParameter(
					Stream.concat(standardSortableSearchParameters.stream(), resourceSearchParameters.stream())));

			operations.getOrDefault(resource, Collections.emptyList()).forEach(r::addOperation);
			standardOperations.forEach(r::addOperation);

			r.setSupportedProfile(
					profileUrlsByResource.getOrDefault(resourceDefAnnotation.name(), Collections.emptyList()));
		}

		return statement;
	}

	private CapabilityStatementRestResourceSearchParamComponent createIncludeParameter(
			List<IncludeParameterDefinition> includes)
	{
		String values = includes.stream().flatMap(this::toIncludeParameterNames).sorted()
				.collect(Collectors.joining(", ", "[", "]"));
		return createSearchParameter("_include", "", SearchParamType.SPECIAL,
				"Additional resources to return, allowed values: " + values
						+ " (use one _include parameter for every resource to include)");
	}

	private CapabilityStatementRestResourceSearchParamComponent createRevIncludeParameter(
			List<IncludeParameterDefinition> revIncludes)
	{
		String values = revIncludes.stream().flatMap(this::toIncludeParameterNames).sorted()
				.collect(Collectors.joining(", ", "[", "]"));

		return createSearchParameter("_revinclude", "", SearchParamType.SPECIAL,
				"Additional resources to return, allowed values: " + values
						+ " (use one _revinclude parameter for every resource to include)");
	}

	private Stream<String> toIncludeParameterNames(IncludeParameterDefinition def)
	{
		return Arrays.stream(def.targetResourceTypes()).map(target -> target.getAnnotation(ResourceDef.class).name())
				.map(target -> def.resourceType().getAnnotation(ResourceDef.class).name() + ":" + def.parameterName()
						+ ":" + target);
	}

	private CapabilityStatementRestResourceOperationComponent createValidateOperation()
	{
		return createOperation("validate", "http://hl7.org/fhir/OperationDefinition/Resource-validate",
				"The validate operation checks whether the attached content would be acceptable either generally, as a create, an update or as a delete to an existing resource. The action the server takes depends on the mode parameter");
	}

	private CapabilityStatementRestResourceSearchParamComponent createSortParameter(
			@SuppressWarnings("rawtypes") Stream<Class<? extends AbstractSearchParameter>> parameters)
	{
		String values = parameters.map(p -> p.getAnnotation(SearchParameterDefinition.class)).map(def -> def.name())
				.sorted().collect(Collectors.joining(", ", "[", "]"));

		return createSearchParameter("_sort", "", SearchParamType.SPECIAL,
				"Specify the returned order, allowed values: " + values
						+ " (one or multiple as comma separated string), prefix with '-' for reversed order");
	}

	private CapabilityStatementRestResourceSearchParamComponent createLastUpdatedParameter()
	{
		return createSearchParameter(ResourceLastUpdated.class);
	}

	private CapabilityStatementRestResourceSearchParamComponent createPageParameter()
	{
		return createSearchParameter("_page", "", SearchParamType.NUMBER,
				"Specify the page number, 1 if not specified");
	}

	private CapabilityStatementRestResourceSearchParamComponent createCountParameter(int defaultPageCount)
	{
		return createSearchParameter("_count", "", SearchParamType.NUMBER,
				"Specify the numer of returned resources per page, " + defaultPageCount + " if not specified");
	}

	private CapabilityStatementRestResourceSearchParamComponent createFormatParameter()
	{
		String formatValues = Streams
				.concat(Stream.of(ParameterConverter.JSON_FORMAT), ParameterConverter.JSON_FORMATS.stream(),
						Stream.of(ParameterConverter.XML_FORMAT), ParameterConverter.XML_FORMATS.stream())
				.collect(Collectors.joining(", ", "[", "]"));
		CapabilityStatementRestResourceSearchParamComponent createFormatParameter = createSearchParameter("_format", "",
				SearchParamType.SPECIAL,
				"Specify the returned format of the payload response, allowed values: " + formatValues);
		return createFormatParameter;
	}

	private CapabilityStatementRestResourceSearchParamComponent createIdParameter()
	{
		return createSearchParameter(ResourceId.class);
	}

	private CapabilityStatementRestResourceSearchParamComponent createPrettyParameter()
	{
		CapabilityStatementRestResourceSearchParamComponent createFormatParameter = createSearchParameter("_pretty", "",
				SearchParamType.SPECIAL,
				"Ask for a pretty printed response for human convenience, allowed values: [true, false]");
		return createFormatParameter;
	}

	private CapabilityStatementRestResourceSearchParamComponent createProfileParameter()
	{
		return createSearchParameter(ResourceProfile.class);
	}

	private CapabilityStatementRestResourceOperationComponent createOperation(String name, String definition,
			String documentation)
	{
		return new CapabilityStatementRestResourceOperationComponent().setName(name).setDefinition(definition)
				.setDocumentation(documentation);
	}

	private CapabilityStatementRestResourceSearchParamComponent createSearchParameter(Class<?> parameter)
	{
		SearchParameterDefinition d = parameter.getAnnotation(SearchParameterDefinition.class);
		return createSearchParameter(d.name(), d.definition(), d.type(), d.documentation());
	}

	private CapabilityStatementRestResourceSearchParamComponent createSearchParameter(String name, String definition,
			SearchParamType type, String documentation)
	{
		return new CapabilityStatementRestResourceSearchParamComponent().setName(name).setDefinition(definition)
				.setType(type).setDocumentation(documentation);
	}
}
