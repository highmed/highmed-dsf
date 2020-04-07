package org.highmed.pseudonymization.encoding;

import java.util.HashMap;
import java.util.Map;

import org.highmed.pseudonymization.mpi.Idat;
import org.highmed.pseudonymization.mpi.MasterPatientIndexClient;

class MasterPatientIndexClientTestImpl implements MasterPatientIndexClient
{
	final Map<String, Idat> idats = new HashMap<>();

	MasterPatientIndexClientTestImpl(Map<String, Idat> idats)
	{
		if (idats != null)
			this.idats.putAll(idats);
	}

	@Override
	public Idat fetchIdat(String ehrID)
	{
		return idats.get(ehrID);
	}
}