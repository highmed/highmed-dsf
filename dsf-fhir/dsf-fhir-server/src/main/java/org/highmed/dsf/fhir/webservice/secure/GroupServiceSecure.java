package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.GroupService;
import org.hl7.fhir.r4.model.Group;

public class GroupServiceSecure extends AbstractServiceSecure<Group, GroupService> implements GroupService
{
	public GroupServiceSecure(GroupService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
