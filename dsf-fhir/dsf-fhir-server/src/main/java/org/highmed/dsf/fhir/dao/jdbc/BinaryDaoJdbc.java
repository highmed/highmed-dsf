package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.search.parameters.BinaryContentType;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.IdType;

import ca.uhn.fhir.context.FhirContext;

public class BinaryDaoJdbc extends AbstractResourceDaoJdbc<Binary> implements BinaryDao
{
	public BinaryDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Binary.class, "binaries", "binary_json", "binary_id",
				new PreparedStatementFactoryBinary(fhirContext), BinaryContentType::new);
	}

	@Override
	protected Binary copy(Binary resource)
	{
		return resource.copy();
	}

	@Override
	protected Binary getResource(ResultSet result, int index) throws SQLException
	{
		// TODO Bugfix HAPI is removing version information from bundle.id
		Binary binary = super.getResource(result, index);
		IdType fixedId = new IdType(binary.getResourceType().name(), binary.getIdElement().getIdPart(),
				binary.getMeta().getVersionId());
		binary.setIdElement(fixedId);
		return binary;
	}

	@Override
	protected void modifySearchResultResource(Binary resource, Connection connection) throws SQLException
	{
		try (PreparedStatement statement = connection
				.prepareStatement("SELECT binary_data FROM binaries WHERE binary_id = ? AND version = ?"))
		{
			statement.setObject(1, uuidToPgObject(toUuid(resource.getIdElement().getIdPart())));
			statement.setLong(2, resource.getMeta().getVersionIdElement().getIdPartAsLong());

			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
				{
					byte[] data = result.getBytes(1);
					resource.setData(data);
				}
				else
					throw new SQLException(
							"Binary resource with id " + resource.getIdElement().getIdPart() + " not found");
			}
		}
	}
}
