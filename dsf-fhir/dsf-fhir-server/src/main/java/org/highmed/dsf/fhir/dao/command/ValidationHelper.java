package org.highmed.dsf.fhir.dao.command;

import org.highmed.dsf.fhir.authentication.User;
import org.hl7.fhir.r4.model.Resource;

public interface ValidationHelper
{
	void checkResourceValidForCreate(User user, Resource resource);

	void checkResourceValidForUpdate(User user, Resource resource);
}
