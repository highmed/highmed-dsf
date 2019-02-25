package org.highmed.fhir.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.highmed.fhir.dao.AbstractDomainResourceDao;
import org.highmed.fhir.search.Matcher;
import org.highmed.fhir.search.SearchQuery;
import org.hl7.fhir.r4.model.DomainResource;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class MatcherFactory
{
	private final Map<String, AbstractDomainResourceDao<? extends DomainResource>> daosByResourceName = new HashMap<>();

	public MatcherFactory(Map<String, AbstractDomainResourceDao<? extends DomainResource>> daosByResourceName)
	{
		if (daosByResourceName != null)
			this.daosByResourceName.putAll(daosByResourceName);
	}

	public Optional<Matcher> createQuery(String uri)
	{
		UriComponents componentes = UriComponentsBuilder.fromUriString(uri).build();
		String path = componentes.getPath();

		MultiValueMap<String, String> queryParameters = componentes.getQueryParams();

		if (daosByResourceName.containsKey(path))
		{
			AbstractDomainResourceDao<? extends DomainResource> dao = daosByResourceName.get(path);
			SearchQuery<? extends DomainResource> query = dao.createSearchQuery(1, 1);
			query.configureParameters(queryParameters);
			return Optional.of(query);
		}
		else
			return Optional.empty();
	}
}
