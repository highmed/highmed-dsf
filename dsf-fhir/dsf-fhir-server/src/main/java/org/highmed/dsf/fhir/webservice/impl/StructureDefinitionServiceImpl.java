package org.highmed.dsf.fhir.webservice.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionSnapshotDao;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventManager;
import org.highmed.dsf.fhir.function.ConsumerWithSqlAndResourceNotFoundException;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.parameters.ResourceLastUpdated;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionUrl;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceValidator;
import org.highmed.dsf.fhir.service.SnapshotDependencies;
import org.highmed.dsf.fhir.service.SnapshotDependencyAnalyzer;
import org.highmed.dsf.fhir.service.SnapshotGenerator;
import org.highmed.dsf.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.highmed.dsf.fhir.service.SnapshotInfo;
import org.highmed.dsf.fhir.webservice.specification.StructureDefinitionService;
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

	public StructureDefinitionServiceImpl(String resourceTypeName, String serverBase, String path, int defaultPageCount,
			StructureDefinitionDao dao, ResourceValidator validator, EventManager eventManager,
			ExceptionHandler exceptionHandler, EventGenerator eventGenerator, ResponseGenerator responseGenerator,
			ParameterConverter parameterConverter, StructureDefinitionSnapshotDao structureDefinitionSnapshotDao,
			SnapshotGenerator sanapshotGenerator, SnapshotDependencyAnalyzer snapshotDependencyAnalyzer,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver)
	{
		super(StructureDefinition.class, resourceTypeName, serverBase, path, defaultPageCount, dao, validator,
				eventManager, exceptionHandler, eventGenerator, responseGenerator, parameterConverter,
				referenceExtractor, referenceResolver);

		this.snapshotDao = structureDefinitionSnapshotDao;
		this.snapshotGenerator = sanapshotGenerator;
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

		return postCreate(forPost);
	}

	@Override
	protected Consumer<StructureDefinition> preUpdate(StructureDefinition resource)
	{
		StructureDefinition forPost = resource.hasSnapshot() ? resource.copy() : null;

		resource.setSnapshot(null);

		return postUpdate(forPost);
	}

	private Consumer<StructureDefinition> postCreate(StructureDefinition preResource)
	{
		return postResource ->
		{
			if (preResource != null && preResource.hasSnapshot())
			{
				handleSnapshot(preResource,
						info -> snapshotDao.create(
								parameterConverter.toUuid(resourceTypeName, postResource.getIdElement().getIdPart()),
								preResource, info));
			}
			else if (postResource != null)
			{
				try
				{
					SnapshotWithValidationMessages s = snapshotGenerator.generateSnapshot(postResource);

					if (s != null && s.getSnapshot() != null && s.getMessages().isEmpty())
						handleSnapshot(s.getSnapshot(), info -> snapshotDao.create(
								parameterConverter.toUuid(resourceTypeName, postResource.getIdElement().getIdPart()),
								postResource, info));
				}
				catch (Exception e)
				{
					logger.warn("Error while generating snapshot for StructureDefinition with id "
							+ postResource.getIdElement().getIdPart(), e);
				}
			}
		};
	}

	private Consumer<StructureDefinition> postUpdate(StructureDefinition preResource)
	{
		return postResource ->
		{
			if (preResource != null && preResource.hasSnapshot())
			{
				handleSnapshot(preResource, info -> snapshotDao.update(preResource, info));
			}
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

		exceptionHandler.catchAndLogSqlException(() -> snapshotDao.deleteAllByDependency(snapshot.getUrl()));

		exceptionHandler.catchAndLogSqlAndResourceNotFoundException(resourceTypeName,
				() -> dbOp.accept(new SnapshotInfo(dependencies)));
	}

	@Override
	protected Consumer<String> preDelete(String id)
	{
		return this::afterDelete;
	}

	private void afterDelete(String id)
	{
		exceptionHandler.catchAndLogSqlAndResourceNotFoundException(resourceTypeName,
				() -> snapshotDao.delete(parameterConverter.toUuid(resourceTypeName, id)));
	}

	@Override
	public Response postSnapshotNew(String snapshotPath, Parameters parameters, UriInfo uri, HttpHeaders headers)
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

			return getSnapshot(url.getValue(), uri, headers);
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
				return responseGenerator.response(Status.OK, sd, parameterConverter.getMediaType(uri, headers)).build();
			else
				return responseGenerator
						.response(Status.OK, generateSnapshot(sd), parameterConverter.getMediaType(uri, headers))
						.build();
		}
		else
		{
			// TODO OperationOutcome resource vs. url
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	private Response getSnapshot(String url, UriInfo uri, HttpHeaders headers)
	{
		SearchQuery<StructureDefinition> query = snapshotDao.createSearchQuery(1, 1);
		Map<String, List<String>> searchParameters = new HashMap<>();
		searchParameters.put(StructureDefinitionUrl.PARAMETER_NAME, Collections.singletonList(url));
		searchParameters.put(SearchQuery.PARAMETER_SORT,
				Collections.singletonList("-" + ResourceLastUpdated.PARAMETER_NAME));
		query.configureParameters(searchParameters);

		PartialResult<StructureDefinition> result = exceptionHandler
				.handleSqlException(() -> snapshotDao.search(query));

		Optional<StructureDefinition> snapshot = Optional
				.ofNullable(result.getPartialResult().isEmpty() ? null : result.getPartialResult().get(0));

		return snapshot
				.map(d -> responseGenerator.response(Status.OK, d, parameterConverter.getMediaType(uri, headers)))
				.orElse(Response.status(Status.NOT_FOUND)).build();
	}

	@Override
	public Response getSnapshotNew(String snapshotPath, UriInfo uri, HttpHeaders headers)
	{
		return getSnapshot(uri.getQueryParameters().getFirst("url"), uri, headers);
	}

	@Override
	public Response postSnapshotExisting(String snapshotPath, String id, UriInfo uri, HttpHeaders headers)
	{
		return getSnapshotExisting(snapshotPath, id, uri, headers);
	}

	@Override
	public Response getSnapshotExisting(String snapshotPath, String id, UriInfo uri, HttpHeaders headers)
	{
		Optional<StructureDefinition> snapshot = exceptionHandler.catchAndLogSqlAndResourceDeletedExceptionAndIfReturn(
				() -> snapshotDao.read(parameterConverter.toUuid(resourceTypeName, id)), Optional::empty,
				Optional::empty);

		if (snapshot.isPresent())
			return snapshot
					.map(d -> responseGenerator.response(Status.OK, d, parameterConverter.getMediaType(uri, headers)))
					.get().build();

		Optional<StructureDefinition> differential = exceptionHandler.handleSqlAndResourceDeletedException(
				resourceTypeName, () -> dao.read(parameterConverter.toUuid(resourceTypeName, id)));

		return differential.map(this::generateSnapshot)
				.map(d -> responseGenerator.response(Status.OK, d, parameterConverter.getMediaType(uri, headers)))
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
