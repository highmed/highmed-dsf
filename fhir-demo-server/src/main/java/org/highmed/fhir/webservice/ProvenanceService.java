package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.ProvenanceDao;
import org.hl7.fhir.r4.model.Provenance;

@Path(ProvenanceService.RESOURCE_TYPE_NAME)
public class ProvenanceService extends AbstractService<ProvenanceDao, Provenance>
{
	public static final String RESOURCE_TYPE_NAME = "Provenance";

	public ProvenanceService(String serverBase, int defaultPageCount, ProvenanceDao provenanceDao)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE_NAME, provenanceDao, withSearchParameters());
	}
}
