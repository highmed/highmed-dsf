package org.highmed.openehr.model.structure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.highmed.openehr.json.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.datatypes.IntegerRowElement;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSetTest
{
	private static final Logger logger = LoggerFactory.getLogger(ResultSetTest.class);

	private static ObjectMapper objectMapper;

	@BeforeClass
	public static void objectMapper()
	{
		ResultSetTest.objectMapper = OpenEhrObjectMapperFactory.createObjectMapper();
	}

	@Test
	public void testReadWriteSimple() throws Exception
	{
		Meta meta = new Meta("href", "type", "schemaVersion", "created", "generator", "executedAql");
		List<Column> columns = Arrays.asList(new Column("name1", "path1"), new Column("name2", "path2"));
		List<List<RowElement>> rows = Arrays.asList(Arrays.asList(new IntegerRowElement(123)),
				Arrays.asList(new StringRowElement("456")));
		ResultSet resultSet1 = new ResultSet(meta, "name", "query", columns, rows);

		printResultSetRowElements(resultSet1);

		String json;
		try (StringWriter w = new StringWriter())
		{
			objectMapper.writeValue(w, resultSet1);
			json = w.toString();
			logger.debug("ResultSet json: {}", json);
		}

		try (StringReader r = new StringReader(json))
		{
			ResultSet resultSet2 = objectMapper.readValue(r, ResultSet.class);

			printResultSetRowElements(resultSet2);

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

	@Test
	public void testReadWriteJson1() throws Exception
	{
		testReadWriteFromFile(Paths.get("src/test/resources/result_1.json"),
				Paths.get("src/test/resources/result_1_min.json"));
	}

	@Test
	public void testReadWriteJson2() throws Exception
	{
		testReadWriteFromFile(Paths.get("src/test/resources/result_2.json"),
				Paths.get("src/test/resources/result_2_min.json"));
	}

	@Test
	public void testReadWriteJson3() throws Exception
	{
		testReadWriteFromFile(Paths.get("src/test/resources/result_3.json"),
				Paths.get("src/test/resources/result_3_min.json"));
	}

	@Test
	public void testReadWriteJson4() throws Exception
	{
		testReadWriteFromFile(Paths.get("src/test/resources/result_4.json"),
				Paths.get("src/test/resources/result_4_min.json"));
	}

	@Test
	public void testReadWriteJson5() throws Exception
	{
		testReadWriteFromFile(Paths.get("src/test/resources/result_5.json"),
				Paths.get("src/test/resources/result_5_min.json"));
	}

	private void testReadWriteFromFile(Path jsonWithWiteSpace, Path jsonMin)
			throws IOException, JsonParseException, JsonMappingException, JsonGenerationException
	{
		ResultSet resultSet;
		try (InputStream in = Files.newInputStream(jsonWithWiteSpace))
		{
			resultSet = objectMapper.readValue(in, ResultSet.class);
			assertNotNull(resultSet);

			printResultSetRowElements(resultSet);
		}

		String json;
		try (StringWriter w = new StringWriter())
		{
			objectMapper.writeValue(w, resultSet);
			json = w.toString();
			logger.debug("ResultSet json: {}", json);
		}

		try (StringReader r = new StringReader(json))
		{
			ResultSet resultSet2 = objectMapper.readValue(r, ResultSet.class);

			printResultSetRowElements(resultSet2);
		}

		String readMinJson = Files.readString(jsonMin);
		assertEquals(readMinJson, json);
	}

	private void printResultSetRowElements(ResultSet resultSet)
	{
		for (int p = 0; p < resultSet.getRows().size(); p++)
		{
			List<RowElement> columns = resultSet.getRow(p);
			if (columns != null)
			{
				for (int c = 0; c < columns.size(); c++)
				{
					RowElement rowElement = columns.get(c);
					logger.debug(
							p + "," + c + " (" + (rowElement == null ? "null" : rowElement.getClass().getSimpleName())
									+ "): " + (rowElement == null ? "null" : rowElement.getValueAsString()));
				}
			}
			else
				logger.debug(p + ",-: null");
		}
	}
}
