package org.highmed.dsf.fhir.dao;

import java.sql.SQLException;
import java.util.Optional;

import org.hl7.fhir.r4.model.Organization;

public interface OrganizationDao extends ResourceDao<Organization>
{
	Optional<Organization> readActiveNotDeletedByThumbprint(String thumbprintHex) throws SQLException;
}
