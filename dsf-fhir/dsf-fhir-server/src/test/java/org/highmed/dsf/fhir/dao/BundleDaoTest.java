package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.BundleDaoJdbc;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;

import ca.uhn.fhir.context.FhirContext;

public class BundleDaoTest extends AbstractResourceDaoTest<Bundle, BundleDao>
{
	private static final BundleType type = BundleType.SEARCHSET;
	private static final String language = "Demo Bundle language";

	public BundleDaoTest()
	{
		super(Bundle.class);
	}

	@Override
	protected BundleDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new BundleDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected Bundle createResource()
	{
		Bundle bundle = new Bundle();
		bundle.setType(type);
		return bundle;
	}

	@Override
	protected void checkCreated(Bundle resource)
	{
		assertEquals(type, resource.getType());
	}

	@Override
	protected Bundle updateResource(Bundle resource)
	{
		resource.setLanguage(language);
		return resource;
	}

	@Override
	protected void checkUpdates(Bundle resource)
	{
		assertEquals(language, resource.getLanguage());
	}
}
