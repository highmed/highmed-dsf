package org.highmed.fhir.client;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;

class PreferReturnOutcomeWithRetryImpl implements PreferReturnOutcomeWithRetry
{
	private final FhirWebserviceClientJersey delegate;

	PreferReturnOutcomeWithRetryImpl(FhirWebserviceClientJersey delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public OperationOutcome create(Resource resource)
	{
		return delegate.create(PreferReturnType.OPERATION_OUTCOME, resource).getOperationOutcome();
	}

	@Override
	public OperationOutcome createConditionaly(Resource resource, String ifNoneExistCriteria)
	{
		return delegate.createConditionaly(PreferReturnType.OPERATION_OUTCOME, resource, ifNoneExistCriteria)
				.getOperationOutcome();
	}

	@Override
	public OperationOutcome createBinary(InputStream in, MediaType mediaType, String securityContextReference)
	{
		return delegate.createBinary(PreferReturnType.OPERATION_OUTCOME, in, mediaType, securityContextReference)
				.getOperationOutcome();
	}

	@Override
	public OperationOutcome update(Resource resource)
	{
		return delegate.update(PreferReturnType.OPERATION_OUTCOME, resource).getOperationOutcome();
	}

	@Override
	public OperationOutcome updateConditionaly(Resource resource, Map<String, List<String>> criteria)
	{
		return delegate.updateConditionaly(PreferReturnType.OPERATION_OUTCOME, resource, criteria)
				.getOperationOutcome();
	}

	@Override
	public OperationOutcome updateBinary(String id, InputStream in, MediaType mediaType,
			String securityContextReference)
	{
		return delegate.updateBinary(PreferReturnType.OPERATION_OUTCOME, id, in, mediaType, securityContextReference)
				.getOperationOutcome();
	}

	@Override
	public Bundle postBundle(Bundle bundle)
	{
		return delegate.postBundle(PreferReturnType.OPERATION_OUTCOME, bundle);
	}

	@Override
	public PreferReturnOutcome withRetry(int nTimes, long delayMillis)
	{
		if (nTimes < 0)
			throw new IllegalArgumentException("nTimes < 0");
		if (delayMillis < 0)
			throw new IllegalArgumentException("delayMillis < 0");

		return new PreferReturnOutcomeRetryImpl(delegate, nTimes, delayMillis);
	}

	@Override
	public PreferReturnOutcome withRetryForever(long delayMillis)
	{
		if (delayMillis < 0)
			throw new IllegalArgumentException("delayMillis < 0");

		return new PreferReturnOutcomeRetryImpl(delegate, RETRY_FOREVER, delayMillis);
	}
}