package org.highmed.dsf.fhir.search;

import java.sql.SQLException;

import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.hl7.fhir.r4.model.Resource;

public interface Matcher
{
	void resloveReferencesForMatching(Resource resource, DaoProvider daoProvider) throws SQLException;

	boolean matches(Resource resource);

	Class<? extends Resource> getResourceType();
}
