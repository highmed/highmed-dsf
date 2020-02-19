package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.BinaryDao;
import org.hl7.fhir.r4.model.Binary;

public class BinaryAuthorizationRule extends AbstractAuthorizationRule<Binary, BinaryDao>
{
	public BinaryAuthorizationRule(BinaryDao dao)
	{
		super(dao);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, Binary newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, Binary existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, Binary oldResource, Binary newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, Binary oldResource)
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
