package org.highmed.dsf.fhir.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.highmed.dsf.fhir.history.History;
import org.highmed.dsf.fhir.history.user.HistoryUserFilter;
import org.highmed.dsf.fhir.search.PageAndCount;
import org.hl7.fhir.r4.model.Resource;

public interface HistoryDao
{
	History readHistory(List<HistoryUserFilter> filters, PageAndCount pageAndCount) throws SQLException;

	History readHistory(HistoryUserFilter filter, PageAndCount pageAndCount, Class<? extends Resource> resource)
			throws SQLException;

	History readHistory(HistoryUserFilter filter, PageAndCount pageAndCount, Class<? extends Resource> resource,
			UUID id) throws SQLException;
}
