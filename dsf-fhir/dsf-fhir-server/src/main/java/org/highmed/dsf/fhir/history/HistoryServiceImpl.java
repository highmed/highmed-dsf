package org.highmed.dsf.fhir.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.HistoryDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.history.user.HistoryUserFilterFactory;
import org.highmed.dsf.fhir.search.PageAndCount;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class HistoryServiceImpl implements HistoryService, InitializingBean
{
	private final int defaultPageCount;
	private final ParameterConverter parameterConverter;
	private final ExceptionHandler exceptionHandler;
	private final ResponseGenerator responseGenerator;
	private final HistoryDao historyDao;
	private final HistoryUserFilterFactory historyUserFilterFactory;

	public HistoryServiceImpl(int defaultPageCount, ParameterConverter parameterConverter,
			ExceptionHandler exceptionHandler, ResponseGenerator responseGenerator, HistoryDao historyDao,
			HistoryUserFilterFactory historyUserFilterFactory)
	{
		this.defaultPageCount = defaultPageCount;
		this.parameterConverter = parameterConverter;
		this.exceptionHandler = exceptionHandler;
		this.responseGenerator = responseGenerator;
		this.historyDao = historyDao;
		this.historyUserFilterFactory = historyUserFilterFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(parameterConverter, "parameterConverter");
		Objects.requireNonNull(exceptionHandler, "exceptionHandler");
		Objects.requireNonNull(responseGenerator, "responseGenerator");
		Objects.requireNonNull(historyDao, "historyDao");
		Objects.requireNonNull(historyUserFilterFactory, "historyUserFilterFactory");
	}

	@Override
	public Bundle getHistory(User user, UriInfo uri, HttpHeaders headers)
	{
		return getHistory(user, uri, headers, null, null);
	}

	@Override
	public Bundle getHistory(User user, UriInfo uri, HttpHeaders headers, Class<? extends Resource> resource)
	{
		return getHistory(user, uri, headers, resource, null);
	}

	@Override
	public Bundle getHistory(User user, UriInfo uri, HttpHeaders headers, Class<? extends Resource> resource, String id)
	{
		MultivaluedMap<String, String> queryParameters = uri.getQueryParameters();

		Integer page = parameterConverter.getFirstInt(queryParameters, SearchQuery.PARAMETER_PAGE);
		int effectivePage = page == null ? 1 : page;

		Integer count = parameterConverter.getFirstInt(queryParameters, SearchQuery.PARAMETER_COUNT);
		int effectiveCount = (count == null || count < 0) ? defaultPageCount : count;

		PageAndCount pageAndCount = new PageAndCount(effectivePage, effectiveCount);

		History history;
		if (resource == null && id == null)
			history = exceptionHandler.handleSqlException(
					() -> historyDao.readHistory(historyUserFilterFactory.getUserFilters(user), pageAndCount));
		else if (resource != null && id != null)
			history = exceptionHandler.handleSqlException(
					() -> historyDao.readHistory(historyUserFilterFactory.getUserFilter(user, resource), pageAndCount,
							resource, parameterConverter.toUuid(getResourceTypeName(resource), id)));
		else if (resource != null)
			history = exceptionHandler.handleSqlException(() -> historyDao
					.readHistory(historyUserFilterFactory.getUserFilter(user, resource), pageAndCount, resource));
		else
			throw new WebApplicationException();

		// TODO parse parameters _since and _at
		List<SearchQueryParameterError> errors = new ArrayList<>(); // TODO
		
		UriBuilder bundleUri = uri.getAbsolutePathBuilder();
		String format = queryParameters.getFirst(SearchQuery.PARAMETER_FORMAT);
		String pretty = queryParameters.getFirst(SearchQuery.PARAMETER_PRETTY);

		return responseGenerator.createHistoryBundle(history, errors, bundleUri, format, pretty);
	}

	private String getResourceTypeName(Class<? extends Resource> resource)
	{
		if (resource == null)
			return null;
		else
			return resource.getAnnotation(ResourceDef.class).name();
	}
}
