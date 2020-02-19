package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.GroupDao;
import org.hl7.fhir.r4.model.Group;

public class GroupAuthorizationRule extends AbstractAuthorizationRule<Group, GroupDao>
{
	public GroupAuthorizationRule(GroupDao dao)
	{
		super(dao);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, Group newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, Group existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, Group oldResource, Group newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, Group oldResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
