package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.BinaryService;
import org.hl7.fhir.r4.model.Binary;

@Path(BinaryServiceJaxrs.PATH)
public class BinaryServiceJaxrs extends AbstractServiceJaxrs<Binary, BinaryService> implements BinaryService
{
	public static final String PATH = "Binary";

	public BinaryServiceJaxrs(BinaryService delegate)
	{
		super(delegate);
	}
}
