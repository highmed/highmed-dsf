package org.highmed.fhir.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.dao.HealthcareServiceDao;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.r4.model.HealthcareService;

import ca.uhn.fhir.rest.api.Constants;

@Path(HealthcareServiceService.RESOURCE_TYPE_NAME)
public class HealthcareServiceService extends AbstractService<HealthcareServiceDao, HealthcareService>
{
	public static final String RESOURCE_TYPE_NAME = "HealthcareService";

	public HealthcareServiceService(String serverBase, int defaultPageCount, HealthcareServiceDao healthcareServiceDao)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE_NAME, healthcareServiceDao);
	}

	@GET
	@Produces({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
			Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	public Response search(@QueryParam("page") Integer page, @QueryParam("_count") Integer count,
			@QueryParam("_format") String format, @Context UriInfo uri)
	{
		int effectivePage = page == null ? 1 : page;
		int effectiveCount = (count == null || count < 0) ? getDefaultPageCount() : count;

		PartialResult<HealthcareService> patients = handleSql(() -> getDao().search(effectivePage, effectiveCount));

		UriBuilder bundleUri = uri.getAbsolutePathBuilder();

		return response(Status.OK, createSearchSet(patients, bundleUri, format), toSpecialMimeType(format)).build();
	}
}
