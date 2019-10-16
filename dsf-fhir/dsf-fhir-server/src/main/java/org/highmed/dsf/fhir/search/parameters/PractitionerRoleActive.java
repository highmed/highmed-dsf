package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractBooleanParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = PractitionerActive.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/PractitionerRole-active", type = SearchParamType.TOKEN, documentation = "Whether this practitioner role record is in active use [true|false]")
public class PractitionerRoleActive extends AbstractBooleanParameter<PractitionerRole>
{
	public static final String PARAMETER_NAME = "active";

	public PractitionerRoleActive()
	{
		super(PARAMETER_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		return "(practitioner_role->>'active')::BOOLEAN = ?";
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
		statement.setBoolean(parameterIndex, value);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof PractitionerRole))
			return false;

		PractitionerRole p = (PractitionerRole) resource;

		return p.getActive() == value;
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "(practitioner_role->>'active')::BOOLEAN" + sortDirectionWithSpacePrefix;
	}
}
