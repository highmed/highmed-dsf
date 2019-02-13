package org.highmed.fhir.webservice;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.highmed.fhir.dao.search.SearchId;
import org.highmed.fhir.dao.search.SearchOrganizationNameOrAlias;
import org.highmed.fhir.dao.search.SearchParameter;
import org.highmed.fhir.dao.search.SearchParameter.SearchParameterDefinition;
import org.highmed.fhir.dao.search.SearchTaskRequester;
import org.highmed.fhir.dao.search.SearchTaskStatus;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementImplementationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementKind;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementSoftwareComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.RestfulCapabilityMode;
import org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteraction;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.api.Constants;

@Path(ConformanceService.PATH)
public class ConformanceService
{
	public static final String PATH = "metadata";

	private static final Logger logger = LoggerFactory.getLogger(ConformanceService.class);

	private final String serverBase;

	public ConformanceService(String serverBase)
	{
		this.serverBase = serverBase;
	}

	@GET
	@Produces({ Constants.CT_FHIR_JSON_NEW, Constants.CT_FHIR_XML_NEW, Constants.CT_FHIR_JSON, Constants.CT_FHIR_XML,
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getMetadata(@QueryParam("mode") String mode)
	{
		logger.trace("GET {}/?mode={}", PATH, mode);

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
		statement.setFhirVersion("4.0.0");
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
		websocketExtension.setValue(new UrlType(serverBase.replace("http", "ws") + "/ws"));

		var resources = Arrays.asList(HealthcareService.class, Location.class, Organization.class, Patient.class,
				PractitionerRole.class, Practitioner.class, Provenance.class, ResearchStudy.class,
				StructureDefinition.class, Subscription.class, Task.class);

		var searchParameters = new HashMap<Class<? extends DomainResource>, List<CapabilityStatementRestResourceSearchParamComponent>>();

		var taskRequester = createCapabilityStatementPart(SearchTaskRequester.class);
		var taskStatus = createCapabilityStatementPart(SearchTaskStatus.class);
		searchParameters.put(Task.class, Arrays.asList(taskRequester, taskStatus));

		var organizationNameOrAlias = createCapabilityStatementPart(SearchOrganizationNameOrAlias.class);
		searchParameters.put(Organization.class, Arrays.asList(taskRequester, organizationNameOrAlias));

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

			r.addSearchParam(createCapabilityStatementPart(SearchId.class));
			searchParameters.getOrDefault(resource, Collections.emptyList()).forEach(r::addSearchParam);
		}

		return Response.ok(statement).build();
	}

	private CapabilityStatementRestResourceSearchParamComponent createCapabilityStatementPart(
			Class<? extends SearchParameter> parameter)
	{
		SearchParameterDefinition d = parameter.getAnnotation(SearchParameterDefinition.class);

		return new CapabilityStatementRestResourceSearchParamComponent().setName(d.name()).setDefinition(d.definition())
				.setType(d.type()).setDocumentation(d.documentation());
	}
}
