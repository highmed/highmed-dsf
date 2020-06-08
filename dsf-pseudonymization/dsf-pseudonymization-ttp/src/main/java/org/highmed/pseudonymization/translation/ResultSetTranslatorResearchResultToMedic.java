package org.highmed.pseudonymization.translation;

import java.util.List;

import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.domain.PersonWithMdat;

public interface ResultSetTranslatorResearchResultToMedic
{
	ResultSet translate(List<Column> columns, List<PersonWithMdat> personsWithMdat);
}
