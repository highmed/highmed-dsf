package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.PatientDao;
import org.hl7.fhir.r4.model.Patient;

public class PatientAuthorizationRule extends AbstractAuthorizationRule<Patient, PatientDao>
{
	public PatientAuthorizationRule(PatientDao dao)
	{
		super(dao);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, Patient newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, Patient existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, Patient oldResource, Patient newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, Patient oldResource)
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
