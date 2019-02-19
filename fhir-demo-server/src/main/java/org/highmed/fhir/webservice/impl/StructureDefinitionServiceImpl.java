package org.highmed.fhir.webservice.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
import org.highmed.fhir.function.ConsumerWithSqlAndResourceNotFoundException;
import org.highmed.fhir.search.PartialResult;
import org.highmed.fhir.search.SearchQuery;
import org.highmed.fhir.search.parameters.ResourceLastUpdated;
import org.highmed.fhir.search.parameters.StructureDefinitionUrl;
import org.highmed.fhir.search.parameters.basic.AbstractSearchParameter;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.service.SnapshotDependencies;
import org.highmed.fhir.service.SnapshotDependencyAnalyzer;
import org.highmed.fhir.service.SnapshotGenerator;
import org.highmed.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.highmed.fhir.service.SnapshotInfo;
import org.highmed.fhir.webservice.specification.StructureDefinitionService;
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

public class StructureDefinitionServiceImpl extends AbstractServiceImpl<StructureDefinitionDao, StructureDefinition>
		implements StructureDefinitionService
{
	private static final Logger logger = LoggerFactory.getLogger(StructureDefinitionServiceImpl.class);

	private final StructureDefinitionSnapshotDao snapshotDao;
	private final SnapshotGenerator snapshotGenerator;
	private final SnapshotDependencyAnalyzer snapshotDependencyAnalyzer;

	public StructureDefinitionServiceImpl(String serverBase, int defaultPageCount,
			StructureDefinitionDao structureDefinitionDao, ResourceValidator validator, EventManager eventManager,
			ServiceHelperImpl<StructureDefinition> serviceHelper,
			StructureDefinitionSnapshotDao structureDefinitionSnapshotDao, SnapshotGenerator anapshotGenerator,
			SnapshotDependencyAnalyzer snapshotDependencyAnalyzer)
	{
		super(serverBase, defaultPageCount, structureDefinitionDao, validator, eventManager, serviceHelper);

		this.snapshotDao = structureDefinitionSnapshotDao;
		this.snapshotGenerator = anapshotGenerator;
		this.snapshotDependencyAnalyzer = snapshotDependencyAnalyzer;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(snapshotGenerator, "snapshotGenerator");
	}

	@Override
	protected Consumer<StructureDefinition> preCreate(StructureDefinition resource) throws WebApplicationException
	{
		StructureDefinition forPost = resource.hasSnapshot() ? resource.copy() : null;

		resource.setSnapshot(null);

		return postCreateOrUpdate(forPost);
	}

	@Override
	protected Consumer<StructureDefinition> preUpdate(StructureDefinition resource)
	{
		return postCreateOrUpdate(resource);
	}

	private Consumer<StructureDefinition> postCreateOrUpdate(StructureDefinition preResource)
	{
		return postResource ->
		{
			if (preResource != null && preResource.hasSnapshot())
				handleSnapshot(preResource, info -> snapshotDao
						.create(serviceHelper.withUuid(postResource.getIdElement().getIdPart()), preResource, info));
			else if (postResource != null)
			{
				try
				{
					SnapshotWithValidationMessages s = snapshotGenerator.generateSnapshot(postResource);

					if (s != null && s.getSnapshot() != null && s.getMessages().isEmpty())
						handleSnapshot(s.getSnapshot(), info -> snapshotDao.update(s.getSnapshot(), info));
				}
				catch (Exception e)
				{
					logger.warn("Error while generating snapshot for StructureDefinition with id "
							+ postResource.getIdElement().getIdPart(), e);
				}
			}
		};
	}

	private void handleSnapshot(StructureDefinition snapshot,
			ConsumerWithSqlAndResourceNotFoundException<SnapshotInfo> dbOp)
	{
		SnapshotDependencies dependencies = snapshotDependencyAnalyzer.analyzeSnapshotDependencies(snapshot);

		serviceHelper.catchAndLogSqlException(() -> snapshotDao.deleteAllByDependency(snapshot.getUrl()));

		serviceHelper.catchAndLogSqlAndResourceNotFoundException(() -> dbOp.accept(new SnapshotInfo(dependencies)));
	}

	@Override
	protected Consumer<String> beforeDelete(String id)
	{
		return this::afterDelete;
	}

	private void afterDelete(String id)
	{
		serviceHelper.catchAndLogSqlException(() -> snapshotDao.delete(serviceHelper.withUuid(id)));
	}

	@Override
	public Response postSnapshotNew(@PathParam("snapshot") String snapshotPath, @QueryParam("_format") String format,
			Parameters parameters, @Context UriInfo uri)
	{
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

			SearchQuery query = snapshotDao.createSearchQuery(1, 1);
			MultivaluedHashMap<String, String> searchParameters = new MultivaluedHashMap<>();
			searchParameters.putSingle(StructureDefinitionUrl.PARAMETER_NAME, url.getValue());
			searchParameters.putSingle(AbstractSearchParameter.SORT_PARAMETER,
					"-" + ResourceLastUpdated.PARAMETER_NAME);
			query.configureParameters(searchParameters);

			PartialResult<StructureDefinition> result = serviceHelper
					.handleSqlException(() -> snapshotDao.search(query));

			Optional<StructureDefinition> snapshot = Optional
					.ofNullable(result.getPartialResult().isEmpty() ? null : result.getPartialResult().get(0));

			return snapshot.map(d -> serviceHelper.response(Status.OK, d, serviceHelper.toSpecialMimeType(format)))
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
				return serviceHelper.response(Status.OK, sd, serviceHelper.toSpecialMimeType(format)).build();
			else
				return serviceHelper.response(Status.OK, generateSnapshot(sd), serviceHelper.toSpecialMimeType(format))
						.build();
		}
		else
		{
			// TODO OperationOutcome resource vs. url
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@Override
	public Response getSnapshotNew(String snapshotPath, String url, String format, UriInfo uri)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response postSnapshotExisting(@PathParam("snapshot") String snapshotPath, @PathParam("id") String id,
			@QueryParam("_format") String format, @Context UriInfo uri)
	{
		return getSnapshotExisting(snapshotPath, id, format, uri);
	}

	@Override
	public Response getSnapshotExisting(@PathParam("snapshot") String snapshotPath, @PathParam("id") String id,
			@QueryParam("_format") String format, @Context UriInfo uri)
	{
		Optional<StructureDefinition> snapshot = serviceHelper.catchAndLogSqlAndResourceDeletedExceptionAndIfReturn(
				() -> snapshotDao.read(serviceHelper.withUuid(id)), Optional::empty, Optional::empty);

		if (snapshot.isPresent())
			return snapshot.map(d -> serviceHelper.response(Status.OK, d, serviceHelper.toSpecialMimeType(format)))
					.get().build();

		Optional<StructureDefinition> differential = serviceHelper
				.handleSqlAndResourceDeletedException(() -> dao.read(serviceHelper.withUuid(id)));

		return differential.map(this::generateSnapshot)
				.map(d -> serviceHelper.response(Status.OK, d, serviceHelper.toSpecialMimeType(format)))
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
