package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.ProvenanceDaoJdbc;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Signature;

import ca.uhn.fhir.context.FhirContext;

public class ProvenanceDaoTest extends AbstractResourceDaoTest<Provenance, ProvenanceDao>
{
	private final Date recorded = new GregorianCalendar(2019, 0, 1, 10, 20, 30).getTime();
	private final byte[] signatureData = "foo bar baz".getBytes();

	public ProvenanceDaoTest()
	{
		super(Provenance.class);
	}

	@Override
	protected ProvenanceDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new ProvenanceDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected Provenance createResource()
	{
		Provenance provenance = new Provenance();
		provenance.setRecorded(recorded);
		return provenance;
	}

	@Override
	protected void checkCreated(Provenance resource)
	{
		assertEquals(recorded, resource.getRecorded());
	}

	@Override
	protected Provenance updateResource(Provenance resource)
	{
		resource.setSignature(Collections.singletonList(new Signature().setData(signatureData)));
		return resource;
	}

	@Override
	protected void checkUpdates(Provenance resource)
	{
		assertEquals(1, resource.getSignature().size());
		assertTrue(Arrays.equals(signatureData, resource.getSignatureFirstRep().getData()));
	}
}
