package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.highmed.fhir.dao.DomainResourceDao;
import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;

public abstract class AbstractCommandWithResource<R extends DomainResource, D extends DomainResourceDao<R>>
		extends AbstractCommand implements Command
{
	protected final R resource;
	protected final D dao;
	protected final ExceptionHandler exceptionHandler;
	protected final ParameterConverter parameterConverter;

	public AbstractCommandWithResource(int transactionPriority, int index, Bundle bundle, BundleEntryComponent entry,
			String serverBase, R resource, D dao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter)
	{
		super(transactionPriority, index, bundle, entry, serverBase);

		this.resource = resource;
		this.dao = dao;
		this.exceptionHandler = exceptionHandler;
		this.parameterConverter = parameterConverter;
	}

	protected R latest(Map<String, IdType> idTranslationTable, Connection connection) throws SQLException
	{
		try
		{
			String id = idTranslationTable.get(entry.getFullUrl()).getIdPart();
			return dao.readWithTransaction(connection, parameterConverter.toUuid(resource.getResourceType().name(), id))
					.orElseThrow(() -> exceptionHandler.internalServerError(new ResourceNotFoundException(id)));
		}
		catch (ResourceDeletedException e)
		{
			throw exceptionHandler.internalServerError(e);
		}
	}
}
