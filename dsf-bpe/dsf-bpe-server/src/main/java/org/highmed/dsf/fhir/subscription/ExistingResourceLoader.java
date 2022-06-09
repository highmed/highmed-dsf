package org.highmed.dsf.fhir.subscription;

import java.util.List;
import java.util.Map;

public interface ExistingResourceLoader
{
	void readExistingResources(Map<String, List<String>> searchCriteriaQueryParameters);
}
