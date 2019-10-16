package org.highmed.dsf.fhir.dao.converter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

import org.highmed.dsf.fhir.service.SnapshotInfo;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SnapshotInfoConverter implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public SnapshotInfoConverter(ObjectMapper objectMapper)
	{
		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	public SnapshotInfo fromDb(String json)
	{
		if (json == null)
			return null;

		try
		{
			return objectMapper.reader().forType(SnapshotInfo.class).readValue(json);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public PGobject toDb(SnapshotInfo info)
	{
		if (info == null)
			return null;

		try
		{
			PGobject o = new PGobject();
			o.setType("JSONB");

			o.setValue(objectMapper.writer().forType(SnapshotInfo.class).writeValueAsString(info));

			return o;
		}
		catch (JsonProcessingException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
