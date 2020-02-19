package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientUserFilter extends AbstractUserFilter
{
	private static final Logger logger = LoggerFactory.getLogger(PatientUserFilter.class);

	public PatientUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user);
	}

	@Override
	public String getFilterQuery()
	{
		// TODO implement

		logger.warn("{}#getFilterQuery not implemented yet", getClass().getName());
		return "false";
	}

	@Override
	public int getSqlParameterCount()
	{
		logger.warn("{}#getSqlParameterCount not implemented yet", getClass().getName());
		return 0;
	}

	@Override
	public void modifyStatement(int parameterIndex, PreparedStatement statement) throws SQLException
	{
		// TODO implement

		logger.warn("{}#modifyStatement not implemented yet", getClass().getName());
	}
}
