package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class BundleXmlFhirAdapter extends XmlFhirAdapter<Bundle>
{
	public BundleXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Bundle.class);
	}

	@Override
	protected Bundle fixResource(Bundle resource)
	{
		if (resource.hasIdElement() && resource.getIdElement().hasIdPart()
				&& !resource.getIdElement().hasVersionIdPart() && resource.hasMeta()
				&& resource.getMeta().hasVersionId())
		{
			// TODO Bugfix HAPI is removing version information from bundle.id
			IdType fixedId = new IdType(resource.getResourceType().name(), resource.getIdElement().getIdPart(),
					resource.getMeta().getVersionId());
			resource.setIdElement(fixedId);
		}

		// TODO Bugfix HAPI is removing version information from bundle.id
		resource.getEntry().stream().filter(e -> e.hasResource() && e.getResource() instanceof Bundle)
				.map(e -> (Bundle) e.getResource()).forEach(this::fixResource);

		return resource;
	}
}
