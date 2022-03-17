package org.highmed.dsf.fhir.search;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.ws.rs.core.UriBuilder;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;

public interface SearchQueryParameter<R extends Resource> extends MatcherParameter
{
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface SearchParameterDefinition
	{
		String name();

		String definition();

		SearchParamType type();

		String documentation();
	}

	void configure(Map<String, List<String>> queryParameters);

	List<SearchQueryParameterError> getErrors();

	boolean isDefined();

	String getFilterQuery();

	int getSqlParameterCount();

	void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement,
			BiFunctionWithSqlException<String, Object[], Array> arrayCreator) throws SQLException;

	/**
	 * Will not be called if {@link #isDefined()} returns <code>false</code>
	 *
	 * @param bundleUri
	 *            never <code>null</code>
	 */
	void modifyBundleUri(UriBuilder bundleUri);

	Optional<SearchQuerySortParameter> getSortParameter();

	List<SearchQueryIncludeParameter> getIncludeParameters();

	String getParameterName();

	Stream<String> getBaseAndModifiedParameterNames();
}
