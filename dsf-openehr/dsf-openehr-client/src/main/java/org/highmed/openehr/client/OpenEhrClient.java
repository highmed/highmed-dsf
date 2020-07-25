package org.highmed.openehr.client;

import javax.ws.rs.core.MultivaluedMap;

import org.highmed.openehr.model.structure.ResultSet;

public interface OpenEhrClient
{
	ResultSet query(String query, MultivaluedMap<String, Object> headers);
}
