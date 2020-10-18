package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.highmed.dsf.fhir.dao.jdbc.SubscriptionDaoJdbc;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.junit.Test;

public class SubscriptionDaoTest extends AbstractResourceDaoTest<Subscription, SubscriptionDao>
{
	private static final String reason = "Demo Subscription Reason";
	private static final SubscriptionStatus status = SubscriptionStatus.ACTIVE;

	public SubscriptionDaoTest()
	{
		super(Subscription.class, SubscriptionDaoJdbc::new);
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

	@Test
	public void testExistsActiveNotDeletedByAddressDeleted() throws Exception
	{
		Subscription activeSubscriptionToDelete = createResource();
		Subscription createdActiveSubscriptionToDelete = dao.create(activeSubscriptionToDelete);
		assertNotNull(createdActiveSubscriptionToDelete);

		boolean deleted = dao.delete(UUID.fromString(createdActiveSubscriptionToDelete.getIdElement().getIdPart()));
		assertTrue(deleted);

		Subscription activeSubscription = createResource();
		Subscription createdActiveSubscription = dao.create(activeSubscription);
		assertNotNull(createdActiveSubscription);

		Subscription offSubscription = createResource();
		offSubscription.setStatus(SubscriptionStatus.OFF);
		Subscription createdOffSubscription = dao.create(offSubscription);
		assertNotNull(createdOffSubscription);

		List<Subscription> activeSubscriptions = dao.readByStatus(SubscriptionStatus.ACTIVE);
		assertNotNull(activeSubscriptions);
		assertEquals(1, activeSubscriptions.size());
	}
}
