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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.dao.StructureDefinitionDao;
import org.highmed.fhir.search.parameters.StructureDefinitionUrl;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.service.SnapshotGenerator;
import org.highmed.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(StructureDefinitionService.RESOURCE_TYPE_NAME)
public class StructureDefinitionService extends AbstractService<StructureDefinitionDao, StructureDefinition>
{
	public static final String RESOURCE_TYPE_NAME = "StructureDefinition";

	private static final Logger logger = LoggerFactory.getLogger(StructureDefinitionService.class);

	private final SnapshotGenerator snapshotGenerator;

	public StructureDefinitionService(String serverBase, int defaultPageCount,
			StructureDefinitionDao structureDefinitionDao, ResourceValidator validator,
			SnapshotGenerator snapshotGenerator)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE_NAME, structureDefinitionDao, validator,
				StructureDefinitionUrl::new);

		this.snapshotGenerator = snapshotGenerator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(snapshotGenerator, "snapshotGenerator");
	}

	@POST
	@Path("/{snapshot : [$]snapshot(/)?}")
	public Response snapshotNew(@PathParam("snapshot") String snapshot, @QueryParam("_format") String format,
			Parameters parameters, @Context UriInfo uri)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		return Response.ok().build();
	}

	@POST
	@Path("/{id}/{snapshot : [$]snapshot(/)?}")
	public Response snapshotExisting(@PathParam("snapshot") String snapshot, @PathParam("id") String id,
			@QueryParam("_format") String format, Parameters parameters, @Context UriInfo uri)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		Optional<StructureDefinition> differential = withUuid(id,
				uuid -> handleSqlAndDeleted(() -> getDao().read(uuid)));

		return differential.map(d -> response(Status.OK, snapshotGenerator.generateSnapshot(d).getSnapshot(),
				toSpecialMimeType(format))).orElse(Response.status(Status.NOT_FOUND)).build();
	}

	@GET
	@Path("/{id}/{snapshot : [$]snapshot(/)?}")
	public Response getSnapshotExisting(@PathParam("snapshot") String snapshot, @PathParam("id") String id,
			@QueryParam("_format") String format, @Context UriInfo uri)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		//TODO 1. try get snapshot from db, 2. try generating
		
		Optional<StructureDefinition> differential = withUuid(id,
				uuid -> handleSqlAndDeleted(() -> getDao().read(uuid)));

		return differential.map(this::generateSnapshot).map(d -> response(Status.OK, d, toSpecialMimeType(format)))
				.orElse(Response.status(Status.NOT_FOUND)).build();
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
