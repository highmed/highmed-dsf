package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response.Status;

import org.highmed.fhir.dao.DomainResourceDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class UpdateCommand<R extends DomainResource, D extends DomainResourceDao<R>>
		extends AbstractCommandWithResource<R, D> implements Command
{
	private static final Logger logger = LoggerFactory.getLogger(UpdateCommand.class);

	protected final ResponseGenerator responseGenerator;
	protected final EventManager eventManager;
	protected final EventGenerator eventGenerator;

	protected R updatedResource;

	public UpdateCommand(int index, Bundle bundle, BundleEntryComponent entry, String serverBase, R resource, D dao,
			ExceptionHandler exceptionHandler, ParameterConverter parameterConverter,
			ResponseGenerator responseGenerator, EventManager eventManager, EventGenerator eventGenerator)
	{
		super(3, index, bundle, entry, serverBase, resource, dao, exceptionHandler, parameterConverter);

		this.responseGenerator = responseGenerator;
		this.eventManager = eventManager;
		this.eventGenerator = eventGenerator;
	}

	@Override
	public void preExecute(Map<String, IdType> idTranslationTable)
	{
		// TODO validate entry.getFullUrl() vs resource.getIdElement()
		// TODO validate entry.getFullUrl() is urn:uuid:... if conditional update
		// TODO validate entry.getFullUrl() is id with server base matching if direct update with id
	}

	@Override
	public void execute(Map<String, IdType> idTranslationTable, Connection connection)
			throws SQLException, WebApplicationException
	{
		UriComponents componentes = UriComponentsBuilder.fromUriString(entry.getRequest().getUrl()).build();

		if (componentes.getPathSegments().size() == 2 && componentes.getQueryParams().isEmpty())
			updateById(idTranslationTable, connection, componentes.getPathSegments().get(0),
					componentes.getPathSegments().get(1));
		else if (componentes.getPathSegments().size() == 1 && !componentes.getQueryParams().isEmpty())
			updateByCondition(idTranslationTable, connection, componentes.getPathSegments().get(0),
					parameterConverter.cleanQueryParameters(componentes.getQueryParams()));
		else
			throw new WebApplicationException(
					responseGenerator.badDeleteRequestUrl(index, entry.getRequest().getUrl()));
	}

	private void updateById(Map<String, IdType> idTranslationTable, Connection connection, String resourceTypeName,
			String pathId) throws SQLException
	{
		IdType resourceId = resource.getIdElement();

		if (!Objects.equals(pathId, resourceId.getIdPart()))
			throw new WebApplicationException(
					responseGenerator.pathVsElementIdInBundle(index, resourceTypeName, pathId, resourceId));
		if (resourceId.getBaseUrl() != null && !serverBase.equals(resourceId.getBaseUrl()))
			throw new WebApplicationException(
					responseGenerator.invalidBaseUrlInBundle(index, resourceTypeName, resourceId));

		R latest = latest(idTranslationTable, connection);

		if (!Objects.equals(resourceTypeName, latest.getResourceType().name()))
			throw new WebApplicationException(responseGenerator.nonMatchingResourceTypeAndRequestUrlInBundle(index,
					resourceTypeName, entry.getRequest().getUrl()));

		Optional<Long> ifMatch = Optional.ofNullable(entry.getRequest().getIfMatch())
				.flatMap(parameterConverter::toEntityTag).flatMap(parameterConverter::toVersion);

		updatedResource = exceptionHandler.handleSqlExAndResourceNotFoundExForUpdateAsCreateAndResouceVersionNonMatchEx(
				resourceTypeName, () -> dao.updateWithTransaction(connection, latest, ifMatch.orElse(null)));
	}

	private void updateByCondition(Map<String, IdType> idTranslationTable, Connection connection,
			String resourceTypeName, Map<String, List<String>> queryParameters)
	{
		// TODO
		// add actual id of matching resource to idTranslationTable
	}

	@Override
	public BundleEntryComponent postExecute()
	{
		try
		{
			eventManager.handleEvent(eventGenerator.newResourceUpdatedEvent(updatedResource));
		}
		catch (Exception e)
		{
			logger.warn("Error while handling resource updated event", e);
		}

		BundleEntryComponent resultEntry = new BundleEntryComponent();
		resultEntry.setFullUrl(new IdType(serverBase, updatedResource.getResourceType().name(),
				updatedResource.getIdElement().getIdPart(), null).getValue());
		BundleEntryResponseComponent response = resultEntry.getResponse();
		response.setStatus(Status.OK.getStatusCode() + " " + Status.OK.getReasonPhrase());
		response.setLocation(updatedResource.getIdElement()
				.withServerBase(serverBase, updatedResource.getResourceType().name()).getValue());
		response.setEtag(new EntityTag(updatedResource.getMeta().getVersionId(), true).toString());
		response.setLastModified(updatedResource.getMeta().getLastUpdated());

		return resultEntry;
	}
}
