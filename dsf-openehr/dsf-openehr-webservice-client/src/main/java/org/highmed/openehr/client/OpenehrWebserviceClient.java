package org.highmed.openehr.client;

import javax.ws.rs.core.MultivaluedMap;

import org.highmed.openehr.model.structure.ResultSet;

public interface OpenehrWebserviceClient
{
	ResultSet query(String query, MultivaluedMap<String, Object> headers);
}
