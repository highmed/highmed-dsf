package org.highmed.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.DocumentReferenceDao;
import org.highmed.dsf.fhir.search.parameters.DocumentReferenceIdentifier;
import org.highmed.dsf.fhir.search.parameters.user.DocumentReferenceUserFilter;
import org.hl7.fhir.r4.model.DocumentReference;

import ca.uhn.fhir.context.FhirContext;

public class DocumentReferenceDaoJdbc extends AbstractResourceDaoJdbc<DocumentReference> implements DocumentReferenceDao
{
	public DocumentReferenceDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource,
			FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, DocumentReference.class, "document_references",
				"document_reference", "document_reference_id", DocumentReferenceUserFilter::new,
				with(DocumentReferenceIdentifier::new), with());
	}

	@Override
	protected DocumentReference copy(DocumentReference resource)
	{
		return resource.copy();
	}
}
