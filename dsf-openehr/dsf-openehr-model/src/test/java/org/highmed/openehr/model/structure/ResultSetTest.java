package org.highmed.openehr.model.structure;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.highmed.openehr.deserializer.RowElementDeserializer;
import org.highmed.openehr.model.datatypes.DvCount;
import org.highmed.openehr.model.datatypes.DvOther;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.Meta;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ResultSetTest
{
	private static final Logger logger = LoggerFactory.getLogger(ResultSetTest.class);

	@Test
	public void testReadWrite() throws Exception
	{
		Meta meta = new Meta("href", "type", "schemaVersion", "created", "generator", "executedAql");
		List<Column> columns = Arrays.asList(new Column("name1", "path1"), new Column("name2", "path2"));
		List<List<RowElement<?>>> rows = Arrays.asList(Arrays.asList(new DvCount(123)),
				Arrays.asList(new DvOther("456")));
		ResultSet resultSet1 = new ResultSet(meta, "name", "query", columns, rows);

		String json;
		try (StringWriter w = new StringWriter())
		{
			objectMapper().writeValue(w, resultSet1);
			json = w.toString();
			logger.debug("ResultSet json: {}", json);
		}

		try (StringReader r = new StringReader(json))
		{
			ResultSet resultSet2 = objectMapper().readValue(r, ResultSet.class);
			assertNotNull(resultSet2);

			assertNotNull(resultSet2.getColumns());
			assertEquals(resultSet1.getColumns().size(), resultSet2.getColumns().size());
			assertNotNull(resultSet2.getColumns().get(0));
			assertEquals(resultSet1.getColumns().get(0).getName(), resultSet2.getColumns().get(0).getName());
			assertEquals(resultSet1.getColumns().get(0).getPath(), resultSet2.getColumns().get(0).getPath());
			assertNotNull(resultSet2.getColumns().get(0));
			assertEquals(resultSet1.getColumns().get(1).getName(), resultSet2.getColumns().get(1).getName());
			assertEquals(resultSet1.getColumns().get(1).getPath(), resultSet2.getColumns().get(1).getPath());

			assertNotNull(resultSet2.getMeta());
			assertEquals(resultSet1.getMeta().getCreated(), resultSet2.getMeta().getCreated());
			assertEquals(resultSet1.getMeta().getExecutedAql(), resultSet2.getMeta().getExecutedAql());
			assertEquals(resultSet1.getMeta().getGenerator(), resultSet2.getMeta().getGenerator());
			assertEquals(resultSet1.getMeta().getHref(), resultSet2.getMeta().getHref());
			assertEquals(resultSet1.getMeta().getSchemaVersion(), resultSet2.getMeta().getSchemaVersion());
			assertEquals(resultSet1.getMeta().getType(), resultSet2.getMeta().getType());

			assertEquals(resultSet1.getQuery(), resultSet2.getQuery());
			assertEquals(resultSet1.getName(), resultSet2.getName());

			assertNotNull(resultSet2.getRows());
			assertEquals(resultSet1.getRows().size(), resultSet2.getRows().size());
			assertNotNull(resultSet2.getRows().get(0));
			assertEquals(resultSet1.getRows().get(0).size(), resultSet2.getRows().get(0).size());
			assertNotNull(resultSet2.getRows().get(0).get(0));
			// not testing row values, since strings are not serialized/de-serialized in the same way

			assertNotNull(resultSet2.getRows().get(1));
			assertEquals(resultSet1.getRows().get(1).size(), resultSet2.getRows().get(1).size());
			assertNotNull(resultSet2.getRows().get(1).get(0));
			// not testing row values, since strings are not serialized/de-serialized in the same way
		}
	}

	private static ObjectMapper objectMapper()
	{
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addDeserializer(RowElement.class, new RowElementDeserializer());
		objectMapper.registerModule(module);

		return objectMapper;
	}
}
