package org.highmed.fhir.search;

import java.sql.SQLException;

import org.highmed.fhir.dao.provider.DaoProvider;
import org.hl7.fhir.r4.model.DomainResource;

public interface Matcher
{
	void resloveReferencesForMatching(DomainResource resource, DaoProvider daoProvider) throws SQLException;

	boolean matches(DomainResource resource);

	Class<? extends DomainResource> getResourceType();
}
