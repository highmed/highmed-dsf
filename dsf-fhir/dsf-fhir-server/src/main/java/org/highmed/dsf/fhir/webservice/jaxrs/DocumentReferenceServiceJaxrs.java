package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.DocumentReferenceService;
import org.hl7.fhir.r4.model.DocumentReference;

@Path(DocumentReferenceServiceJaxrs.PATH)
public class DocumentReferenceServiceJaxrs extends
		AbstractResourceServiceJaxrs<DocumentReference, DocumentReferenceService> implements DocumentReferenceService
{
	public static final String PATH = "DocumentReference";

	public DocumentReferenceServiceJaxrs(DocumentReferenceService delegate)
	{
		super(delegate);
	}
}
