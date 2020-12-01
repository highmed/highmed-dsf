package org.highmed.dsf.bpe.dao;

import java.sql.SQLException;
import java.util.Map;

import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.highmed.dsf.bpe.process.ProcessState;

public interface ProcessStateDao
{
	void updateStates(Map<ProcessKeyAndVersion, ProcessState> states) throws SQLException;

	Map<ProcessKeyAndVersion, ProcessState> getStates() throws SQLException;
}
