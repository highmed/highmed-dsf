package org.highmed.dsf.fhir.subscription;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Resource;

public interface ExistingResourceLoader<R extends Resource>
{
	void readExistingResources(Map<String, List<String>> searchCriteriaQueryParameters);
}
