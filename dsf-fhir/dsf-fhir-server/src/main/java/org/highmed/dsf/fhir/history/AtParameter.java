package org.highmed.dsf.fhir.history;

import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Resource;

public class AtParameter extends AbstractDateTimeParameter<DomainResource>
{
	public AtParameter()
	{
		super("_at", "last_updated");
	}

	@Override
	public boolean matches(Resource resource)
	{
		// Not implemented for history
		throw new UnsupportedOperationException();
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		// Not implemented for history
		throw new UnsupportedOperationException();
	}
}
