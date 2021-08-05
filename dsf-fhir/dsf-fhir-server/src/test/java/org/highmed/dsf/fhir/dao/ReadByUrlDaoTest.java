package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.hl7.fhir.r4.model.DomainResource;
import org.junit.Test;

public interface ReadByUrlDaoTest<D extends DomainResource>
{
	D createResourceWithUrlAndVersion();

	String getUrl();

	String getVersion();

	ReadByUrlDao<D> readByUrlDao();

	ResourceDao<D> getDao();

	@Test
	default void testReadByUrlAndVersionWithUrl1() throws Exception
	{
		D newResource = createResourceWithUrlAndVersion();
		getDao().create(newResource);

		Optional<D> readByUrlAndVersion = readByUrlDao().readByUrlAndVersion(getUrl());
		assertTrue(readByUrlAndVersion.isPresent());
	}

	@Test
	default void testReadByUrlAndVersionWithUrlAndVersion1() throws Exception
	{
		D newResource = createResourceWithUrlAndVersion();
		getDao().create(newResource);

		Optional<D> readByUrlAndVersion = readByUrlDao().readByUrlAndVersion(getUrl() + "|" + getVersion());
		assertTrue(readByUrlAndVersion.isPresent());
	}

	@Test
	default void testReadByUrlAndVersionWithUrl2() throws Exception
	{
		D newResource = createResourceWithUrlAndVersion();
		getDao().create(newResource);

		Optional<D> readByUrlAndVersion = readByUrlDao().readByUrlAndVersion(getUrl(), null);
		assertTrue(readByUrlAndVersion.isPresent());
	}

	@Test
	default void testReadByUrlAndVersionWithUrlAndVersion2() throws Exception
	{
		D newResource = createResourceWithUrlAndVersion();
		getDao().create(newResource);

		Optional<D> readByUrlAndVersion = readByUrlDao().readByUrlAndVersion(getUrl(), getVersion());
		assertTrue(readByUrlAndVersion.isPresent());
	}
}
