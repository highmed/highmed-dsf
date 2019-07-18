package org.highmed.dsf.fhir.dao.jdbc;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.search.parameters.BinaryContentType;
import org.hl7.fhir.r4.model.Binary;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BinaryDaoJdbc extends AbstractAdditionalColumnsResourceDaoJdbc<Binary> implements BinaryDao
{
	public BinaryDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Binary.class, "binaries", "binary_json", "binary_id", Arrays.asList("binary_data"), BinaryContentType::new);
	}

	@Override
	protected Binary copy(Binary resource)
	{
		return resource.copy();
	}

	@Override
	protected PreparedStatement initializeCreateStatement(PreparedStatement statement, Binary resource, UUID uuid) throws SQLException {
		{
			statement.setObject(1, uuidToPgObject(uuid));

			Binary resourceWithoutContent = copy(resource);
			resourceWithoutContent.setContent(new byte[0]);
			statement.setObject(2, resourceToPgObject(resourceWithoutContent));

			statement.setBinaryStream(3, new ByteArrayInputStream(resource.getContent()));

			return statement;
		}
	}

	@Override
	protected Optional<Binary> assembleResourceFromReadResult(ResultSet result) throws SQLException {
		Optional<Binary> optional = Optional.of(getResource(result, 1));
		optional.get().setContent(result.getBytes(3));
		return optional;
	}

	@Override
	protected Optional<Binary> assembleResourceFromReadVersionResult(ResultSet result) throws SQLException {
		Optional<Binary> optional = Optional.of(getResource(result, 1));
		optional.get().setContent(result.getBytes(2));
		return optional;
	}

	@Override
	protected PreparedStatement initializeUpdateStatement(PreparedStatement statement, Binary resource, UUID uuid, long version) throws SQLException {
		statement.setObject(1, uuidToPgObject(uuid));
		statement.setLong(2, version);

		Binary resourceWithoutContent = copy(resource);
		resourceWithoutContent.setContent(new byte[0]);
		statement.setObject(3, resourceToPgObject(resourceWithoutContent));
		statement.setBinaryStream(4, new ByteArrayInputStream(resource.getContent()));

		return statement;
	}

	@Override
	protected PreparedStatement initializeUpdateSameRowStatement(PreparedStatement statement, Binary resource, UUID uuid, long version) throws SQLException {
		Binary resourceWithoutContent = copy(resource);
		resourceWithoutContent.setContent(new byte[0]);
		statement.setObject(1, resourceToPgObject(resourceWithoutContent));
		statement.setBinaryStream(2, new ByteArrayInputStream(resource.getContent()));

		statement.setObject(3, uuidToPgObject(uuid));
		statement.setLong(4, version);

		return statement;
	}
}
