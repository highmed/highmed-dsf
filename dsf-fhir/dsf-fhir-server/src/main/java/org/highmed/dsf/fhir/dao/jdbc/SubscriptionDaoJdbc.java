package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.SubscriptionDao;
import org.highmed.dsf.fhir.search.parameters.SubscriptionChannelPayload;
import org.highmed.dsf.fhir.search.parameters.SubscriptionChannelType;
import org.highmed.dsf.fhir.search.parameters.SubscriptionCriteria;
import org.highmed.dsf.fhir.search.parameters.SubscriptionStatus;
import org.hl7.fhir.r4.model.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class SubscriptionDaoJdbc extends AbstractResourceDaoJdbc<Subscription> implements SubscriptionDao
{
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionDaoJdbc.class);

	public SubscriptionDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Subscription.class, "subscriptions", "subscription", "subscription_id",
				SubscriptionCriteria::new, SubscriptionStatus::new, SubscriptionChannelType::new,
				SubscriptionChannelPayload::new);
	}

	@Override
	protected Subscription copy(Subscription resource)
	{
		return resource.copy();
	}

	@Override
	public List<Subscription> readByStatus(org.hl7.fhir.r4.model.Subscription.SubscriptionStatus status)
			throws SQLException
	{
		if (status == null)
			return Collections.emptyList();

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement statement = connection
						.prepareStatement("SELECT " + getResourceColumn() + " FROM (SELECT DISTINCT ON ("
								+ getResourceIdColumn() + ") " + getResourceColumn() + " FROM " + getResourceTable()
								+ " WHERE NOT deleted ORDER BY " + getResourceIdColumn() + ", version DESC) AS current_"
								+ getResourceTable() + " WHERE " + getResourceColumn() + "->>'status' = ?"))
		{
			statement.setString(1, status.toCode());

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				List<Subscription> all = new ArrayList<>();

				while (result.next())
					all.add(getResource(result, 1));

				return all;
			}
		}
	}
}
