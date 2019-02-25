package org.highmed.fhir.webservice.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.search.parameters.OrganizationName;
import org.highmed.fhir.search.parameters.ResourceId;
import org.highmed.fhir.search.parameters.ResourceLastUpdated;
import org.highmed.fhir.search.parameters.StructureDefinitionUrl;
import org.highmed.fhir.search.parameters.SubscriptionChannelType;
import org.highmed.fhir.search.parameters.SubscriptionStatus;
import org.highmed.fhir.search.parameters.TaskRequester;
import org.highmed.fhir.search.parameters.TaskStatus;
import org.highmed.fhir.search.parameters.basic.SearchParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter.SearchParameterDefinition;
import org.highmed.fhir.webservice.specification.ConformanceService;
import org.highmed.fhir.websocket.EventEndpoint;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementImplementationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementKind;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceOperationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementSoftwareComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.RestfulCapabilityMode;
import org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteraction;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.FHIRVersion;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UrlType;
import org.hl7.fhir.r4.model.codesystems.RestfulSecurityService;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Streams;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.api.Constants;

public class ConformanceServiceImpl implements ConformanceService, InitializingBean
{
	private final CapabilityStatement capabilityStatement;
	private final ParameterConverter parameterConverter;

	public ConformanceServiceImpl(String serverBase, int defaultPageCount, ParameterConverter parameterConverter)
	{
		capabilityStatement = createCapabilityStatement(serverBase, defaultPageCount);
		this.parameterConverter = parameterConverter;
	}

	@Override
	public String getPath()
	{
		throw new UnsupportedOperationException("implemented by jaxrs service layer");
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(parameterConverter, "parameterConverter");
	}

	@Override
	public Response getMetadata(String mode, UriInfo uri, HttpHeaders headers)
	{
		return Response.ok(capabilityStatement, parameterConverter.getMediaType(uri, headers)).build();
	}

	private CapabilityStatement createCapabilityStatement(String serverBase, int defaultPageCount)
	{
		CapabilityStatement statement = new CapabilityStatement();
		statement.setStatus(PublicationStatus.ACTIVE);
		statement.setDate(new Date());
		statement.setPublisher("Demo Publisher");
		statement.setKind(CapabilityStatementKind.INSTANCE);
		statement.setSoftware(new CapabilityStatementSoftwareComponent());
		statement.getSoftware().setName("Software Name");
		statement.getSoftware().setVersion("Software Version");
		statement.setImplementation(new CapabilityStatementImplementationComponent());
		statement.getImplementation().setDescription("Implementation Description");
		statement.getImplementation().setUrl(serverBase);
		statement.setFhirVersion(FHIRVersion._4_0_0);
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
		websocketExtension.setValue(new UrlType(serverBase.replace("http", "ws") + EventEndpoint.PATH));

		var resources = Arrays.asList(HealthcareService.class, Location.class, Organization.class, Patient.class,
				PractitionerRole.class, Practitioner.class, Provenance.class, ResearchStudy.class,
				StructureDefinition.class, Subscription.class, Task.class);

		var searchParameters = new HashMap<Class<? extends DomainResource>, List<CapabilityStatementRestResourceSearchParamComponent>>();

		var organizationNameOrAlias = createSearchParameter(OrganizationName.class);
		searchParameters.put(Organization.class, Arrays.asList(organizationNameOrAlias));

		var structureDefinitionUrl = createSearchParameter(StructureDefinitionUrl.class);
		searchParameters.put(StructureDefinition.class, Arrays.asList(structureDefinitionUrl));

		var subscriptionStatus = createSearchParameter(SubscriptionStatus.class);
		var subscriptionChannelType = createSearchParameter(SubscriptionChannelType.class);
		searchParameters.put(Subscription.class, Arrays.asList(subscriptionStatus, subscriptionChannelType));

		var taskRequester = createSearchParameter(TaskRequester.class);
		var taskStatus = createSearchParameter(TaskStatus.class);
		searchParameters.put(Task.class, Arrays.asList(taskRequester, taskStatus));

		var operations = new HashMap<Class<? extends DomainResource>, List<CapabilityStatementRestResourceOperationComponent>>();

		var snapshotOperation = createOperation("snapshot",
				"http://hl7.org/fhir/OperationDefinition/StructureDefinition-snapshot",
				"Generates a StructureDefinition instance with a snapshot, based on a differential in a specified StructureDefinition");
		operations.put(StructureDefinition.class, Arrays.asList(snapshotOperation));

		@SuppressWarnings("unchecked")
		var standardSortableSearchParameters = Arrays.asList(
				createSearchParameter((Class<? extends SearchParameter<? extends DomainResource>>) ResourceId.class),
				createSearchParameter(
						(Class<? extends SearchParameter<? extends DomainResource>>) ResourceLastUpdated.class));
		var standardOperations = Arrays.asList(createValidateOperation());

		for (Class<? extends DomainResource> resource : resources)
		{
			CapabilityStatementRestResourceComponent r = rest.addResource();
			r.setType(resource.getAnnotation(ResourceDef.class).name());
			r.setProfile(resource.getAnnotation(ResourceDef.class).profile());
			r.addInteraction().setCode(TypeRestfulInteraction.CREATE);
			r.addInteraction().setCode(TypeRestfulInteraction.READ);
			r.addInteraction().setCode(TypeRestfulInteraction.VREAD);
			r.addInteraction().setCode(TypeRestfulInteraction.UPDATE);
			r.addInteraction().setCode(TypeRestfulInteraction.DELETE);
			r.addInteraction().setCode(TypeRestfulInteraction.SEARCHTYPE);

			standardSortableSearchParameters.stream().forEach(r::addSearchParam);

			var resourceSearchParameters = searchParameters.getOrDefault(resource, Collections.emptyList());
			resourceSearchParameters.forEach(r::addSearchParam);

			r.addSearchParam(createFormatParameter());
			r.addSearchParam(createCountParameter(defaultPageCount));
			r.addSearchParam(createPageParameter());
			r.addSearchParam(createSortParameter(standardSortableSearchParameters, resourceSearchParameters));

			operations.getOrDefault(resource, Collections.emptyList()).forEach(r::addOperation);
			standardOperations.forEach(r::addOperation);
		}
		return statement;
	}

	private CapabilityStatementRestResourceOperationComponent createValidateOperation()
	{
		return createOperation("validate", "http://hl7.org/fhir/OperationDefinition/Resource-validate",
				"The validate operation checks whether the attached content would be acceptable either generally, as a create, an update or as a delete to an existing resource. The action the server takes depends on the mode parameter");
	}

	private CapabilityStatementRestResourceSearchParamComponent createSortParameter(
			List<CapabilityStatementRestResourceSearchParamComponent> standardSearchParameters,
			List<CapabilityStatementRestResourceSearchParamComponent> resourceSearchParameters)
	{
		return createSearchParameter("_sort", "", SearchParamType.SPECIAL,
				"Specify the returned order, allowed values: "
						+ Streams.concat(standardSearchParameters.stream(), resourceSearchParameters.stream())
								.map(s -> s.getName()).collect(Collectors.joining(", ", "[", "]"))
						+ " (one or multiple as comma separated string), prefix with '-' for reversed order");
	}

	private CapabilityStatementRestResourceSearchParamComponent createPageParameter()
	{
		return createSearchParameter("page", "", SearchParamType.NUMBER, "Specify the page number, 1 if not specified");
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
				SearchParamType.STRING,
				"Specify the returned format of the payload response, allowed values: " + formatValues);
		return createFormatParameter;
	}

	private CapabilityStatementRestResourceOperationComponent createOperation(String name, String definition,
			String documentation)
	{
		return new CapabilityStatementRestResourceOperationComponent().setName(name).setDefinition(definition)
				.setDocumentation(documentation);
	}

	private CapabilityStatementRestResourceSearchParamComponent createSearchParameter(
			Class<? extends SearchParameter<? extends DomainResource>> parameter)
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
