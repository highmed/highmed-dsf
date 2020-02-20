package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryUserFilter extends AbstractUserFilter
{
	private static final Logger logger = LoggerFactory.getLogger(BinaryUserFilter.class);

	public BinaryUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user);
	}

	@Override
	public String getFilterQuery()
	{
		if (UserRole.LOCAL.equals(user.getRole()))
			return "";
		else
		{
			logger.warn("Filter query for non local user -> 'false'");
			return "false";
		}
	}

	@Override
	public int getSqlParameterCount()
	{
		return 0;
	}

	@Override
	public void modifyStatement(int parameterIndex, PreparedStatement statement) throws SQLException
	{
		// nothing to do
	}
}
