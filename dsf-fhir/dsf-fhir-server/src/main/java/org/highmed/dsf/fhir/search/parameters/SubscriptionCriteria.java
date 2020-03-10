package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractStringParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Subscription;

@SearchParameterDefinition(name = SubscriptionCriteria.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Subscription-criteria", type = SearchParamType.STRING, documentation = "The search rules used to determine when to send a notification (always matches exact)")
public class SubscriptionCriteria extends AbstractStringParameter<Subscription>
{
	public static final String PARAMETER_NAME = "criteria";

	public SubscriptionCriteria()
	{
		super(PARAMETER_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		return "subscription->>'criteria' = ?";
	}

	@Override
	public int getSqlParameterCount()
	{
		return 1;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement,
			BiFunctionWithSqlException<String, Object[], Array> arrayCreator) throws SQLException
	{
		statement.setString(parameterIndex, valueAndType.value);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Subscription))
			return false;

		Subscription s = (Subscription) resource;

		return Objects.equals(valueAndType.value, s.getCriteria());
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "subscription->>'criteria'" + sortDirectionWithSpacePrefix;
	}

}
