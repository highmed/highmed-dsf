package org.highmed.dsf.fhir.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;

import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.parser.IParser;

@Produces({ MediaType.TEXT_HTML })
public class HtmlFhirAdapter<T extends BaseResource> implements MessageBodyWriter<T>
{
	private static final String RESOURCE_NAMES = "Account|ActivityDefinition|AdverseEvent|AllergyIntolerance|Appointment|AppointmentResponse|AuditEvent|Basic|Binary"
			+ "|BiologicallyDerivedProduct|BodyStructure|Bundle|CapabilityStatement|CarePlan|CareTeam|CatalogEntry|ChargeItem|ChargeItemDefinition|Claim|ClaimResponse"
			+ "|ClinicalImpression|CodeSystem|Communication|CommunicationRequest|CompartmentDefinition|Composition|ConceptMap|Condition|Consent|Contract|Coverage"
			+ "|CoverageEligibilityRequest|CoverageEligibilityResponse|DetectedIssue|Device|DeviceDefinition|DeviceMetric|DeviceRequest|DeviceUseStatement"
			+ "|DiagnosticReport|DocumentManifest|DocumentReference|DomainResource|EffectEvidenceSynthesis|Encounter|Endpoint|EnrollmentRequest|EnrollmentResponse"
			+ "|EpisodeOfCare|EventDefinition|Evidence|EvidenceVariable|ExampleScenario|ExplanationOfBenefit|FamilyMemberHistory|Flag|Goal|GraphDefinition|Group"
			+ "|GuidanceResponse|HealthcareService|ImagingStudy|Immunization|ImmunizationEvaluation|ImmunizationRecommendation|ImplementationGuide|InsurancePlan"
			+ "|Invoice|Library|Linkage|List|Location|Measure|MeasureReport|Media|Medication|MedicationAdministration|MedicationDispense|MedicationKnowledge"
			+ "|MedicationRequest|MedicationStatement|MedicinalProduct|MedicinalProductAuthorization|MedicinalProductContraindication|MedicinalProductIndication"
			+ "|MedicinalProductIngredient|MedicinalProductInteraction|MedicinalProductManufactured|MedicinalProductPackaged|MedicinalProductPharmaceutical"
			+ "|MedicinalProductUndesirableEffect|MessageDefinition|MessageHeader|MolecularSequence|NamingSystem|NutritionOrder|Observation|ObservationDefinition"
			+ "|OperationDefinition|OperationOutcome|Organization|OrganizationAffiliation|Parameters|Patient|PaymentNotice|PaymentReconciliation|Person|PlanDefinition"
			+ "|Practitioner|PractitionerRole|Procedure|Provenance|Questionnaire|QuestionnaireResponse|RelatedPerson|RequestGroup|ResearchDefinition"
			+ "|ResearchElementDefinition|ResearchStudy|ResearchSubject|Resource|RiskAssessment|RiskEvidenceSynthesis|Schedule|SearchParameter|ServiceRequest|Slot"
			+ "|Specimen|SpecimenDefinition|StructureDefinition|StructureMap|Subscription|Substance|SubstanceNucleicAcid|SubstancePolymer|SubstanceProtein"
			+ "|SubstanceReferenceInformation|SubstanceSourceMaterial|SubstanceSpecification|SupplyDelivery|SupplyRequest|Task|TerminologyCapabilities|TestReport"
			+ "|TestScript|ValueSet|VerificationResult|VisionPrescription";

	private static final Pattern URL_PATTERN = Pattern
			.compile("(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_.|]");
	private static final Pattern REFERENCE_UUID_PATTERN = Pattern.compile("&lt;reference value=\"((" + RESOURCE_NAMES
			+ ")/[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12})\"&gt;");
	private static final Pattern ID_UUID_PATTERN = Pattern.compile(
			"&lt;id value=\"([0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12})\"&gt;");

	private final FhirContext fhirContext;
	private final Class<T> resourceType;

	@Context
	private UriInfo uriInfo;

	protected HtmlFhirAdapter(FhirContext fhirContext, Class<T> resourceType)
	{
		this.fhirContext = fhirContext;
		this.resourceType = resourceType;
	}

	private IParser getParser()
	{
		/* Parsers are not guaranteed to be thread safe */
		IParser p = fhirContext.newXmlParser();
		p.setStripVersionsFromReferences(false);
		p.setOverrideResourceIdWithBundleEntryFullUrl(false);
		p.setPrettyPrint(true);

		return p;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
	{
		return resourceType.equals(type);
	}

	@Override
	public void writeTo(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException
	{
		OutputStreamWriter out = new OutputStreamWriter(entityStream);
		out.write("<html><head>"
				+ "<link rel=\"icon\" type=\"image/svg+xml\" href=\"/fhir/static/favicon.svg\" sizes=\"any\">"
				+ "<link rel=\"icon\" type=\"image/png\" href=\"/fhir/static/favicon_32x32.png\" sizes=\"32x32\">"
				+ "<link rel=\"icon\" type=\"image/png\" href=\"/fhir/static/favicon_96x96.png\" sizes=\"96x96\">"
				+ "<meta name=\"theme-color\" content=\"#29235c\">"
				+ "<script src=\"/fhir/static/prettify.js\"></script>"
				+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"/fhir/static/prettify.css\">"
				+ "<style>li.L0, li.L1, li.L2, li.L3, li.L5, li.L6, li.L7, li.L8 {list-style-type: decimal !important;}</style></head>"
				+ "<body onload=\"prettyPrint()\" style=\"margin:2em;\">"
				+ "<table style=\"margin-bottom:2em;\"><tr><td><image src=\"/fhir/static/highmed.svg\" style=\"height:6em;margin-bottom:0.2em\"></td>"
				+ "<td style=\"vertical-align:bottom;\"><h1 style=\"font-family:monospace;color:#29235c;margin:0 0 0 1em;\">"
				+ URLDecoder.decode(uriInfo.getRequestUri().toString(), StandardCharsets.UTF_8)
				+ "</h1></td></tr></table>" + "<pre class=\"prettyprint linenums\">");
		String content = getParser().encodeResourceToString(t).replace("<", "&lt;").replace(">", "&gt;");

		Matcher urlMatcher = URL_PATTERN.matcher(content);
		content = urlMatcher.replaceAll(result -> "<a href=\"" + result.group() + "\">" + result.group() + "</a>");

		Matcher referenceUuidMatcher = REFERENCE_UUID_PATTERN.matcher(content);
		content = referenceUuidMatcher.replaceAll(result -> "&lt;reference value=\"<a href=\"/fhir/" + result.group(1)
				+ "\">" + result.group(1) + "</a>\"&gt");

		Matcher idUuidMatcher = ID_UUID_PATTERN.matcher(content);
		content = idUuidMatcher.replaceAll(result ->
		{
			Optional<String> resourceName = getResourceName(t, result.group(1));
			return resourceName.map(rN -> "&lt;id value=\"<a href=\"/fhir/" + rN + "/" + result.group(1) + "\">"
					+ result.group(1) + "</a>\"&gt").orElse(result.group(0));
		});

		out.write(content);
		out.write("</pre></html>");
		out.flush();
	}

	private Optional<String> getResourceName(T t, String uuid)
	{
		if (t instanceof Bundle)
			return ((Bundle) t).getEntry().stream().filter(c -> uuid.equals(c.getResource().getIdElement().getIdPart()))
					.map(c -> c.getResource().getClass().getAnnotation(ResourceDef.class).name()).findFirst();
		else if (t instanceof Resource)
			return Optional.of(t.getClass().getAnnotation(ResourceDef.class).name());
		else
			return Optional.empty();
	}
}
