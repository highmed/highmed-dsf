package org.highmed.dsf.fhir.websocket;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

public class LastEventTimeIoTest
{
	@Test
	public void testWriteRead() throws Exception
	{
		Path lastEventTimeFile = Paths.get("target", UUID.randomUUID().toString());
		try
		{
			LastEventTimeIo io = new LastEventTimeIo(lastEventTimeFile);

			LocalDateTime written = io.writeLastEventTime(LocalDateTime.now());
			assertNotNull(written);

			Optional<LocalDateTime> read = io.readLastEventTime();
			assertTrue(read.isPresent());

			assertEquals(written, read.get());
		}
		finally
		{
			Files.deleteIfExists(lastEventTimeFile);
		}
	}

	@Test
	public void testReadNotExistingFile() throws Exception
	{
		Path lastEventTimeFile = Paths.get("target", UUID.randomUUID().toString());
		LastEventTimeIo io = new LastEventTimeIo(lastEventTimeFile);

		assertFalse(io.readLastEventTime().isPresent());
	}

	@Test
	public void testReadEmptyFile() throws Exception
	{
		Path lastEventTimeFile = Paths.get("target", UUID.randomUUID().toString());
		Files.createFile(lastEventTimeFile);
		
		LastEventTimeIo io = new LastEventTimeIo(lastEventTimeFile);
		
		assertFalse(io.readLastEventTime().isPresent());
	}
}
