package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.ProvenanceDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.ProvenanceService;
import org.hl7.fhir.r4.model.Provenance;

public class ProvenanceServiceImpl extends AbstractServiceImpl<ProvenanceDao, Provenance> implements ProvenanceService
{
	public ProvenanceServiceImpl(String serverBase, int defaultPageCount, ProvenanceDao provenanceDao,
			ResourceValidator validator, EventManager eventManager, ServiceHelperImpl<Provenance> serviceHelper)
	{
		super(serverBase, defaultPageCount, provenanceDao, validator, eventManager, serviceHelper);
	}
}
