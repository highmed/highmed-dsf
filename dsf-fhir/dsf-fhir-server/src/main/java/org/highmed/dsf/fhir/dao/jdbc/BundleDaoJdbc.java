package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.BundleDao;
import org.highmed.dsf.fhir.search.parameters.BundleIdentifier;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;

import ca.uhn.fhir.context.FhirContext;

public class BundleDaoJdbc extends AbstractResourceDaoJdbc<Bundle> implements BundleDao
{
	public BundleDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Bundle.class, "bundles", "bundle", "bundle_id", BundleIdentifier::new);
	}

	@Override
	protected Bundle copy(Bundle resource)
	{
		return resource.copy();
	}

	@Override
	protected Bundle getResource(ResultSet result, int index) throws SQLException
	{
		// TODO Bugfix HAPI is removing version information from bundle.id
		Bundle bundle = super.getResource(result, index);
		IdType fixedId = new IdType(bundle.getResourceType().name(), bundle.getIdElement().getIdPart(),
				bundle.getMeta().getVersionId());
		bundle.setIdElement(fixedId);
		return bundle;
	}
}
