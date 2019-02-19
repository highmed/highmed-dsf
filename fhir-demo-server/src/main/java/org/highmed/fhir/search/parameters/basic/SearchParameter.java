package org.highmed.fhir.search.parameters.basic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

public interface SearchParameter<R extends DomainResource>
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

	static IllegalStateException notDefined()
	{
		return new IllegalStateException("not defined");
	}

	void configure(MultivaluedMap<String, String> queryParameters);

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

	boolean matches(R resource);

	SortParameter getSortParameter();

	String getParameterName();
}
