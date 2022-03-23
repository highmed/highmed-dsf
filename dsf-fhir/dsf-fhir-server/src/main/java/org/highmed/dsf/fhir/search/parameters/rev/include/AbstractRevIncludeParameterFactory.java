package org.highmed.dsf.fhir.search.parameters.rev.include;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.search.IncludeParts;
import org.highmed.dsf.fhir.search.SearchQueryIncludeParameter;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.search.SearchQueryRevIncludeParameterFactory;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Resource;

public abstract class AbstractRevIncludeParameterFactory implements SearchQueryRevIncludeParameterFactory
{
	private final List<SearchQueryParameterError> errors = new ArrayList<>();

	/**
	 * The name of the source resource from which the join comes
	 */
	private final String resourceTypeName;
	/**
	 * The name of the search parameter which must be of type reference
	 */
	private final String parameterName;
	/**
	 * (Optional) A specific of type of target resource (for when the search parameter refers to multiple possible
	 * target types)
	 */
	private final List<String> targetResourceTypeNames;

	private List<IncludeParts> includeParts;

	/**
	 * @param resourceTypeName
	 *            The name of the source resource from which the join comes
	 * @param parameterName
	 *            The name of the search parameter which must be of type reference
	 * @param targetResourceTypeName
	 *            target resource type
	 */
	public AbstractRevIncludeParameterFactory(String resourceTypeName, String parameterName,
			String... targetResourceTypeName)
	{
		this.resourceTypeName = resourceTypeName;
		this.parameterName = parameterName;
		this.targetResourceTypeNames = Arrays.asList(targetResourceTypeName);
	}

	@Override
	public void configure(List<String> revIncludeParameters)
	{
		includeParts = getRevIncludeParts(revIncludeParameters);
	}

	private List<IncludeParts> getRevIncludeParts(List<String> revIncludeParameterValues)
	{
		List<IncludeParts> includeParts = revIncludeParameterValues.stream().map(IncludeParts::fromString)
				.filter(p -> resourceTypeName.equals(p.getSourceResourceTypeName())
						&& parameterName.equals(p.getSearchParameterName())
						&& ((targetResourceTypeNames.size() == 1 && p.getTargetResourceTypeName() == null)
								|| targetResourceTypeNames.contains(p.getTargetResourceTypeName())))
				.collect(Collectors.toList());

		return includeParts;
	}

	protected final void addError(SearchQueryParameterError error)
	{
		errors.add(error);
	}

	@Override
	public List<SearchQueryParameterError> getErrors()
	{
		return Collections.unmodifiableList(errors);
	}

	protected abstract String getRevIncludeSql(IncludeParts includeParts);

	/**
	 * Use this method to modify the include resources. This method can be used if the resources returned by the include
	 * SQL are not complete and additional content needs to be retrieved from a not included column. For example the
	 * content of a {@link Binary} resource might not be stored in the json column.
	 *
	 * @param resource
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 */
	protected abstract void modifyIncludeResource(Resource resource, Connection connection);

	@Override
	public List<SearchQueryIncludeParameter> getRevIncludeParameters()
	{
		return includeParts.stream()
				.map(ip -> new SearchQueryIncludeParameter(getRevIncludeSql(ip), ip, this::modifyIncludeResource))
				.collect(Collectors.toList());
	}
}
