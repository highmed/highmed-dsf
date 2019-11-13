package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractBooleanParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = PractitionerActive.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Practitioner-active", type = SearchParamType.TOKEN, documentation = "Whether the practitioner record is active [true|false]")
public class PractitionerActive extends AbstractBooleanParameter<Practitioner>
{
	public static final String PARAMETER_NAME = "active";

	public PractitionerActive()
	{
		super(PARAMETER_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		return "(practitioner->>'active')::BOOLEAN = ?";
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

		if (!(resource instanceof Practitioner))
			return false;

		Practitioner p = (Practitioner) resource;

		return p.getActive() == value;
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "(practitioner->>'active')::BOOLEAN" + sortDirectionWithSpacePrefix;
	}
}
