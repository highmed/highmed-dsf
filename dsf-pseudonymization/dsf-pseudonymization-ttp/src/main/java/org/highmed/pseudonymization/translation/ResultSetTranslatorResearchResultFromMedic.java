package org.highmed.pseudonymization.translation;

import java.util.List;

import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.domain.PseudonymizedPersonWithMdat;

public interface ResultSetTranslatorResearchResultFromMedic
{
	List<PseudonymizedPersonWithMdat> translate(ResultSet resultSet);
}
