package org.highmed.fhir.webservice;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.dao.StructureDefinitionDao;
import org.highmed.fhir.dao.StructureDefinitionSnapshotDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.function.FunctionWithSqlAndResourceNotFoundException;
import org.highmed.fhir.search.PartialResult;
import org.highmed.fhir.search.SearchQuery;
import org.highmed.fhir.search.parameters.ResourceLastUpdated;
import org.highmed.fhir.search.parameters.StructureDefinitionUrl;
import org.highmed.fhir.search.parameters.basic.AbstractSearchParameter;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.service.SnapshotGenerator;
import org.highmed.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UriType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(StructureDefinitionService.RESOURCE_TYPE_NAME)
public class StructureDefinitionService extends AbstractService<StructureDefinitionDao, StructureDefinition>
{
	public static final String RESOURCE_TYPE_NAME = "StructureDefinition";

	private static final Logger logger = LoggerFactory.getLogger(StructureDefinitionService.class);

	private final StructureDefinitionSnapshotDao snapshotDao;
	private final SnapshotGenerator snapshotGenerator;

	public StructureDefinitionService(String serverBase, int defaultPageCount,
			StructureDefinitionDao structureDefinitionDao,
			StructureDefinitionSnapshotDao structureDefinitionSnapshotDao, ResourceValidator validator,
			EventManager eventManager, SnapshotGenerator snapshotGenerator)
	{
		super(serverBase, defaultPageCount, StructureDefinition.class, structureDefinitionDao, validator, eventManager,
				StructureDefinitionUrl::new);

		this.snapshotDao = structureDefinitionSnapshotDao;
		this.snapshotGenerator = snapshotGenerator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(snapshotGenerator, "snapshotGenerator");
	}

	@Override
	protected void preCreate(StructureDefinition resource) throws WebApplicationException
	{
		// TODO logging
		pre(resource, snapshotDao::create);
	}

	@Override
	protected void preUpdate(StructureDefinition resource)
	{
		// TODO logging
		pre(resource, snapshotDao::update);
	}

	private void pre(StructureDefinition resource,
			FunctionWithSqlAndResourceNotFoundException<StructureDefinition, StructureDefinition> dbOp)
	{
		if (resource.hasSnapshot())
		{
			catchAndLogSqlAndResourceNotFoundException(() -> dbOp.apply(resource));

			resource.setSnapshot(null);
		}
	}

	@Override
	protected void postCreate(StructureDefinition createdResource) throws WebApplicationException
	{
		// TODO logging
		post(createdResource, snapshotDao::create);
	}

	@Override
	protected void postUpdate(StructureDefinition updatedResource)
	{
		// TODO logging
		post(updatedResource, snapshotDao::create);
	}

	@Override
	protected void preDelete(String id)
	{
		// nothing to do
	}

	@Override
	protected void postDelete(String id)
	{
		catchAndLogSqlException(() -> snapshotDao.delete(withUuid(id)));
	}

	private void post(StructureDefinition createdResource,
			FunctionWithSqlAndResourceNotFoundException<StructureDefinition, StructureDefinition> dbOp)
	{
		boolean snapshotExists = catchAndLogSqlExceptionAndIfReturn(
				() -> snapshotDao.hasNonDeletedResource(withUuid(createdResource.getIdElement().getIdPart())),
				() -> false);

		if (!snapshotExists)
		{
			SnapshotWithValidationMessages s = snapshotGenerator.generateSnapshot(createdResource);

			if (s.getMessages().isEmpty())
				catchAndLogSqlAndResourceNotFoundException(() -> dbOp.apply(createdResource));
		}
	}

	@GET
	@Path("/{id}/{snapshot : [$]snapshot(/)?}")
	public Response getSnapshotExisting(@PathParam("snapshot") String snapshotPath, @PathParam("id") String id,
			@QueryParam("_format") String format, @Context UriInfo uri)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		Optional<StructureDefinition> snapshot = catchAndLogSqlAndResourceDeletedExceptionAndIfReturn(
				() -> snapshotDao.read(withUuid(id)), Optional::empty, Optional::empty);

		if (snapshot.isPresent())
			return snapshot.map(d -> response(Status.OK, d, toSpecialMimeType(format))).get().build();

		Optional<StructureDefinition> differential = handleSqlAndResourceDeletedException(
				() -> getDao().read(withUuid(id)));

		return differential.map(this::generateSnapshot).map(d -> response(Status.OK, d, toSpecialMimeType(format)))
				.orElse(Response.status(Status.NOT_FOUND)).build();
	}

	@POST
	@Path("/{id}/{snapshot : [$]snapshot(/)?}")
	public Response snapshotExisting(@PathParam("snapshot") String snapshotPath, @PathParam("id") String id,
			@QueryParam("_format") String format, @Context UriInfo uri)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());
		// redirecting to GET with 303 See Other
		return Response.status(Status.SEE_OTHER).location(uri.getRequestUri()).build();
	}

	@POST
	@Path("/{snapshot : [$]snapshot(/)?}")
	public Response snapshotNew(@PathParam("snapshot") String snapshotPath, @QueryParam("_format") String format,
			Parameters parameters, @Context UriInfo uri)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		Type urlType = parameters.getParameter("url");
		Optional<ParametersParameterComponent> resource = parameters.getParameter().stream()
				.filter(p -> "resource".equals(p.getName())).findFirst();

		if (urlType != null && resource.isEmpty())
		{
			if (!(urlType instanceof StringType || urlType instanceof UriType))
				return Response.status(Status.BAD_REQUEST).build(); // TODO OperationOutcome

			@SuppressWarnings("unchecked")
			PrimitiveType<String> url = (PrimitiveType<String>) urlType;

			logger.trace("Parameters with url {}", url.getValue());

			SearchQuery query = createSearchQuery(1, 1, snapshotDao, 
					createSearchParameters(new StructureDefinitionUrl("structure_definition_snapshot")));
			MultivaluedHashMap<String, String> searchParameters = new MultivaluedHashMap<>();
			searchParameters.putSingle(StructureDefinitionUrl.PARAMETER_NAME, url.getValue());
			searchParameters.putSingle(AbstractSearchParameter.SORT_PARAMETER,
					"-" + ResourceLastUpdated.PARAMETER_NAME);
			query.configureParameters(searchParameters);

			PartialResult<StructureDefinition> result = handleSqlException(() -> snapshotDao.search(query));

			Optional<StructureDefinition> snapshot = Optional
					.ofNullable(result.getPartialResult().isEmpty() ? null : result.getPartialResult().get(0));

			return snapshot.map(d -> response(Status.OK, d, toSpecialMimeType(format)))
					.orElse(Response.status(Status.NOT_FOUND)).build();
		}
		else if (urlType == null && resource.isPresent() && resource.get().getResource() != null)
		{
			if (!(resource.get().getResource() instanceof StructureDefinition))
				return Response.status(Status.BAD_REQUEST).build(); // TODO OperationOutcome

			StructureDefinition sd = (StructureDefinition) resource.get().getResource();

			logger.trace("Parameters with StructureDefinition resource url {}", sd.getUrl());

			if (!sd.hasDifferential())
				return Response.status(Status.BAD_REQUEST).build(); // TODO OperationOutcome

			if (sd.hasSnapshot())
				return response(Status.OK, sd, toSpecialMimeType(format)).build();
			else
				return response(Status.OK, generateSnapshot(sd), toSpecialMimeType(format)).build();
		}
		else
		{
			// TODO OperationOutcome resource vs. url
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	private StructureDefinition generateSnapshot(StructureDefinition differential)
	{
		SnapshotWithValidationMessages snapshot = snapshotGenerator.generateSnapshot(differential);

		if (snapshot.getMessages().isEmpty())
			return snapshot.getSnapshot();
		else
		{
			OperationOutcome outcome = new OperationOutcome();
			List<OperationOutcomeIssueComponent> issues = snapshot.getMessages().stream()
					.map(vm -> new OperationOutcomeIssueComponent().setSeverity(IssueSeverity.ERROR)
							.setCode(IssueType.STRUCTURE).setDiagnostics(vm.getMessage()))
					.collect(Collectors.toList());
			outcome.setIssue(issues);
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(outcome).build());
		}
	}
}
