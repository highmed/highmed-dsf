package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.IdType;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BinaryXmlFhirAdapter extends XmlFhirAdapter<Binary>
{
	public BinaryXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Binary.class);
	}

	@Override
	protected Binary fixResource(Binary resource)
	{
		if (resource.hasIdElement() && resource.getIdElement().hasIdPart()
				&& !resource.getIdElement().hasVersionIdPart() && resource.hasMeta()
				&& resource.getMeta().hasVersionId())
		{
			// TODO Bugfix HAPI is removing version information from binary.id
			IdType fixedId = new IdType(resource.getResourceType().name(), resource.getIdElement().getIdPart(),
					resource.getMeta().getVersionId());
			resource.setIdElement(fixedId);
		}

		return resource;
	}
}
