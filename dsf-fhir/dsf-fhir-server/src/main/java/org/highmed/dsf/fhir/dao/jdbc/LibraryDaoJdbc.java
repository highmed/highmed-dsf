package org.highmed.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.LibraryDao;
import org.highmed.dsf.fhir.search.parameters.user.LibraryUserFilter;
import org.hl7.fhir.r4.model.Library;

import ca.uhn.fhir.context.FhirContext;

public class LibraryDaoJdbc extends AbstractResourceDaoJdbc<Library> implements LibraryDao
{
	public LibraryDaoJdbc(DataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Library.class, "libraries", "library", "library_id", LibraryUserFilter::new,
				with(), with());
	}

	@Override
	protected Library copy(Library resource)
	{
		return resource.copy();
	}
}
