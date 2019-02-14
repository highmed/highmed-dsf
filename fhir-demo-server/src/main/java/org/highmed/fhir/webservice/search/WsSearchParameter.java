package org.highmed.fhir.webservice.search;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

public interface WsSearchParameter
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

	void configure(MultivaluedMap<String, String> queryParameters);

	boolean isDefined();

	void modifyBundleUri(UriBuilder bundleUri);
}
