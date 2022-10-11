package org.highmed.dsf.fhir.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;

import com.google.common.base.Objects;

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
			.compile("&lt;reference value=\"((" + RESOURCE_NAMES + ")/" + UUID + ")\"/&gt;");
	private static final Pattern JSON_REFERENCE_UUID_PATTERN = Pattern
			.compile("\"reference\": \"((" + RESOURCE_NAMES + ")/" + UUID + ")\",");
	private static final Pattern XML_ID_UUID_AND_VERSION_PATTERN = Pattern.compile(
			"&lt;id value=\"(" + UUID + ")\"/&gt;\\n([ ]*)&lt;meta&gt;\\n([ ]*)&lt;versionId value=\"([0-9]+)\"/&gt;");
	private static final Pattern JSON_ID_UUID_AND_VERSION_PATTERN = Pattern
			.compile("\"id\": \"(" + UUID + ")\",\\n([ ]*)\"meta\": \\{\\n([ ]*)\"versionId\": \"([0-9]+)\",");

	private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

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

	protected FhirContext getFhirContext()
	{
		return fhirContext;
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
		out.write("<script src=\"/fhir/static/help.js\"></script>\n");
		out.write("<script src=\"/fhir/static/form.js\"></script>\n");
		out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/fhir/static/prettify.css\">\n");
		out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/fhir/static/highmed.css\">\n");
		out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/fhir/static/form.css\">\n");
		out.write("<title>DSF" + (uriInfo.getPath() == null || uriInfo.getPath().isEmpty() ? "" : ": ")
				+ uriInfo.getPath() + "</title>\n</head>\n");
		out.write("<body onload=\"prettyPrint();openInitialTab(" + String.valueOf(isHtmlEnabled())
				+ ");checkBookmarked();\">\n");
		out.write("<div id=\"icons\">\n");
		out.write("<svg class=\"icon\" id=\"help-icon\" viewBox=\"0 0 24 24\" onclick=\"showHelp();\">\n");
		out.write("<title>Show Help</title>\n");
		out.write(
				"<path d=\"M11.07,12.85c0.77-1.39,2.25-2.21,3.11-3.44c0.91-1.29,0.4-3.7-2.18-3.7c-1.69,0-2.52,1.28-2.87,2.34L6.54,6.96 C7.25,4.83,9.18,3,11.99,3c2.35,0,3.96,1.07,4.78,2.41c0.7,1.15,1.11,3.3,0.03,4.9c-1.2,1.77-2.35,2.31-2.97,3.45 c-0.25,0.46-0.35,0.76-0.35,2.24h-2.89C10.58,15.22,10.46,13.95,11.07,12.85z M14,20c0,1.1-0.9,2-2,2s-2-0.9-2-2c0-1.1,0.9-2,2-2 S14,18.9,14,20z\"/>\n");
		out.write("</svg>\n");
		out.write(
				"<a href=\"\" download=\"\" id=\"download-link\" title=\"\"><svg class=\"icon\" id=\"download\" viewBox=\"0 0 24 24\">\n");
		out.write(
				"<path d=\"M18,15v3H6v-3H4v3c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2v-3H18z M17,11l-1.41-1.41L13,12.17V4h-2v8.17L8.41,9.59L7,11l5,5 L17,11z\"/>\n");
		out.write("</svg></a>\n");
		out.write("<svg class=\"icon\" id=\"bookmark-add\" viewBox=\"0 0 24 24\" onclick=\"addCurrentBookmark();\">\n");
		out.write("<title>Add Bookmark</title>\n");
		out.write(
				"<path d=\"M17,11v6.97l-5-2.14l-5,2.14V5h6V3H7C5.9,3,5,3.9,5,5v16l7-3l7,3V11H17z M21,7h-2v2h-2V7h-2V5h2V3h2v2h2V7z\"/>\n");
		out.write("</svg>\n");
		out.write(
				"<svg class=\"icon\" id=\"bookmark-remove\" viewBox=\"0 0 24 24\" onclick=\"removeCurrentBookmark();\" style=\"display:none;\">\n");
		out.write("<title>Remove Bookmark</title>\n");
		out.write(
				"<path d=\"M17,11v6.97l-5-2.14l-5,2.14V5h6V3H7C5.9,3,5,3.9,5,5v16l7-3l7,3V11H17z M21,7h-6V5h6V7z\"/>\n");
		out.write("</svg>\n");
		out.write("<svg class=\"icon\" id=\"bookmark-list\" viewBox=\"0 0 24 24\" onclick=\"showBookmarks();\">\n");
		out.write("<title>Show Bookmarks</title>\n");
		out.write(
				"<path d=\"M9,1H19A2,2 0 0,1 21,3V19L19,18.13V3H7A2,2 0 0,1 9,1M15,20V7H5V20L10,17.82L15,20M15,5C16.11,5 17,5.9 17,7V23L10,20L3,23V7A2,2 0 0,1 5,5H15Z\"/>\n");
		out.write("</svg>\n");
		out.write("</div>\n");
		out.write("<div id=\"help\" style=\"display:none;\">\n");
		out.write("<h3 id=\"help-title\">Query Parameters</h3>\n");
		out.write("<svg class=\"icon\" id=\"help-close\" viewBox=\"0 0 24 24\" onclick=\"closeHelp();\">\n");
		out.write("<title>Close Help</title>\n");
		out.write(
				"<path fill=\"currentColor\" d=\"M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z\"/>\n");
		out.write("</svg>\n");
		out.write("<div id=\"help-list\">\n");
		out.write("</div>\n");
		out.write("</div>\n");
		out.write("<div id=\"bookmarks\" style=\"display:none;\">\n");
		out.write("<h3 id=\"bookmarks-title\">Bookmarks</h3>\n");
		out.write(
				"<svg class=\"icon\" id=\"bookmark-list-close\" viewBox=\"0 0 24 24\" onclick=\"closeBookmarks();\">\n");
		out.write("<title>Close Bookmarks</title>\n");
		out.write(
				"<path fill=\"currentColor\" d=\"M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z\"/>\n");
		out.write("</svg>\n");
		out.write("<div id=\"bookmarks-list\">\n");
		out.write("</div>\n");
		out.write("</div>\n");
		out.write("<table id=\"header\"><tr>\n");
		out.write("<td><image src=\"/fhir/static/highmed.svg\"></td>\n");
		out.write("<td id=\"url\"><h1>");
		out.write(getUrlHeading(t));
		out.write("</h1></td>\n");
		out.write("</tr></table>\n");
		out.write("<div class=\"tab\">\n");

		if (isHtmlEnabled())
			out.write("<button id=\"html-button\" class=\"tablinks\" onclick=\"openTab('html')\">html</button>\n");

		out.write("<button id=\"json-button\" class=\"tablinks\" onclick=\"openTab('json')\">json</button>\n");
		out.write("<button id=\"xml-button\" class=\"tablinks\" onclick=\"openTab('xml')\">xml</button>\n");
		out.write("</div>\n");

		writeXml(t, out);
		writeJson(t, out);

		if (isHtmlEnabled())
			writeHtml(t, out);

		out.write("</html>");
		out.flush();
	}

	private String getUrlHeading(T t) throws MalformedURLException
	{
		URI uri = getResourceUrl(t).map(this::toURI).orElse(uriInfo.getRequestUri());
		String[] pathSegments = uri.getPath().split("/");

		String u = serverBaseProvider.getServerBase();
		String heading = "<a href=\"" + u + "\" title=\"Open " + u + "\">" + u + "</a>";

		for (int i = 2; i < pathSegments.length; i++)
		{
			u += "/" + pathSegments[i];
			heading += "<a href=\"" + u + "\" title=\"Open " + u + "\">/" + pathSegments[i] + "</a>";
		}

		if (uri.getQuery() != null)
		{
			u += "?" + uri.getQuery();
			heading += "<a href=\"" + u + "\" title=\"Open " + u + "\">?" + uri.getQuery() + "</a>";
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
		if (t instanceof Resource && t.getIdElement().hasIdPart())
		{
			if (!uriInfo.getPath().contains("_history"))
				return Optional.of(String.format("%s/%s/%s", serverBaseProvider.getServerBase(),
						t.getIdElement().getResourceType(), t.getIdElement().getIdPart()));
			else
				return Optional.of(String.format("%s/%s/%s/_history/%s", serverBaseProvider.getServerBase(),
						t.getIdElement().getResourceType(), t.getIdElement().getIdPart(),
						t.getIdElement().getVersionIdPart()));
		}
		else if (t instanceof Bundle && !t.getIdElement().hasIdPart())
			return ((Bundle) t).getLink().stream().filter(c -> "self".equals(c.getRelation())).findFirst()
					.map(c -> c.getUrl());
		else
			return Optional.empty();
	}

	private void writeXml(T t, OutputStreamWriter out) throws IOException
	{
		IParser parser = getParser(fhirContext::newXmlParser);

		out.write("<pre id=\"xml\" class=\"prettyprint linenums lang-xml\" style=\"display:none;\">");
		String content = parser.encodeResourceToString(t);

		content = content.replace("&amp;", "&amp;amp;").replace("&apos;", "&amp;apos;").replace("&gt;", "&amp;gt;")
				.replace("&lt;", "&amp;lt;").replace("&quot;", "&amp;quot;");
		content = simplifyXml(content);
		content = content.replace("<", "&lt;").replace(">", "&gt;");

		Matcher versionMatcher = XML_ID_UUID_AND_VERSION_PATTERN.matcher(content);
		content = versionMatcher.replaceAll(result ->
		{
			Optional<String> resourceName = getResourceName(t, result.group(1));
			return resourceName.map(rN -> "&lt;id value=\"<a href=\"/fhir/" + rN + "/" + result.group(1) + "\">"
					+ result.group(1) + "</a>\"/&gt;\n" + result.group(2) + "&lt;meta&gt;\n" + result.group(3)
					+ "&lt;versionId value=\"" + "<a href=\"/fhir/" + rN + "/" + result.group(1) + "/_history/"
					+ result.group(4) + "\">" + result.group(4) + "</a>" + "\"/&gt;").orElse(result.group(0));
		});

		Matcher urlMatcher = URL_PATTERN.matcher(content);
		content = urlMatcher.replaceAll(result -> "<a href=\""
				+ result.group().replace("&amp;amp;", "&amp;").replace("&amp;apos;", "&apos;")
						.replace("&amp;gt;", "&gt;").replace("&amp;lt;", "&lt;").replace("&amp;quot;", "&quot;")
				+ "\">" + result.group() + "</a>");

		Matcher referenceUuidMatcher = XML_REFERENCE_UUID_PATTERN.matcher(content);
		content = referenceUuidMatcher.replaceAll(result -> "&lt;reference value=\"<a href=\"/fhir/" + result.group(1)
				+ "\">" + result.group(1) + "</a>\"&gt");

		out.write(content);
		out.write("</pre>\n");
	}

	private Transformer newTransformer() throws TransformerConfigurationException
	{
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "3");
		return transformer;
	}

	private String simplifyXml(String xml)
	{
		try
		{
			Transformer transformer = newTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(writer));
			return writer.toString();
		}
		catch (TransformerException e)
		{
			throw new RuntimeException(e);
		}
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

		Matcher idUuidMatcher = JSON_ID_UUID_AND_VERSION_PATTERN.matcher(content);
		content = idUuidMatcher.replaceAll(result ->
		{
			Optional<String> resourceName = getResourceName(t, result.group(1));
			return resourceName.map(rN -> "\"id\": \"<a href=\"/fhir/" + rN + "/" + result.group(1) + "\">"
					+ result.group(1) + "</a>\",\n" + result.group(2) + "\"meta\": {\n" + result.group(3)
					+ "\"versionId\": \"" + "<a href=\"/fhir/" + rN + "/" + result.group(1) + "/_history/"
					+ result.group(4) + "\">" + result.group(4) + "</a>" + "\",").orElse(result.group(0));
		});

		out.write(content);
		out.write("</pre>\n");
	}

	private void writeHtml(T t, OutputStreamWriter out) throws IOException
	{
		out.write("<div id=\"html\" class=\"prettyprint lang-html\" style=\"display:none;\">\n");
		doWriteHtml(t, out);
		out.write("</div>\n");
	}

	/**
	 * Override this method to return <code>true</code> if the HTML tab should be shown. This implies overriding
	 * {@link #doWriteHtml(BaseResource, OutputStreamWriter)} as well.
	 *
	 * @return <code>true</code> if the html tab should be shown, <code>false</code> otherwise (default
	 *         <code>false</code>)
	 */
	protected boolean isHtmlEnabled()
	{
		return false;
	}

	/**
	 * Use this method to write output to the html tab. This implies overriding {@link #isHtmlEnabled()} as well.
	 *
	 * @param t
	 *            the resource, not <code>null</code>
	 * @param out
	 *            the outputStreamWriter, not <code>null</code>
	 * @throws IOException
	 */
	protected void doWriteHtml(T t, OutputStreamWriter out) throws IOException
	{
	}

	private Optional<String> getResourceName(T t, String uuid)
	{
		if (t instanceof Bundle)
		{
			// if persistent Bundle resource
			if (Objects.equal(uuid, t.getIdElement().getIdPart()))
				return Optional.of(t.getClass().getAnnotation(ResourceDef.class).name());
			else
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
		}
		else if (t instanceof Resource)
			return Optional.of(t.getClass().getAnnotation(ResourceDef.class).name());
		else
			return Optional.empty();
	}
}
