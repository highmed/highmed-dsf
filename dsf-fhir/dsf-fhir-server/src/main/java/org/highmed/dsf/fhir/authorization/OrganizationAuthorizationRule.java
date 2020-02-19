package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.hl7.fhir.r4.model.Organization;

public class OrganizationAuthorizationRule extends AbstractAuthorizationRule<Organization, OrganizationDao>
{
	public OrganizationAuthorizationRule(OrganizationDao dao)
	{
		super(dao);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, Organization newResource)
	{
		// check organization not existing if contains identifier with identifier.system (or extension)
		// http://highmed.org/fhir/NamingSystem/certificate-thumbprint-hex with same identifier.value
		// no two organizations can have the same certificate thumb-print
		
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, Organization existingResource)
	{
		// see create, no two organizations can have the same certificate thumb-print
		
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, Organization oldResource, Organization newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, Organization oldResource)
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
