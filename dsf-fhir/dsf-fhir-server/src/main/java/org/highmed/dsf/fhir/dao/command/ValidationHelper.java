package org.highmed.dsf.fhir.dao.command;

import org.highmed.dsf.fhir.authentication.User;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.validation.ValidationResult;

public interface ValidationHelper
{
	ValidationResult checkResourceValidForCreate(User user, Resource resource);

	ValidationResult checkResourceValidForUpdate(User user, Resource resource);
}
