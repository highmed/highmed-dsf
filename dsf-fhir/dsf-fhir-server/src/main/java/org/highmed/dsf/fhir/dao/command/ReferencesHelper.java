package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;

public interface ReferencesHelper<R extends Resource>
{
	void resolveTemporaryAndConditionalReferencesOrLiteralInternalRelatedArtifactUrls(
			Map<String, IdType> idTranslationTable, Connection connection) throws WebApplicationException;

	void resolveLogicalReferences(Connection connection) throws WebApplicationException;

	void checkReferences(Map<String, IdType> idTranslationTable, Connection connection) throws WebApplicationException;
}