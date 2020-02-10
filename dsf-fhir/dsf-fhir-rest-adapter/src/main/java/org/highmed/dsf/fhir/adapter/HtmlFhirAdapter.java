package org.highmed.dsf.fhir.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Supplier;
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
	@FunctionalInterface
	public static interface ServerBaseProvider
	{
		String getServerBase();
	}

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

	private static final String UUID = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

	private static final Pattern URL_PATTERN = Pattern
			.compile("(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_.|]");
	private static final Pattern XML_REFERENCE_UUID_PATTERN = Pattern
			.compile("&lt;reference value=\"((" + RESOURCE_NAMES + ")/" + UUID + ")\"&gt;");
	private static final Pattern JSON_REFERENCE_UUID_PATTERN = Pattern
			.compile("\"reference\": \"((" + RESOURCE_NAMES + ")/" + UUID + ")\",");
	private static final Pattern XML_ID_UUID_PATTERN = Pattern.compile("&lt;id value=\"(" + UUID + ")\"&gt;");
	private static final Pattern JSON_ID_UUID_PATTERN = Pattern.compile("\"id\": \"(" + UUID + ")\",");

	private final FhirContext fhirContext;
	private final ServerBaseProvider serverBaseProvider;
	private final Class<T> resourceType;

	@Context
	private UriInfo uriInfo;

	protected HtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider, Class<T> resourceType)
	{
		this.fhirContext = fhirContext;
		this.serverBaseProvider = serverBaseProvider;
		this.resourceType = resourceType;
	}

	/* Parsers are not guaranteed to be thread safe */
	private IParser getParser(Supplier<IParser> parser)
	{
		IParser p = parser.get();
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
		out.write("<html>\n<head>\n");
		out.write("<link rel=\"icon\" type=\"image/svg+xml\" href=\"/fhir/static/favicon.svg\">\n");
		out.write("<link rel=\"icon\" type=\"image/png\" href=\"/fhir/static/favicon_32x32.png\" sizes=\"32x32\">\n");
		out.write("<link rel=\"icon\" type=\"image/png\" href=\"/fhir/static/favicon_96x96.png\" sizes=\"96x96\">\n");
		out.write("<meta name=\"theme-color\" content=\"#29235c\">\n");
		out.write("<script src=\"/fhir/static/prettify.js\"></script>\n");
		out.write("<script src=\"/fhir/static/tabs.js\"></script>\n");
		out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/fhir/static/prettify.css\">\n");
		out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/fhir/static/highmed.css\">\n");
		out.write("<title>DSF" + (uriInfo.getPath() == null || uriInfo.getPath().isEmpty() ? "" : ": ")
				+ uriInfo.getPath() + "</title>\n</head>\n");
		out.write("<body onload=\"prettyPrint();openInitialTab();\">\n");
		out.write("<table id=\"header\"><tr>\n");
		out.write("<td><image src=\"/fhir/static/highmed.svg\"></td>\n");
		out.write("<td id=\"url\"><h1>");
		out.write(getUrlHeading(t));
		out.write("</h1></td>\n");
		out.write("</tr></table>\n");
		out.write("<div class=\"tab\">\n");
		out.write("<button id=\"json-button\" class=\"tablinks\" onclick=\"openTab('json')\">json</button>\n");
		out.write("<button id=\"xml-button\" class=\"tablinks\" onclick=\"openTab('xml')\">xml</button>\n");
		out.write("</div>\n");

		writeXml(t, out);
		writeJson(t, out);

		out.write("</html>");
		out.flush();
	}

	private String getUrlHeading(T t) throws MalformedURLException
	{
		URI uri = getResourceUrl(t).map(this::toURI).orElse(uriInfo.getRequestUri());
		String[] pathSegments = uri.getPath().split("/");

		String u = serverBaseProvider.getServerBase();
		String heading = "<a href=\"" + u + "\">" + u + "</a>";

		for (int i = 2; i < pathSegments.length; i++)
		{
			u += "/" + pathSegments[i];
			heading += "<a href=\"" + u + "\">/" + pathSegments[i] + "</a>";
		}

		if (uri.getQuery() != null)
		{
			u += "?" + uri.getQuery();
			heading += "<a href=\"" + u + "\">?" + uri.getQuery() + "</a>";
		}

		return heading;
	}

	private URI toURI(String str)
	{
		try
		{
			return new URI(str);
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	private Optional<String> getResourceUrl(T t) throws MalformedURLException
	{
		if (t instanceof Bundle)
			return ((Bundle) t).getLink().stream().filter(c -> "self".equals(c.getRelation())).findFirst()
					.map(c -> c.getUrl());
		else if (t instanceof Resource && t.getIdElement().getResourceType() != null
				&& t.getIdElement().getIdPart() != null)
		{
			return Optional.of(String.format("%s/%s/%s", serverBaseProvider.getServerBase(),
					t.getIdElement().getResourceType(), t.getIdElement().getIdPart()));
		}
		else
			return Optional.empty();
	}

	private void writeXml(T t, OutputStreamWriter out) throws IOException
	{
		IParser parser = getParser(fhirContext::newXmlParser);

		out.write("<pre id=\"xml\" class=\"prettyprint linenums lang-xml\" style=\"display:none;\">");
		String content = parser.encodeResourceToString(t).replace("<", "&lt;").replace(">", "&gt;");

		Matcher urlMatcher = URL_PATTERN.matcher(content);
		content = urlMatcher.replaceAll(result -> "<a href=\"" + result.group() + "\">" + result.group() + "</a>");

		Matcher referenceUuidMatcher = XML_REFERENCE_UUID_PATTERN.matcher(content);
		content = referenceUuidMatcher.replaceAll(result -> "&lt;reference value=\"<a href=\"/fhir/" + result.group(1)
				+ "\">" + result.group(1) + "</a>\"&gt");

		Matcher idUuidMatcher = XML_ID_UUID_PATTERN.matcher(content);
		content = idUuidMatcher.replaceAll(result ->
		{
			Optional<String> resourceName = getResourceName(t, result.group(1));
			return resourceName.map(rN -> "&lt;id value=\"<a href=\"/fhir/" + rN + "/" + result.group(1) + "\">"
					+ result.group(1) + "</a>\"&gt").orElse(result.group(0));
		});

		out.write(content);
		out.write("</pre>\n");
	}

	private void writeJson(T t, OutputStreamWriter out) throws IOException
	{
		IParser parser = getParser(fhirContext::newJsonParser);

		out.write("<pre id=\"json\" class=\"prettyprint linenums lang-json\" style=\"display:none;\">");
		String content = parser.encodeResourceToString(t).replace("<", "&lt;").replace(">", "&gt;");

		Matcher urlMatcher = URL_PATTERN.matcher(content);
		content = urlMatcher.replaceAll(result -> "<a href=\"" + result.group() + "\">" + result.group() + "</a>");

		Matcher referenceUuidMatcher = JSON_REFERENCE_UUID_PATTERN.matcher(content);
		content = referenceUuidMatcher.replaceAll(
				result -> "\"reference\": \"<a href=\"/fhir/" + result.group(1) + "\">" + result.group(1) + "</a>\",");

		Matcher idUuidMatcher = JSON_ID_UUID_PATTERN.matcher(content);
		content = idUuidMatcher.replaceAll(result ->
		{
			Optional<String> resourceName = getResourceName(t, result.group(1));
			return resourceName.map(rN -> "\"id\": \"<a href=\"/fhir/" + rN + "/" + result.group(1) + "\">"
					+ result.group(1) + "</a>\",").orElse(result.group(0));
		});

		out.write(content);
		out.write("</pre>\n");
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
