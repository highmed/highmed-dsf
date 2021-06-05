package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.hl7.fhir.r4.model.Identifier;

abstract class AbstractMetaTagAuthorizationRoleUserFilter extends AbstractUserFilter
{
	public AbstractMetaTagAuthorizationRoleUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}

	@Override
	public String getFilterQuery()
	{
		return "(" + getFilterQueryBase(resourceColumn) + ")";
	}

	protected String getFilterQueryBase(String resource)
	{
		String query = resource + "->'meta'->'tag' @> ?::jsonb OR " + resource
				+ "->'meta'->'tag' @> ?::jsonb OR (SELECT count(*) FROM ("
				+ "SELECT jsonb_path_query(e,'$[*]?(@.url==\""
				+ ReadAccessHelper.EXTENSION_READ_ACCESS_CONSORTIUM_ROLE_CONSORTIUM
				+ "\").valueIdentifier')->>'value' AS parent_identifier_value" + ",jsonb_path_query(e,'$[*]?(@.url==\""
				+ ReadAccessHelper.EXTENSION_READ_ACCESS_CONSORTIUM_ROLE_ROLE + "\").valueCoding') AS member_role "
				+ "FROM (" + "SELECT jsonb_path_query(" + resource + "," + "'$.meta.tag[*]?(@.code==\""
				+ ReadAccessHelper.READ_ACCESS_TAG_VALUE_ROLE + "\"&&@.system==\""
				+ ReadAccessHelper.READ_ACCESS_TAG_SYSTEM + "\").extension[*]?" + "(@.url==\""
				+ ReadAccessHelper.EXTENSION_READ_ACCESS_CONSORTIUM_ROLE + "\").extension') AS e"
				+ ") AS tag_extensions " + "INTERSECT " + "SELECT "
				+ "jsonb_path_query(p.parent->'identifier','$[*]?(@.system==\""
				+ ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM + "\")')->>'value' AS parent_identifier_value"
				+ ",jsonb_array_elements(jsonb_array_elements(a.a->'code')->'coding') AS member_role " + "FROM "
				+ "(SELECT organization_affiliation AS a FROM current_organization_affiliations WHERE (organization_affiliation->>'active')::boolean) AS a "
				+ "LEFT JOIN "
				+ "(SELECT organization AS member FROM current_organizations WHERE (organization->>'active')::boolean) AS m "
				+ "ON a->'participatingOrganization'->>'reference' = ('Organization/' || (member->>'id')) "
				+ "LEFT JOIN "
				+ "(SELECT organization AS parent FROM current_organizations WHERE (organization->>'active')::boolean) AS p "
				+ "ON a->'organization'->>'reference' = ('Organization/' || (parent->>'id')) "
				+ "WHERE m.member->'identifier' @> ?::jsonb" + ") AS memberships) > 0";

		if (UserRole.LOCAL.equals(user.getRole()))
			query += " OR " + resource + "->'meta'->'tag' @> ?::jsonb";

		return query;
	}

	@Override
	public int getSqlParameterCount()
	{
		if (UserRole.LOCAL.equals(user.getRole()))
			return 4;
		else
			return 3;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
	{
		String organizationIdentifierValue = getOrganizationIdentifier().orElseThrow(() -> new NoSuchElementException(
				"Users organization has no identifier with system " + ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM));

		if (subqueryParameterIndex == 1)
		{
			statement.setString(parameterIndex, "[{\"system\":\"" + ReadAccessHelper.READ_ACCESS_TAG_SYSTEM
					+ "\",\"code\":\"" + ReadAccessHelper.READ_ACCESS_TAG_VALUE_ALL + "\"}]");
		}
		else if (subqueryParameterIndex == 2)
		{
			statement.setString(parameterIndex,
					"[{\"extension\":[{\"url\":\"" + ReadAccessHelper.EXTENSION_READ_ACCESS_ORGANIZATION
							+ "\",\"valueIdentifier\":{\"system\":\"" + ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM
							+ "\",\"value\":\"" + organizationIdentifierValue + "\"}}],\"system\":\""
							+ ReadAccessHelper.READ_ACCESS_TAG_SYSTEM + "\",\"code\":\""
							+ ReadAccessHelper.READ_ACCESS_TAG_VALUE_ORGANIZATION + "\"}]");
		}
		else if (subqueryParameterIndex == 3)
		{
			statement.setString(parameterIndex, "[{\"system\":\"" + ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM
					+ "\",\"value\":\"" + organizationIdentifierValue + "\"}]");
		}
		else if (subqueryParameterIndex == 4)
		{
			statement.setString(parameterIndex, "[{\"system\":\"" + ReadAccessHelper.READ_ACCESS_TAG_SYSTEM
					+ "\",\"code\":\"" + ReadAccessHelper.READ_ACCESS_TAG_VALUE_LOCAL + "\"}]");
		}
		else
			throw new IllegalStateException("Unexpected subqueryParameterIndex " + subqueryParameterIndex);
	}

	private Optional<String> getOrganizationIdentifier()
	{
		return user == null ? Optional.empty()
				: user.getOrganization().getIdentifier().stream().filter(i -> i != null).filter(Identifier::hasSystem)
						.filter(i -> ReadAccessHelper.ORGANIZATION_IDENTIFIER_SYSTEM.equals(i.getSystem()))
						.filter(Identifier::hasValue).findFirst().map(Identifier::getValue);
	}
}
