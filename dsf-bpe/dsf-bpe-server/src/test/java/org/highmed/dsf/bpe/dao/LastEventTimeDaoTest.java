package org.highmed.dsf.bpe.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;

public class LastEventTimeDaoTest extends AbstractDaoTest
{
	private LastEventTimeDao dao = new LastEventTimeDaoJdbc(defaultDataSource, "test_type");

	@Test
	public void testReadEmpty() throws Exception
	{
		Optional<LocalDateTime> lastEventTime = dao.readLastEventTime();
		assertNotNull(lastEventTime);
		assertTrue(lastEventTime.isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void testWriteLocalDateTimeNull() throws Exception
	{
		dao.writeLastEventTime((LocalDateTime) null);
	}

	@Test(expected = NullPointerException.class)
	public void testWriteDateNull() throws Exception
	{
		dao.writeLastEventTime((Date) null);
	}

	@Test
	public void testWriteLocalDateTime() throws Exception
	{
		LocalDateTime write = LocalDateTime.now();
		LocalDateTime written = dao.writeLastEventTime(write);

		assertNotNull(written);
		assertNotSame(write, written);
		assertEquals(write.truncatedTo(ChronoUnit.MILLIS), written);
	}

	@Test
	public void testWriteLocalDateTime2() throws Exception
	{
		LocalDateTime write1 = LocalDateTime.now();
		LocalDateTime written1 = dao.writeLastEventTime(write1);

		assertNotNull(written1);
		assertNotSame(write1, written1);
		assertEquals(write1.truncatedTo(ChronoUnit.MILLIS), written1);

		LocalDateTime write2 = LocalDateTime.now().plusMinutes(1);
		LocalDateTime written2 = dao.writeLastEventTime(write2);

		assertNotNull(written2);
		assertNotSame(write2, written2);
		assertEquals(write2.truncatedTo(ChronoUnit.MILLIS), written2);
	}

	@Test
	public void testReadWriteReadLocalDateTime() throws Exception
	{
		Optional<LocalDateTime> readLastEvent1 = dao.readLastEventTime();
		assertNotNull(readLastEvent1);
		assertTrue(readLastEvent1.isEmpty());

		LocalDateTime lastEvent = dao.writeLastEventTime(LocalDateTime.now());

		Optional<LocalDateTime> readLastEvent2 = dao.readLastEventTime();
		assertNotNull(readLastEvent2);
		assertTrue(readLastEvent2.isPresent());

		assertEquals(lastEvent, readLastEvent2.get());
	}

	@Test
	public void testReadWriteReadLocalDateTime2() throws Exception
	{
		Optional<LocalDateTime> readLastEvent0 = dao.readLastEventTime();
		assertNotNull(readLastEvent0);
		assertTrue(readLastEvent0.isEmpty());

		LocalDateTime lastEvent = dao.writeLastEventTime(LocalDateTime.now());

		Optional<LocalDateTime> readLastEvent1 = dao.readLastEventTime();
		assertNotNull(readLastEvent1);
		assertTrue(readLastEvent1.isPresent());

		assertEquals(lastEvent, readLastEvent1.get());

		LocalDateTime lastEvent2 = dao.writeLastEventTime(LocalDateTime.now().plusMinutes(1));

		Optional<LocalDateTime> readLastEvent2 = dao.readLastEventTime();
		assertNotNull(readLastEvent2);
		assertTrue(readLastEvent2.isPresent());

		assertEquals(lastEvent2, readLastEvent2.get());
	}

	@Test
	public void testWriteDate() throws Exception
	{
		Date write = new Date();
		Date written = dao.writeLastEventTime(write);

		assertEquals(write, written);
	}

	@Test
	public void testReadWriteReadDate() throws Exception
	{
		Optional<LocalDateTime> readLastEvent1 = dao.readLastEventTime();
		assertNotNull(readLastEvent1);
		assertTrue(readLastEvent1.isEmpty());

		Date lastEvent = dao.writeLastEventTime(new Date());

		Optional<LocalDateTime> readLastEvent2 = dao.readLastEventTime();
		assertNotNull(readLastEvent2);
		assertTrue(readLastEvent2.isPresent());

		assertEquals(LocalDateTime.ofInstant(lastEvent.toInstant(), ZoneId.systemDefault()), readLastEvent2.get());
	}
}
