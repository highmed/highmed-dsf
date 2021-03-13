package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.highmed.dsf.fhir.dao.jdbc.LibraryDaoJdbc;
import org.hl7.fhir.r4.model.Library;

public class LibraryDaoTest extends AbstractResourceDaoTest<Library, LibraryDao> implements ReadByUrlDaoTest<Library>
{
	private static final String name = "Demo Library";
	private static final String description = "Demo Library Description";

	public LibraryDaoTest()
	{
		super(Library.class, LibraryDaoJdbc::new);
	}

	@Override
	protected Library createResource()
	{
		Library library = new Library();
		library.setName(name);
		return library;
	}

	@Override
	protected void checkCreated(Library resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected Library updateResource(Library resource)
	{
		resource.setDescription(description);
		return resource;
	}

	@Override
	protected void checkUpdates(Library resource)
	{
		assertEquals(description, resource.getDescription());
	}

	@Override
	public Library createResourceWithUrlAndVersion()
	{
		Library resource = createResource();
		resource.setUrl(getUrl());
		resource.setVersion(getVersion());
		return resource;
	}

	@Override
	public String getUrl()
	{
		return "http://test.com/fhir/Library/test-library";
	}

	@Override
	public String getVersion()
	{
		return "0.5.0";
	}

	@Override
	public ReadByUrlDao<Library> readByUrlDao()
	{
		return getDao();
	}

	@Override
	public ResourceDao<Library> dao()
	{
		return getDao();
	}
}
