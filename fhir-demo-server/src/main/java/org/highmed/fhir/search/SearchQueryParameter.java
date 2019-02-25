package org.highmed.fhir.search;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

public interface SearchQueryParameter<R extends DomainResource> extends MatcherParameter
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

	boolean isDefined();

	String getFilterQuery();

	int getSqlParameterCount();

	void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException;

	/**
	 * Will not be called if {@link #isDefined()} returns <code>false</code>
	 * 
	 * @param bundleUri
	 *            never <code>null</code>
	 */
	void modifyBundleUri(UriBuilder bundleUri);

	SearchQuerySortParameter getSortParameter();

	String getParameterName();
}
