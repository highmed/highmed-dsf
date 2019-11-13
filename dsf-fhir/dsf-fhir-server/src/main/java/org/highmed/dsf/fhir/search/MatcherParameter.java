package org.highmed.dsf.fhir.search;

import java.sql.SQLException;

import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.hl7.fhir.r4.model.Resource;

public interface MatcherParameter
{
	default void resolveReferencesForMatching(Resource resource, DaoProvider daoProvider) throws SQLException
	{
	}

	boolean matches(Resource resource);
}
