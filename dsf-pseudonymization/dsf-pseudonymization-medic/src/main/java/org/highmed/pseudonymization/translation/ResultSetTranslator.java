package org.highmed.pseudonymization.translation;

import org.highmed.openehr.model.structure.ResultSet;

public interface ResultSetTranslator
{
	ResultSet translate(ResultSet resultSet);
}
