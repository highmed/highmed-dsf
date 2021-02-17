package org.highmed.dsf.fhir.history;

import java.util.Collections;
import java.util.List;

import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Resource;

public class SinceParameter extends AbstractDateTimeParameter<DomainResource>
{
	public SinceParameter()
	{
		super("_since", "last_updated");
	}

	@Override
	protected void checkParameters(List<String> parameters)
	{
		List<DateTimeValueAndTypeAndSearchType> superValuesAndTypes = super.getValuesAndTypes();

		if (superValuesAndTypes.size() > 1)
			addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_NUMBER_OF_VALUES,
					parameterName, parameters, "More than one " + parameterName + " values"));

		if (superValuesAndTypes.size() == 1)
		{
			DateTimeValueAndTypeAndSearchType vT = superValuesAndTypes.get(0);
			if (!DateTimeSearchType.EQ.equals(vT.searchType) || !DateTimeType.ZONED_DATE_TIME.equals(vT.type))
				addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE, parameterName,
						parameters, "Not instant"));
		}
	}

	@Override
	public List<DateTimeValueAndTypeAndSearchType> getValuesAndTypes()
	{
		List<DateTimeValueAndTypeAndSearchType> superValuesAndTypes = super.getValuesAndTypes();

		if (superValuesAndTypes.size() == 1)
		{
			DateTimeValueAndTypeAndSearchType vT = superValuesAndTypes.get(0);
			if (DateTimeSearchType.EQ.equals(vT.searchType) && DateTimeType.ZONED_DATE_TIME.equals(vT.type))
				return Collections
						.singletonList(new DateTimeValueAndTypeAndSearchType(vT.value, vT.type, DateTimeSearchType.GE));
		}

		return superValuesAndTypes;
	}

	@Override
	public boolean matches(Resource resource)
	{
		// Not implemented for history
		throw new UnsupportedOperationException();
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		// Not implemented for history
		throw new UnsupportedOperationException();
	}
}
