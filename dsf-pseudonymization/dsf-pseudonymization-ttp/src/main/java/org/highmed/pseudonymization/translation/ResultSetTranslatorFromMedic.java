package org.highmed.pseudonymization.translation;

import java.util.List;

import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.domain.PersonWithMdat;

public interface ResultSetTranslatorFromMedic
{
	List<PersonWithMdat> translate(String organization, ResultSet resultSet);
}
