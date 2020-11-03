package org.highmed.fhir.client;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;

class PreferReturnOutcomeRetryImpl extends AbstractFhirWebserviceClientJerseyWithRetry implements PreferReturnOutcome
{
	PreferReturnOutcomeRetryImpl(FhirWebserviceClientJersey delegate, int nTimes, long delayMillis)
	{
		super(delegate, nTimes, delayMillis);
	}

	@Override
	public OperationOutcome create(Resource resource)
	{
		return retry(nTimes, delayMillis,
				() -> delegate.create(PreferReturnType.OPERATION_OUTCOME, resource).getOperationOutcome());
	}

	@Override
	public OperationOutcome createConditionaly(Resource resource, String ifNoneExistCriteria)
	{
		return retry(nTimes, delayMillis,
				() -> delegate.createConditionaly(PreferReturnType.OPERATION_OUTCOME, resource, ifNoneExistCriteria)
						.getOperationOutcome());
	}

	@Override
	public OperationOutcome createBinary(InputStream in, MediaType mediaType, String securityContextReference)
	{
		return retry(nTimes, delayMillis,
				() -> delegate.createBinary(PreferReturnType.OPERATION_OUTCOME, in, mediaType, securityContextReference)
						.getOperationOutcome());
	}

	@Override
	public OperationOutcome update(Resource resource)
	{
		return retry(nTimes, delayMillis,
				() -> delegate.update(PreferReturnType.OPERATION_OUTCOME, resource).getOperationOutcome());
	}

	@Override
	public OperationOutcome updateConditionaly(Resource resource, Map<String, List<String>> criteria)
	{
		return retry(nTimes, delayMillis, () -> delegate
				.updateConditionaly(PreferReturnType.OPERATION_OUTCOME, resource, criteria).getOperationOutcome());
	}

	@Override
	public OperationOutcome updateBinary(String id, InputStream in, MediaType mediaType,
			String securityContextReference)
	{
		return retry(nTimes, delayMillis,
				() -> delegate
						.updateBinary(PreferReturnType.OPERATION_OUTCOME, id, in, mediaType, securityContextReference)
						.getOperationOutcome());
	}

	@Override
	public Bundle postBundle(Bundle bundle)
	{
		return retry(nTimes, delayMillis, () -> delegate.postBundle(PreferReturnType.OPERATION_OUTCOME, bundle));
	}
}