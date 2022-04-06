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
import org.hl7.fhir.r4.model.IdType;
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
		out.write("<!DOCTYPE html>\n");
		out.write("<html>\n<head>\n");
		out.write("<link rel=\"icon\" type=\"image/svg+xml\" href=\"/fhir/static/favicon.svg\">\n");
		out.write("<link rel=\"icon\" type=\"image/png\" href=\"/fhir/static/favicon_32x32.png\" sizes=\"32x32\">\n");
		out.write("<link rel=\"icon\" type=\"image/png\" href=\"/fhir/static/favicon_96x96.png\" sizes=\"96x96\">\n");
		out.write("<meta name=\"theme-color\" content=\"#29235c\">\n");
		out.write("<script src=\"/fhir/static/prettify.js\"></script>\n");
		out.write("<script src=\"/fhir/static/tabs.js\"></script>\n");
		out.write("<script src=\"/fhir/static/bookmarks.js\"></script>\n");
		out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/fhir/static/prettify.css\">\n");
		out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/fhir/static/highmed.css\">\n");
		out.write("<title>DSF" + (uriInfo.getPath() == null || uriInfo.getPath().isEmpty() ? "" : ": ")
				+ uriInfo.getPath() + "</title>\n</head>\n");
		out.write("<body onload=\"prettyPrint();openInitialTab();checkBookmarked();\">\n");
		out.write("<div id=\"icons\">\n");
		out.write("<svg class=\"icon\" id=\"bookmark-add\" viewBox=\"0 0 24 24\" onclick=\"addCurrentBookmark();\">\n");
		out.write(
				"<path d=\"M17,18L12,15.82L7,18V5H17M17,3H7A2,2 0 0,0 5,5V21L12,18L19,21V5C19,3.89 18.1,3 17,3Z\" />\n");
		out.write("</svg>\n");
		out.write(
				"<svg class=\"icon\" id=\"bookmark-remove\" viewBox=\"0 0 24 24\" onclick=\"removeCurrentBookmark();\" style=\"display:none;\">\n");
		out.write("<path d=\"M17,3H7A2,2 0 0,0 5,5V21L12,18L19,21V5C19,3.89 18.1,3 17,3Z\"/>\n");
		out.write("</svg>\n");
		out.write("<svg class=\"icon\" id=\"bookmark-list\" viewBox=\"0 0 24 24\" onclick=\"showBookmarks();\">\n");
		out.write(
				"<path d=\"M9,1H19A2,2 0 0,1 21,3V19L19,18.13V3H7A2,2 0 0,1 9,1M15,20V7H5V20L10,17.82L15,20M15,5C16.11,5 17,5.9 17,7V23L10,20L3,23V7A2,2 0 0,1 5,5H15Z\"/>\n");
		out.write("</svg>\n");
		out.write("</div>\n");
		out.write("<div id=\"bookmarks\" style=\"display:none;\">\n");
		out.write("<h3 id=\"bookmarks-title\">Bookmarks</h3>\n");
		out.write(
				"<svg class=\"icon\" id=\"bookmark-list-close\" viewBox=\"0 0 24 24\" onclick=\"closeBookmarks();\">\n");
		out.write(
				"<path fill=\"currentColor\" d=\"M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z\"/>\n");
		out.write("</svg>\n");
		out.write("<div id=\"list\">\n");
		out.write("</div>\n");
		out.write("</div>\n");
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
			if (!uriInfo.getPath().contains("_history"))
				return Optional.of(String.format("%s/%s/%s", serverBaseProvider.getServerBase(),
						t.getIdElement().getResourceType(), t.getIdElement().getIdPart()));
			else
				return Optional.of(String.format("%s/%s/%s/_history/%s", serverBaseProvider.getServerBase(),
						t.getIdElement().getResourceType(), t.getIdElement().getIdPart(),
						t.getIdElement().getVersionIdPart()));
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
			return ((Bundle) t).getEntry().stream().filter(c ->
			{
				if (c.hasResource())
					return uuid.equals(c.getResource().getIdElement().getIdPart());
				else
					return uuid.equals(new IdType(c.getResponse().getLocation()).getIdPart());
			}).map(c ->
			{
				if (c.hasResource())
					return c.getResource().getClass().getAnnotation(ResourceDef.class).name();
				else
					return new IdType(c.getResponse().getLocation()).getResourceType();
			}).findFirst();
		else if (t instanceof Resource)
			return Optional.of(t.getClass().getAnnotation(ResourceDef.class).name());
		else
			return Optional.empty();
	}
}
