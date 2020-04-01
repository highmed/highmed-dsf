package org.highmed.pseudonymization.encoding;

import org.highmed.openehr.model.structure.ResultSet;

public interface ResultSetEncoder
{
	String EHRID_COLUMN_NAME = "EHRID";
	String EHRID_COLUMN_PATH = "/ehr_id/value";
	String MEDICID_COLUMN_NAME = "MEDICID";
	String MEDICID_COLUMN_PATH = "/medic_id/value";
	String RBF_COLUMN_NAME = "RBF";
	String RBF_COLUMN_PATH = "/rbf/value";
	String PSN_COLUMN_NAME = "PSN";
	String PSN_COLUMN_PATH = "/psn/value";

	ResultSet encode(ResultSet resultSet);
	
	ResultSet decode(ResultSet resultSet);
}
