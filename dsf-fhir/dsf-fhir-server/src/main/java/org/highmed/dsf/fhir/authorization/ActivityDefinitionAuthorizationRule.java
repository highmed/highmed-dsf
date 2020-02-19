package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.ActivityDefinitionDao;
import org.hl7.fhir.r4.model.ActivityDefinition;

public class ActivityDefinitionAuthorizationRule
		extends AbstractAuthorizationRule<ActivityDefinition, ActivityDefinitionDao>
{
	public ActivityDefinitionAuthorizationRule(ActivityDefinitionDao dao)
	{
		super(dao);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, ActivityDefinition newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, ActivityDefinition existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, ActivityDefinition oldResource,
			ActivityDefinition newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, ActivityDefinition oldResource)
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
