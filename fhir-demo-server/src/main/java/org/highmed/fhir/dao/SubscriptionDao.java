package org.highmed.fhir.dao;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.r4.model.Subscription;

import ca.uhn.fhir.context.FhirContext;

public class SubscriptionDao extends AbstractDomainResourceDao<Subscription>
{
	public SubscriptionDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Subscription.class, "subscriptions", "subscription", "subscription_id");
	}

	@Override
	protected Subscription copy(Subscription resource)
	{
		return resource.copy();
	}

	public PartialResult<Subscription> search(int page, int count) throws SQLException
	{
		return search(createSearchQueryFactory(page, count).build());
	}
}
