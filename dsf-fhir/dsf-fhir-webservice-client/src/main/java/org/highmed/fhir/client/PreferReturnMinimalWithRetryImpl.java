package org.highmed.fhir.client;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;

class PreferReturnMinimalWithRetryImpl implements PreferReturnMinimalWithRetry
{
	private final FhirWebserviceClientJersey delegate;

	PreferReturnMinimalWithRetryImpl(FhirWebserviceClientJersey delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public IdType create(Resource resource)
	{
		return delegate.create(PreferReturnType.MINIMAL, resource).getId();
	}

	@Override
	public IdType createConditionaly(Resource resource, String ifNoneExistCriteria)
	{
		return delegate.createConditionaly(PreferReturnType.MINIMAL, resource, ifNoneExistCriteria).getId();
	}

	@Override
	public IdType createBinary(InputStream in, MediaType mediaType, String securityContextReference)
	{
		return delegate.createBinary(PreferReturnType.MINIMAL, in, mediaType, securityContextReference).getId();
	}

	@Override
	public IdType update(Resource resource)
	{
		return delegate.update(PreferReturnType.MINIMAL, resource).getId();
	}

	@Override
	public IdType updateConditionaly(Resource resource, Map<String, List<String>> criteria)
	{
		return delegate.updateConditionaly(PreferReturnType.MINIMAL, resource, criteria).getId();
	}

	@Override
	public IdType updateBinary(String id, InputStream in, MediaType mediaType, String securityContextReference)
	{
		return delegate.updateBinary(PreferReturnType.MINIMAL, id, in, mediaType, securityContextReference).getId();
	}

	@Override
	public Bundle postBundle(Bundle bundle)
	{
		return delegate.postBundle(PreferReturnType.MINIMAL, bundle);
	}

	@Override
	public PreferReturnMinimal withRetry(int nTimes, long delayMillis)
	{
		if (nTimes < 0)
			throw new IllegalArgumentException("nTimes < 0");
		if (delayMillis < 0)
			throw new IllegalArgumentException("delayMillis < 0");

		return new PreferReturnMinimalRetryImpl(delegate, nTimes, delayMillis);
	}

	@Override
	public PreferReturnMinimal withRetryForever(long delayMillis)
	{
		if (delayMillis < 0)
			throw new IllegalArgumentException("delayMillis < 0");

		return new PreferReturnMinimalRetryImpl(delegate, RETRY_FOREVER, delayMillis);
	}
}