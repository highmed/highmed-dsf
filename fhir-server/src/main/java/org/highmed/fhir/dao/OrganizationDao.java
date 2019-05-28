package org.highmed.fhir.dao;

import java.sql.SQLException;
import java.util.Optional;

import org.hl7.fhir.r4.model.Organization;

public interface OrganizationDao extends ResourceDao<Organization>
{
	Optional<Organization> readByThumbprint(String thumbprintHex) throws SQLException;
}
