package org.highmed.fhir.client;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.ws.rs.core.MediaType;

import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;

class BasicFhirWebserviceCientWithRetryImpl extends AbstractFhirWebserviceClientJerseyWithRetry
		implements BasicFhirWebserviceClient
{
	BasicFhirWebserviceCientWithRetryImpl(FhirWebserviceClientJersey delegate, int nTimes, long delayMillis)
	{
		super(delegate, nTimes, delayMillis);
	}

	@Override
	public <R extends Resource> R updateConditionaly(R resource, Map<String, List<String>> criteria)
	{
		return retry(nTimes, delayMillis, () -> delegate.updateConditionaly(resource, criteria));
	}

	@Override
	public Binary updateBinary(String id, InputStream in, MediaType mediaType, String securityContextReference)
	{
		return retry(nTimes, delayMillis, () -> delegate.updateBinary(id, in, mediaType, securityContextReference));
	}

	@Override
	public <R extends Resource> R update(R resource)
	{
		return retry(nTimes, delayMillis, () -> delegate.update(resource));
	}

	@Override
	public Bundle postBundle(Bundle bundle)
	{
		return retry(nTimes, delayMillis, () -> delegate.postBundle(bundle));
	}

	@Override
	public <R extends Resource> R createConditionaly(R resource, String ifNoneExistCriteria)
	{
		return retry(nTimes, delayMillis, () -> delegate.createConditionaly(resource, ifNoneExistCriteria));
	}

	@Override
	public Binary createBinary(InputStream in, MediaType mediaType, String securityContextReference)
	{
		return retry(nTimes, delayMillis, () -> delegate.createBinary(in, mediaType, securityContextReference));
	}

	@Override
	public <R extends Resource> R create(R resource)
	{
		return retry(nTimes, delayMillis, () -> delegate.create(resource));
	}

	@Override
	public Bundle searchWithStrictHandling(Class<? extends Resource> resourceType, Map<String, List<String>> parameters)
	{
		return retry(nTimes, delayMillis, () -> delegate.searchWithStrictHandling(resourceType, parameters));
	}

	@Override
	public Bundle search(Class<? extends Resource> resourceType, Map<String, List<String>> parameters)
	{
		return retry(nTimes, delayMillis, () -> delegate.search(resourceType, parameters));
	}

	@Override
	public InputStream readBinary(String id, String version, MediaType mediaType)
	{
		return retry(nTimes, delayMillis, () -> delegate.readBinary(id, version, mediaType));
	}

	@Override
	public InputStream readBinary(String id, MediaType mediaType)
	{
		return retry(nTimes, delayMillis, () -> delegate.readBinary(id, mediaType));
	}

	@Override
	public <R extends Resource> R read(Class<R> resourceType, String id, String version)
	{
		return retry(nTimes, delayMillis, () -> delegate.read(resourceType, id, version));
	}

	@Override
	public Resource read(String resourceTypeName, String id, String version)
	{
		return retry(nTimes, delayMillis, () -> delegate.read(resourceTypeName, id, version));
	}

	@Override
	public <R extends Resource> R read(Class<R> resourceType, String id)
	{
		return retry(nTimes, delayMillis, () -> delegate.read(resourceType, id));
	}

	@Override
	public <R extends Resource> R read(R oldValue)
	{
		return retry(nTimes, delayMillis, () -> delegate.read(oldValue));
	}

	@Override
	public Resource read(String resourceTypeName, String id)
	{
		return retry(nTimes, delayMillis, () -> delegate.read(resourceTypeName, id));
	}

	@Override
	public CapabilityStatement getConformance()
	{
		return retry(nTimes, delayMillis, () -> delegate.getConformance());
	}

	@Override
	public StructureDefinition generateSnapshot(StructureDefinition differential)
	{
		return retry(nTimes, delayMillis, () -> delegate.generateSnapshot(differential));
	}

	@Override
	public StructureDefinition generateSnapshot(String url)
	{
		return retry(nTimes, delayMillis, () -> delegate.generateSnapshot(url));
	}

	@Override
	public boolean exists(IdType resourceTypeIdVersion)
	{
		return retry(nTimes, delayMillis, () -> delegate.exists(resourceTypeIdVersion));
	}

	@Override
	public <R extends Resource> boolean exists(Class<R> resourceType, String id, String version)
	{
		return retry(nTimes, delayMillis, () -> delegate.exists(resourceType, id, version));
	}

	@Override
	public <R extends Resource> boolean exists(Class<R> resourceType, String id)
	{
		return retry(nTimes, delayMillis, () -> delegate.exists(resourceType, id));
	}

	@Override
	public void deletePermanently(Class<? extends Resource> resourceClass, String id)
	{
		retry(nTimes, delayMillis, (Supplier<Void>) () ->
		{
			delegate.deletePermanently(resourceClass, id);
			return null;
		});
	}

	@Override
	public void deleteConditionaly(Class<? extends Resource> resourceClass, Map<String, List<String>> criteria)
	{
		retry(nTimes, delayMillis, (Supplier<Void>) () ->
		{
			delegate.deleteConditionaly(resourceClass, criteria);
			return null;
		});
	}

	@Override
	public void delete(Class<? extends Resource> resourceClass, String id)
	{
		retry(nTimes, delayMillis, (Supplier<Void>) () ->
		{
			delegate.delete(resourceClass, id);
			return null;
		});
	}

	@Override
	public Bundle history(Class<? extends Resource> resourceType, String id, int page, int count)
	{
		return retry(nTimes, delayMillis, () -> delegate.history(resourceType, id, page, count));
	}
}