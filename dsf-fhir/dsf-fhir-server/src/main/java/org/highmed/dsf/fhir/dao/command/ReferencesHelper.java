package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.util.Map;
import java.util.function.Predicate;

import javax.ws.rs.WebApplicationException;

import org.highmed.dsf.fhir.service.ResourceReference;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;

public interface ReferencesHelper<R extends Resource>
{
	void resolveTemporaryAndConditionalReferencesOrLiteralInternalRelatedArtifactOrAttachmentUrls(
			Map<String, IdType> idTranslationTable, Connection connection) throws WebApplicationException;

	void resolveLogicalReferences(Connection connection) throws WebApplicationException;

	void checkReferences(Map<String, IdType> idTranslationTable, Connection connection,
			Predicate<ResourceReference> checkReference) throws WebApplicationException;
}