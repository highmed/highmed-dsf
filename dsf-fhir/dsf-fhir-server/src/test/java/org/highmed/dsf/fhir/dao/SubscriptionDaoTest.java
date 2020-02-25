package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.SubscriptionDaoJdbc;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;

import ca.uhn.fhir.context.FhirContext;

public class SubscriptionDaoTest extends AbstractResourceDaoTest<Subscription, SubscriptionDao>
{
	private static final String reason = "Demo Subscription Reason";
	private static final SubscriptionStatus status = SubscriptionStatus.ACTIVE;

	public SubscriptionDaoTest()
	{
		super(Subscription.class);
	}

	@Override
	protected SubscriptionDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new SubscriptionDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected Subscription createResource()
	{
		Subscription subscription = new Subscription();
		subscription.setStatus(status);
		return subscription;
	}

	@Override
	protected void checkCreated(Subscription resource)
	{
		assertEquals(status, resource.getStatus());
	}

	@Override
	protected Subscription updateResource(Subscription resource)
	{
		resource.setReason(reason);
		return resource;
	}

	@Override
	protected void checkUpdates(Subscription resource)
	{
		assertEquals(reason, resource.getReason());
	}
}
