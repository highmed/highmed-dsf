package org.highmed.dsf.bpe.dao;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public interface LastEventTimeDao
{
	Optional<LocalDateTime> readLastEventTime() throws SQLException;

	/**
	 * @param lastEvent
	 *            not <code>null</code>
	 * @return the given <b>lastEvent</b> with millisecond precision
	 * @throws SQLException
	 *             if a database access error occurs
	 * @see LocalDateTime#truncatedTo(java.time.temporal.TemporalUnit)
	 */
	LocalDateTime writeLastEventTime(LocalDateTime lastEvent) throws SQLException;

	default Date writeLastEventTime(Date lastEvent) throws SQLException
	{
		LocalDateTime ldt = writeLastEventTime(
				lastEvent == null ? null : LocalDateTime.ofInstant(lastEvent.toInstant(), ZoneId.systemDefault()));

		return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
	}
}
