package org.highmed.fhir.search;

import java.sql.SQLException;

import org.highmed.fhir.dao.DaoProvider;
import org.hl7.fhir.r4.model.DomainResource;

public interface MatcherParameter
{
	default void resolveReferencesForMatching(DomainResource resource, DaoProvider daoProvider) throws SQLException
	{
	}

	boolean matches(DomainResource resource);
}
