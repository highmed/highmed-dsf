package org.highmed.fhir.webservice;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementImplementationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementKind;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementSoftwareComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.RestfulCapabilityMode;
import org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteraction;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Patient;
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
		CapabilityStatementRestResourceComponent patientResource = rest.addResource();
		patientResource.setType(Patient.class.getAnnotation(ResourceDef.class).name());
		patientResource.setProfile(Patient.class.getAnnotation(ResourceDef.class).profile());
		patientResource.addInteraction().setCode(TypeRestfulInteraction.CREATE);
		patientResource.addInteraction().setCode(TypeRestfulInteraction.READ);
		patientResource.addInteraction().setCode(TypeRestfulInteraction.VREAD);
		patientResource.addInteraction().setCode(TypeRestfulInteraction.UPDATE);
		patientResource.addInteraction().setCode(TypeRestfulInteraction.DELETE);
		CapabilityStatementRestResourceComponent taskResource = rest.addResource();
		taskResource.setType(Task.class.getAnnotation(ResourceDef.class).name());
		taskResource.setProfile(Task.class.getAnnotation(ResourceDef.class).profile());
		taskResource.addInteraction().setCode(TypeRestfulInteraction.CREATE);
		taskResource.addInteraction().setCode(TypeRestfulInteraction.READ);
		taskResource.addInteraction().setCode(TypeRestfulInteraction.VREAD);
		taskResource.addInteraction().setCode(TypeRestfulInteraction.UPDATE);
		taskResource.addInteraction().setCode(TypeRestfulInteraction.DELETE);
		CapabilityStatementRestResourceComponent subscriptionResource = rest.addResource();
		subscriptionResource.setType(Subscription.class.getAnnotation(ResourceDef.class).name());
		subscriptionResource.setProfile(Task.class.getAnnotation(ResourceDef.class).profile());
		subscriptionResource.addInteraction().setCode(TypeRestfulInteraction.CREATE);
		subscriptionResource.addInteraction().setCode(TypeRestfulInteraction.READ);
		subscriptionResource.addInteraction().setCode(TypeRestfulInteraction.VREAD);
		subscriptionResource.addInteraction().setCode(TypeRestfulInteraction.UPDATE);
		subscriptionResource.addInteraction().setCode(TypeRestfulInteraction.DELETE);

		return Response.ok(statement).build();
	}
}
