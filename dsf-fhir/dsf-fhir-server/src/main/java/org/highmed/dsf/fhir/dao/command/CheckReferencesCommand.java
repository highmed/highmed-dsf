package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.highmed.dsf.fhir.validation.SnapshotGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckReferencesCommand<R extends Resource, D extends ResourceDao<R>>
		extends AbstractCommandWithResource<R, D> implements Command
{
	private static final Logger logger = LoggerFactory.getLogger(CheckReferencesCommand.class);

	private final HTTPVerb verb;

	public CheckReferencesCommand(int index, User user, PreferReturnType returnType, Bundle bundle,
			BundleEntryComponent entry, String serverBase, AuthorizationHelper authorizationHelper, R resource,
			HTTPVerb verb, D dao, ExceptionHandler exceptionHandler, ParameterConverter parameterConverter,
			ResponseGenerator responseGenerator, ReferenceExtractor referenceExtractor,
			ReferenceResolver referenceResolver)
	{
		super(4, index, user, returnType, bundle, entry, serverBase, authorizationHelper, resource, dao,
				exceptionHandler, parameterConverter, responseGenerator, referenceExtractor, referenceResolver);

		this.verb = verb;
	}

	@Override
	public void execute(Map<String, IdType> idTranslationTable, Connection connection,
			ValidationHelper validationHelper, SnapshotGenerator snapshotGenerator)
			throws SQLException, WebApplicationException
	{
		referencesHelper.checkReferences(idTranslationTable, connection, this::checkReferenceAfterUpdate);
	}

	// See also TaskServiceImpl#checkReferenceAfterUpdate
	// See also AbstractResourceServiceImpl#checkReferenceAfterUpdate
	// See also AbstractResourceServiceImpl#checkReferenceAfterCreate
	private boolean checkReferenceAfterUpdate(ResourceReference ref)
	{
		if (resource instanceof Task && HTTPVerb.PUT.equals(verb))
		{
			Task task = (Task) resource;
			if (EnumSet.of(TaskStatus.COMPLETED, TaskStatus.FAILED).contains(task.getStatus()))
			{
				ReferenceType refType = ref.getType(serverBase);
				if ("Task.input".equals(ref.getLocation()) && ReferenceType.LITERAL_EXTERNAL.equals(refType))
				{
					logger.warn("Skipping check of {} reference '{}' at {} in resource with {}, version {}", refType,
							ref.getReference().getReference(), "Task.input", resource.getIdElement().getIdPart(),
							// we are checking against the pre-update resource
							resource.getIdElement().getVersionIdPartAsLong() + 1);
					return false;
				}
			}
		}

		return true;
	}
}
