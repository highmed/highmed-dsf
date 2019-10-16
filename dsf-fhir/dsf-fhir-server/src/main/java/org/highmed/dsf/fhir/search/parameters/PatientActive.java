package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractBooleanParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = PatientActive.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Patient-active", type = SearchParamType.TOKEN, documentation = "Whether the patient record is active [true|false]")
public class PatientActive extends AbstractBooleanParameter<Patient>
{
	public static final String PARAMETER_NAME = "active";

	public PatientActive()
	{
		super(PARAMETER_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		return "(patient->>'active')::BOOLEAN = ?";
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

		if (!(resource instanceof Patient))
			return false;

		Patient p = (Patient) resource;

		return p.getActive() == value;
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "(patient->>'active')::BOOLEAN" + sortDirectionWithSpacePrefix;
	}
}
