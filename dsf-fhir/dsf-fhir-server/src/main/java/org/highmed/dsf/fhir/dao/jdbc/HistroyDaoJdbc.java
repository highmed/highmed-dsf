package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.HistoryDao;
import org.highmed.dsf.fhir.history.AtParameter;
import org.highmed.dsf.fhir.history.History;
import org.highmed.dsf.fhir.history.HistoryEntry;
import org.highmed.dsf.fhir.history.SinceParameter;
import org.highmed.dsf.fhir.history.user.HistoryUserFilter;
import org.highmed.dsf.fhir.search.PageAndCount;
import org.highmed.dsf.fhir.search.SearchQueryParameter;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

public class HistroyDaoJdbc implements HistoryDao, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(HistroyDaoJdbc.class);

	private final DataSource dataSource;
	private final FhirContext fhirContext;
	private final BinaryDaoJdbc binaryDao;

	public HistroyDaoJdbc(DataSource dataSource, FhirContext fhirContext, BinaryDaoJdbc binaryDao)
	{
		this.dataSource = dataSource;
		this.fhirContext = fhirContext;
		this.binaryDao = binaryDao;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(dataSource, "dataSource");
		Objects.requireNonNull(fhirContext, "fhirContext");
		Objects.requireNonNull(binaryDao, "binaryDao");
	}

	@Override
	public History readHistory(List<HistoryUserFilter> filters, PageAndCount pageAndCount, AtParameter atParameter,
			SinceParameter sinceParameter) throws SQLException
	{
		Objects.requireNonNull(filters, "filters");
		Objects.requireNonNull(pageAndCount, "pageAndCount");
		Objects.requireNonNull(atParameter, "atParameter");
		Objects.requireNonNull(sinceParameter, "sinceParameter");

		return readHistory(filters, pageAndCount, atParameter, sinceParameter, null, null);
	}

	@Override
	public History readHistory(HistoryUserFilter filter, PageAndCount pageAndCount, AtParameter atParameter,
			SinceParameter sinceParameter, Class<? extends Resource> resource) throws SQLException
	{
		Objects.requireNonNull(filter, "filter");
		Objects.requireNonNull(pageAndCount, "pageAndCount");
		Objects.requireNonNull(atParameter, "atParameter");
		Objects.requireNonNull(sinceParameter, "sinceParameter");
		Objects.requireNonNull(resource, "resource");

		return readHistory(Collections.singletonList(filter), pageAndCount, atParameter, sinceParameter, resource,
				null);
	}

	@Override
	public History readHistory(HistoryUserFilter filter, PageAndCount pageAndCount, AtParameter atParameter,
			SinceParameter sinceParameter, Class<? extends Resource> resource, UUID id) throws SQLException
	{
		Objects.requireNonNull(filter, "filter");
		Objects.requireNonNull(pageAndCount, "pageAndCount");
		Objects.requireNonNull(atParameter, "atParameter");
		Objects.requireNonNull(sinceParameter, "sinceParameter");
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(id, "id");

		return readHistory(Collections.singletonList(filter), pageAndCount, atParameter, sinceParameter, resource, id);
	}

	private History readHistory(List<HistoryUserFilter> filter, PageAndCount pageAndCount, AtParameter atParameter,
			SinceParameter sinceParameter, Class<? extends Resource> resource, UUID id) throws SQLException
	{
		try (Connection connection = dataSource.getConnection())
		{
			int total = 0;
			try (PreparedStatement statement = connection.prepareStatement(
					createCountSql(id != null, resource != null, filter, atParameter, sinceParameter)))
			{
				configureStatement(statement, id, resource, filter, atParameter, sinceParameter);

				logger.trace("Executing count query '{}'", statement);
				try (ResultSet result = statement.executeQuery())
				{
					if (result.next())
						total = result.getInt(1);
				}
			}

			List<HistoryEntry> entries = new ArrayList<>();
			if (!pageAndCount.isCountOnly(total))
			{
				try (PreparedStatement statement = connection.prepareStatement(
						createReadSql(id != null, resource != null, filter, atParameter, sinceParameter, pageAndCount)))
				{
					configureStatement(statement, id, resource, filter, atParameter, sinceParameter);

					logger.trace("Executing read query '{}'", statement);
					try (ResultSet result = statement.executeQuery())
					{
						while (result.next())
						{
							UUID entryId = result.getObject(1, UUID.class);
							long version = result.getLong(2);
							String resourceType = result.getString(3);
							String method = result.getString(4);
							Timestamp lastUpdated = result.getTimestamp(5);
							Resource entryResource = jsonToResource(result.getString(6), resource);
							modifyResource(entryResource, connection);

							HistoryEntry entry = new HistoryEntry(entryId, String.valueOf(version), resourceType,
									method, lastUpdated == null ? null : lastUpdated.toLocalDateTime(), entryResource);
							entries.add(entry);
						}

					}
				}
			}

			return new History(total, pageAndCount, entries);
		}
	}

	private void modifyResource(Resource resource, Connection connection) throws SQLException
	{
		if (resource instanceof Binary)
			binaryDao.modifySearchResultResource((Binary) resource, connection);
	}

	private PGobject uuidToPgObject(UUID uuid)
	{
		if (uuid == null)
			return null;

		try
		{
			PGobject o = new PGobject();
			o.setType("UUID");
			o.setValue(uuid.toString());
			return o;
		}
		catch (DataFormatException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public IParser getJsonParser()
	{
		IParser p = fhirContext.newJsonParser();
		p.setStripVersionsFromReferences(false);
		return p;
	}

	private Resource jsonToResource(String json, Class<? extends Resource> resourceType)
	{
		if (json == null)
			return null;

		Resource resource;
		if (resourceType != null)
			resource = getJsonParser().parseResource(resourceType, json);
		else
			resource = (Resource) getJsonParser().parseResource(json);

		// TODO Bugfix HAPI is not setting version information from bundle.id while parsing non DomainResource
		if (!(resource instanceof DomainResource))
		{
			IdType fixedId = new IdType(resource.getResourceType().name(), resource.getIdElement().getIdPart(),
					resource.getMeta().getVersionId());
			resource.setIdElement(fixedId);
		}

		return resource;
	}

	private String createCountSql(boolean forId, boolean forResource, List<HistoryUserFilter> filter,
			AtParameter atParameter, SinceParameter sinceParameter)
	{
		String selectSql = "SELECT count(*) FROM history WHERE ";

		return createSql(forId, forResource, filter, atParameter, sinceParameter, selectSql, "");
	}

	private String createReadSql(boolean forId, boolean forResource, List<HistoryUserFilter> filter,
			AtParameter atParameter, SinceParameter sinceParameter, PageAndCount pageAndCount)
	{
		String selectSql = "SELECT id, version, type, method, last_updated, resource FROM history WHERE ";

		return createSql(forId, forResource, filter, atParameter, sinceParameter, selectSql, pageAndCount.getSql());
	}

	private String createSql(boolean forId, boolean forResource, List<HistoryUserFilter> filter,
			AtParameter atParameter, SinceParameter sinceParameter, String selectSql, String limitOffsetSql)
	{
		String idSql = forId ? "id = ?" : null;
		String typeSql = forResource ? "type = ?" : null;
		String filterSql = filter.stream().filter(HistoryUserFilter::isDefined).map(f -> f.getFilterQuery())
				.collect(Collectors.joining(" OR ", "(", ")"));
		filterSql = "()".equals(filterSql) ? null : filterSql;

		Stream<String> params = Stream.of(atParameter, sinceParameter).filter(SearchQueryParameter::isDefined)
				.map(SearchQueryParameter::getFilterQuery);

		return Stream.concat(Stream.of(idSql, typeSql, filterSql).filter(s -> s != null), params)
				.collect(Collectors.joining(" AND ", selectSql, limitOffsetSql));
	}

	private void configureStatement(PreparedStatement statement, UUID id, Class<? extends Resource> resource,
			List<HistoryUserFilter> filter, AtParameter atParameter, SinceParameter sinceParameter) throws SQLException
	{
		int parameterIndex = 1;
		if (id != null)
			statement.setObject(parameterIndex++, uuidToPgObject(id));
		if (resource != null)
			statement.setString(parameterIndex++, resource.getAnnotation(ResourceDef.class).name());

		for (HistoryUserFilter f : filter)
		{
			if (f.isDefined())
			{
				for (int i = 1; i <= f.getSqlParameterCount(); i++)
					f.modifyStatement(parameterIndex++, i, statement);
			}
		}

		if (atParameter.isDefined())
		{
			for (int i = 1; i <= atParameter.getSqlParameterCount(); i++)
				atParameter.modifyStatement(parameterIndex++, i, statement, null);
		}

		if (sinceParameter.isDefined())
		{
			for (int i = 1; i <= sinceParameter.getSqlParameterCount(); i++)
				sinceParameter.modifyStatement(parameterIndex++, i, statement, null);
		}
	}
}
