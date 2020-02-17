package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.dao.GroupDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.GroupService;
import org.hl7.fhir.r4.model.Group;

public class GroupServiceSecure extends AbstractResourceServiceSecure<GroupDao, Group, GroupService>
		implements GroupService
{
	public GroupServiceSecure(GroupService delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, GroupDao groupDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, Group.class, groupDao, exceptionHandler,
				parameterConverter);
	}
}
