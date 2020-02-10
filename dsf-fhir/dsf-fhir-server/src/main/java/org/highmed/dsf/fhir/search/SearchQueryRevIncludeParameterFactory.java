package org.highmed.dsf.fhir.search;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.hl7.fhir.r4.model.Resource;

public interface SearchQueryRevIncludeParameterFactory
{
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface RevIncludeDefinition
	{
		Class<? extends Resource> resourceType();

		String parameterName();

		Class<? extends Resource>[] targetResourceTypes();
	}

	void configure(List<String> revIncludeParameterValues);

	List<SearchQueryParameterError> getErrors();

	List<SearchQueryIncludeParameter> getRevIncludeParameters();
}
