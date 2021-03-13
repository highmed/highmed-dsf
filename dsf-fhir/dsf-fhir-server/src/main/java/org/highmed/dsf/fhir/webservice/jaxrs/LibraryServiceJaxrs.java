package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.LibraryService;
import org.hl7.fhir.r4.model.Library;

@Path(LibraryServiceJaxrs.PATH)
public class LibraryServiceJaxrs extends AbstractResourceServiceJaxrs<Library, LibraryService> implements LibraryService
{
	public static final String PATH = "Library";

	public LibraryServiceJaxrs(LibraryService delegate)
	{
		super(delegate);
	}
}
