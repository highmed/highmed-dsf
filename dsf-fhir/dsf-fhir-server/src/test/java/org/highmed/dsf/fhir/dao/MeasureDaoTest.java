package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.highmed.dsf.fhir.dao.jdbc.MeasureDaoJdbc;
import org.hl7.fhir.r4.model.Measure;

public class MeasureDaoTest extends AbstractResourceDaoTest<Measure, MeasureDao> implements ReadByUrlDaoTest<Measure>
{
	private static final String name = "Demo Measure";
	private static final String description = "Demo Measure Description";

	public MeasureDaoTest()
	{
		super(Measure.class, MeasureDaoJdbc::new);
	}

	@Override
	protected Measure createResource()
	{
		Measure measure = new Measure();
		measure.setName(name);
		return measure;
	}

	@Override
	protected void checkCreated(Measure resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected Measure updateResource(Measure resource)
	{
		resource.setDescription(description);
		return resource;
	}

	@Override
	protected void checkUpdates(Measure resource)
	{
		assertEquals(description, resource.getDescription());
	}

	@Override
	public Measure createResourceWithUrlAndVersion()
	{
		Measure resource = createResource();
		resource.setUrl(getUrl());
		resource.setVersion(getVersion());
		return resource;
	}

	@Override
	public String getUrl()
	{
		return "http://test.com/fhir/Measure/test-measure";
	}

	@Override
	public String getVersion()
	{
		return "0.5.0";
	}

	@Override
	public ReadByUrlDao<Measure> readByUrlDao()
	{
		return getDao();
	}

	@Override
	public ResourceDao<Measure> dao()
	{
		return getDao();
	}
}
