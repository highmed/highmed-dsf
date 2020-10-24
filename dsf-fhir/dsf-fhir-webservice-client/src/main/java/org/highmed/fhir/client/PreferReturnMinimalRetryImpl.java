package org.highmed.fhir.client;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;

class PreferReturnMinimalRetryImpl extends AbstractFhirWebserviceClientJerseyWithRetry implements PreferReturnMinimal
{
	PreferReturnMinimalRetryImpl(FhirWebserviceClientJersey delegate, int nTimes, long delayMillis)
	{
		super(delegate, nTimes, delayMillis);
	}

	@Override
	public IdType create(Resource resource)
	{
		return retry(nTimes, delayMillis, () -> delegate.create(PreferReturnType.MINIMAL, resource).getId());
	}

	@Override
	public IdType createConditionaly(Resource resource, String ifNoneExistCriteria)
	{
		return retry(nTimes, delayMillis,
				() -> delegate.createConditionaly(PreferReturnType.MINIMAL, resource, ifNoneExistCriteria).getId());
	}

	@Override
	public IdType createBinary(InputStream in, MediaType mediaType, String securityContextReference)
	{
		return retry(nTimes, delayMillis,
				() -> delegate.createBinary(PreferReturnType.MINIMAL, in, mediaType, securityContextReference).getId());
	}

	@Override
	public IdType update(Resource resource)
	{
		return retry(nTimes, delayMillis, () -> delegate.update(PreferReturnType.MINIMAL, resource).getId());
	}

	@Override
	public IdType updateConditionaly(Resource resource, Map<String, List<String>> criteria)
	{
		return retry(nTimes, delayMillis,
				() -> delegate.updateConditionaly(PreferReturnType.MINIMAL, resource, criteria).getId());
	}

	@Override
	public IdType updateBinary(String id, InputStream in, MediaType mediaType, String securityContextReference)
	{
		return retry(nTimes, delayMillis, () -> delegate
				.updateBinary(PreferReturnType.MINIMAL, id, in, mediaType, securityContextReference).getId());
	}

	@Override
	public Bundle postBundle(Bundle bundle)
	{
		return retry(nTimes, delayMillis, () -> delegate.postBundle(PreferReturnType.MINIMAL, bundle));
	}
}