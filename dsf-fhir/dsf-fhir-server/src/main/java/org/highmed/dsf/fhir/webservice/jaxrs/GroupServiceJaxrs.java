package org.highmed.dsf.fhir.webservice.jaxrs;

import org.highmed.dsf.fhir.webservice.specification.GroupService;
import org.hl7.fhir.r4.model.Group;

import javax.ws.rs.Path;

@Path(GroupServiceJaxrs.PATH)
public class GroupServiceJaxrs extends AbstractServiceJaxrs<Group, GroupService> implements GroupService
{
	public static final String PATH = "Group";

	public GroupServiceJaxrs(GroupService delegate)
	{
		super(delegate);
	}
}
