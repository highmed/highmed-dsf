package org.highmed.fhir.client;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;

public interface BasicFhirWebserviceClient extends PreferReturnResource
{
	void delete(Class<? extends Resource> resourceClass, String id);

	void deleteConditionaly(Class<? extends Resource> resourceClass, Map<String, List<String>> criteria);

	void deletePermanently(Class<? extends Resource> resourceClass, String id);

	Resource read(String resourceTypeName, String id);

	<R extends Resource> R read(Class<R> resourceType, String id);

	<R extends Resource> boolean exists(Class<R> resourceType, String id);

	InputStream readBinary(String id, MediaType mediaType);

	/**
	 * @param resourceTypeName
	 *            not <code>null</code>
	 * @param id
	 *            not <code>null</code>
	 * @param version
	 *            not <code>null</code>
	 * @return {@link Resource}
	 */
	Resource read(String resourceTypeName, String id, String version);

	<R extends Resource> R read(Class<R> resourceType, String id, String version);

	<R extends Resource> boolean exists(Class<R> resourceType, String id, String version);

	/**
	 * @param id
	 *            not <code>null</code>
	 * @param version
	 *            not <code>null</code>
	 * @param mediaType
	 *            not <code>null</code>
	 * @return {@link InputStream} needs to be closed
	 */
	InputStream readBinary(String id, String version, MediaType mediaType);

	boolean exists(IdType resourceTypeIdVersion);

	Bundle search(Class<? extends Resource> resourceType, Map<String, List<String>> parameters);

	Bundle searchWithStrictHandling(Class<? extends Resource> resourceType, Map<String, List<String>> parameters);

	CapabilityStatement getConformance();

	StructureDefinition generateSnapshot(String url);

	StructureDefinition generateSnapshot(StructureDefinition differential);

	default Bundle history()
	{
		return history(null);
	}

	default Bundle history(int page, int count)
	{
		return history(null, page, count);
	}

	default Bundle history(Class<? extends Resource> resourceType)
	{
		return history(null, null);
	}

	default Bundle history(Class<? extends Resource> resourceType, int page, int count)
	{
		return history(null, null, page, count);
	}

	default Bundle history(Class<? extends Resource> resourceType, String id)
	{
		return history(resourceType, id, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	Bundle history(Class<? extends Resource> resourceType, String id, int page, int count);
}
