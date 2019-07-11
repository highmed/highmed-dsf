package org.highmed.dsf.fhir.dao.jdbc;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.dao.BundleDao;
import org.highmed.dsf.fhir.search.parameters.BinaryContentType;
import org.highmed.dsf.fhir.search.parameters.BundleIdentifier;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BinaryDaoJdbc extends AbstractDomainResourceDaoJdbc<Binary> implements BinaryDao
{
	public BinaryDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Binary.class, "binaries", "binary_data", "binary_id", BinaryContentType::new);
	}

	@Override
	protected Binary copy(Binary resource)
	{
		return resource.copy();
	}
}
