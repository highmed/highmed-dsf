package org.highmed.dsf.fhir.history.user;

import java.util.List;

import org.highmed.dsf.fhir.authentication.User;
import org.hl7.fhir.r4.model.Resource;

public interface HistoryUserFilterFactory
{
	HistoryUserFilter getUserFilter(User user, Class<? extends Resource> resourceType);

	List<HistoryUserFilter> getUserFilters(User user);
}
