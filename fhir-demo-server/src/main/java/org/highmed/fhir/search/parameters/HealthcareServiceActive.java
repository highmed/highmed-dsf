package org.highmed.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.fhir.function.BiFunctionWithSqlException;
import org.highmed.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.fhir.search.parameters.basic.AbstractBooleanParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.HealthcareService;

@SearchParameterDefinition(name = HealthcareServiceActive.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/HealthcareService-active", type = SearchParamType.TOKEN, documentation = "The Healthcare Service is currently marked as active [true|false]")
public class HealthcareServiceActive extends AbstractBooleanParameter<HealthcareService>
{
	public static final String PARAMETER_NAME = "active";

	public HealthcareServiceActive()
	{
		super(PARAMETER_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		return "(healthcare_service->>'active')::BOOLEAN = ?";
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
	public boolean matches(DomainResource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof HealthcareService))
			return false;

		HealthcareService h = (HealthcareService) resource;

		return h.getActive() == value;
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "(healthcare_service->>'active')::BOOLEAN" + sortDirectionWithSpacePrefix;
	}
}
