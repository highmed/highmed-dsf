package org.highmed.dsf.fhir.dao;

import java.sql.SQLException;

import org.hl7.fhir.r4.model.Endpoint;

public interface EndpointDao extends ResourceDao<Endpoint>
{
	boolean existsActiveNotDeletedByAddress(String address) throws SQLException;
}
