package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.postgresql.util.PGobject;

abstract class AbstractMetaTagAuthorizationRoleUserFilter extends AbstractUserFilter
{
	public AbstractMetaTagAuthorizationRoleUserFilter(User user, String resourceTable, String resourceIdColumn)
	{
		super(user, resourceTable, resourceIdColumn);
	}

	@Override
	public String getFilterQuery()
	{
		if (UserRole.LOCAL.equals(user.getRole()))
			return "(SELECT count(*) FROM read_access WHERE read_access.resource_id = " + resourceTable + "."
					+ resourceIdColumn + " AND read_access.resource_version = " + resourceTable + ".version"
					+ " AND (read_access.organization_id = ? OR read_access.access_type = 'ALL' OR read_access.access_type = 'LOCAL')) > 0";
		else
			return "(SELECT count(*) FROM read_access WHERE read_access.resource_id = " + resourceTable + "."
					+ resourceIdColumn + " AND read_access.resource_version = " + resourceTable + ".version"
					+ " AND (read_access.organization_id = ? OR read_access.access_type = 'ALL')) > 0";
	}

	@Override
	public int getSqlParameterCount()
	{
		return 1;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
	{
		String usersOrganizationId = user.getOrganization().getIdElement().getIdPart();
		statement.setObject(parameterIndex, toUuidObject(usersOrganizationId));
	}

	private PGobject toUuidObject(String uuid) throws SQLException
	{
		if (uuid == null)
			return null;

		PGobject uuidObject = new PGobject();
		uuidObject.setType("UUID");
		uuidObject.setValue(uuid);
		return uuidObject;
	}
}
