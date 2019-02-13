package org.highmed.fhir.webservice;

import java.net.URI;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.dao.AbstractDomainResourceDao;
import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.dao.search.PartialResult;
import org.highmed.fhir.function.RunnableWithSqlException;
import org.highmed.fhir.function.SupplierWithSqlAndResourceDeletedException;
import org.highmed.fhir.function.SupplierWithSqlAndResourceNotFoundException;
import org.highmed.fhir.function.SupplierWithSqlException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.UriType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.rest.api.Constants;

public abstract class AbstractService<D extends AbstractDomainResourceDao<R>, R extends DomainResource> implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractService.class);

	private static final List<String> JSON_FORMATS = Arrays.asList(Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW,
			MediaType.APPLICATION_JSON);
	private static final List<String> XML_FORMATS = Arrays.asList(Constants.CT_FHIR_XML, Constants.CT_FHIR_XML_NEW,
			MediaType.APPLICATION_XML, MediaType.TEXT_XML);

	private final String serverBase;
	private final int defaultPageCount;
	private final String resourceTypeName;
	private final D dao;

	public AbstractService(String serverBase, int defaultPageCount, String resourceTypeName, D dao)
	{
		this.serverBase = serverBase;
		this.defaultPageCount = defaultPageCount;
		this.resourceTypeName = resourceTypeName;
		this.dao = dao;
	}

	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(resourceTypeName, "resourceTypeName");
		Objects.requireNonNull(dao, "dao");
	}

	protected D getDao()
	{
		return dao;
	}

	protected int getDefaultPageCount()
	{
		return defaultPageCount;
	}

	protected void handleSql(RunnableWithSqlException f)
	{
		try
		{
			f.run();
		}
		catch (SQLException e)
		{
			logger.error("Error while accessing db", e);
			throw new WebApplicationException();
		}
	}

	protected <RS> RS handleSql(SupplierWithSqlException<RS> s)
	{
		try
		{
			return s.get();
		}
		catch (SQLException e)
		{
			logger.error("Error while accessing db", e);
			throw new WebApplicationException();
		}
	}

	protected <RS> RS handleSqlAndDeleted(SupplierWithSqlAndResourceDeletedException<RS> s)
	{
		try
		{
			return s.get();
		}
		catch (ResourceDeletedException e)
		{
			throw new WebApplicationException(Response.status(Status.GONE).entity(createOutcome(IssueSeverity.ERROR,
					IssueType.DELETED, "Resource with id " + e.getId() + " is marked as deleted.")).build());
		}
		catch (SQLException e)
		{
			logger.error("Error while accessing db", e);
			throw new WebApplicationException();
		}
	}

	protected <RS> RS handleSqlAndNotFound(SupplierWithSqlAndResourceNotFoundException<RS> s)
	{
		try
		{
			return s.get();
		}
		catch (ResourceNotFoundException e)
		{
			throw new WebApplicationException(
					Response.status(Status.METHOD_NOT_ALLOWED).entity(createOutcome(IssueSeverity.ERROR,
							IssueType.PROCESSING, "Resource with id " + e.getId() + " not found.")).build());
		}
		catch (SQLException e)
		{
			logger.error("Error while accessing db", e);
			throw new WebApplicationException();
		}
	}

	protected IdType toFullId(IdType id)
	{
		id.setIdBase(serverBase);
		return id;
	}

	protected String toFullId(String id)
	{
		return toFullId(new IdType(id)).asStringValue();
	}

	private OperationOutcome createOutcome(IssueSeverity severity, IssueType type, String diagnostics)
	{
		OperationOutcome outcome = new OperationOutcome();
		outcome.getIssueFirstRep().setSeverity(severity);
		outcome.getIssueFirstRep().setCode(type);
		outcome.getIssueFirstRep().setDiagnostics(diagnostics);
		return outcome;
	}

	@GET
	@Path("/{id}")
	@Produces({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
			Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	public Response read(@PathParam("id") String id, @QueryParam("_format") String format)
	{
		logger.trace("GET '{}/{}'", resourceTypeName, id);

		String mimeType = toSpecialMimeType(format);

		Optional<R> read = handleSqlAndDeleted(() -> dao.read(new IdType(id)));

		return read.map(d -> response(Status.OK, d, mimeType)).orElse(Response.status(Status.NOT_FOUND)).build();
	}

	@GET
	@Path("/{id}/_history/{vid}")
	@Produces({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
			Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	public Response vread(@PathParam("id") String id, @PathParam("vid") String version,
			@QueryParam("_format") String format)
	{
		logger.trace("GET '{}/{}/_history/{}'", resourceTypeName, id, version);

		String mimeType = toSpecialMimeType(format);

		Optional<R> read = handleSql(() -> dao.readVersion(new IdType(resourceTypeName, id, version)));

		return read.map(d -> response(Status.OK, d, mimeType)).orElse(Response.status(Status.NOT_FOUND)).build();
	}

	protected String toSpecialMimeType(String format)
	{
		if (format == null || format.isBlank())
			return null;
		if (XML_FORMATS.contains(format) || JSON_FORMATS.contains(format))
			return format;
		else if ("xml".equals(format))
			return Constants.CT_FHIR_XML_NEW;
		else if ("json".equals(format))
			return Constants.CT_FHIR_JSON_NEW;
		else
			throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
	}

	protected ResponseBuilder response(Status status, Resource resource, String mimeType)
	{
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(resource, "resource");

		ResponseBuilder b = Response.status(status).entity(resource);

		if (mimeType != null)
			b = b.type(mimeType);

		if (resource.getMeta() != null)
		{
			b = b.lastModified(resource.getMeta().getLastUpdated());
			b = b.tag(resource.getMeta().getVersionId());
		}

		return b;
	}

	@PUT
	@Path("/{id}")
	@Consumes({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
			Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	public Response update(@PathParam("id") String id, R resource)
	{
		logger.trace("PUT '{}/{}'", resourceTypeName, id);

		if (!Objects.equals(id, resource.getIdElement().getIdPart()))
			return Response.status(Status.BAD_REQUEST)
					.entity(createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING, "Path id not equal to "
							+ resourceTypeName + " id (" + id + "vs." + resource.getIdElement().getIdPart() + ")."))
					.build();
		if (resource.getIdElement().getBaseUrl() != null && !serverBase.equals(resource.getIdElement().getBaseUrl()))
			return Response.status(Status.BAD_REQUEST)
					.entity(createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
							resourceTypeName + " id.baseUrl must be null or equal to " + serverBase + ", value "
									+ resource.getIdElement().getBaseUrl() + " unexpected."))
					.build();

		R updated = handleSqlAndNotFound(() -> dao.update(resource));

		return response(Status.OK, updated, null).build();
	}

	@DELETE
	@Path("/{id}")
	@Consumes({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
			Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	public Response delete(@PathParam("id") String id)
	{
		logger.trace("DELETE '{}/{}'", resourceTypeName, id);

		handleSql(() -> dao.delete(new IdType(id)));

		return response(Status.OK, null, null).build();
	}

	@POST
	@Consumes({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
			Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	public Response create(R resource, @Context UriInfo uriInfo)
	{
		logger.trace("POST '{}'", resourceTypeName);

		R createdResource = handleSql(() -> dao.create(resource));

		URI location = uriInfo.getAbsolutePathBuilder().path("/{id}/_history/{vid}")
				.build(createdResource.getIdElement().getIdPart(), createdResource.getIdElement().getVersionIdPart());

		return response(Status.CREATED, createdResource, null).location(location).build();
	}

	protected Bundle createSearchSet(PartialResult<R> tasks, UriBuilder bundleUri, String format)
	{
		Bundle bundle = new Bundle();
		bundle.setId(UUID.randomUUID().toString());
		bundle.getMeta().setLastUpdated(new Date());
		bundle.setType(BundleType.SEARCHSET);
		bundle.setEntry(tasks.getPartialResult().stream()
				.map(r -> new BundleEntryComponent().setResource(r).setFullUrl(toFullId(r.getId())))
				.collect(Collectors.toList()));
		bundle.setTotal(tasks.getOverallCount());

		if (format != null)
			bundleUri = bundleUri.replaceQueryParam("_format", format);

		if (tasks.getPageAndCount().getCount() > 0 && !tasks.getPartialResult().isEmpty())
		{
			bundleUri = bundleUri.replaceQueryParam("_count", tasks.getPageAndCount().getCount());
			bundleUri = bundleUri.replaceQueryParam("page", tasks.getPageAndCount().getPage());
		}
		else
			bundleUri = bundleUri.replaceQueryParam("_count", "0");
		bundle.addLink().setRelation("self").setUrlElement(new UriType(bundleUri.build()));

		if (tasks.getPageAndCount().getCount() > 0 && !tasks.getPartialResult().isEmpty())
		{
			bundleUri = bundleUri.replaceQueryParam("page", 1);
			bundleUri = bundleUri.replaceQueryParam("_count", tasks.getPageAndCount().getCount());
			bundle.addLink().setRelation("first").setUrlElement(new UriType(bundleUri.build()));

			if (tasks.getPageAndCount().getPage() > 1)
			{
				bundleUri = bundleUri.replaceQueryParam("page", tasks.getPageAndCount().getPage() - 1);
				bundleUri = bundleUri.replaceQueryParam("_count", tasks.getPageAndCount().getCount());
				bundle.addLink().setRelation("previous").setUrlElement(new UriType(bundleUri.build()));
			}
			if (!tasks.isLastPage())
			{
				bundleUri = bundleUri.replaceQueryParam("page", tasks.getPageAndCount().getPage() + 1);
				bundleUri = bundleUri.replaceQueryParam("_count", tasks.getPageAndCount().getCount());
				bundle.addLink().setRelation("next").setUrlElement(new UriType(bundleUri.build()));
			}

			bundleUri = bundleUri.replaceQueryParam("page", tasks.getLastPage());
			bundleUri = bundleUri.replaceQueryParam("_count", tasks.getPageAndCount().getCount());
			bundle.addLink().setRelation("last").setUrlElement(new UriType(bundleUri.build()));
		}

		return bundle;
	}
}
