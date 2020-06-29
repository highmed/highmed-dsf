package org.highmed.dsf.fhir.websocket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class LastEventTimeIo implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(LastEventTimeIo.class);
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	private final Path lastEventTimeFile;

	public LastEventTimeIo(Path lastEventTimeFile)
	{
		this.lastEventTimeFile = lastEventTimeFile;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(lastEventTimeFile, "lastEventTimeFile");

		if (Files.exists(lastEventTimeFile) && !Files.isWritable(lastEventTimeFile))
			throw new IOException("Last event time file at " + lastEventTimeFile.toString() + " not writable");
		else if (!Files.isWritable(lastEventTimeFile.getParent()))
			throw new IOException("Last event time file at " + lastEventTimeFile.toString()
					+ " not existing and parent not writable");
	}

	public Optional<LocalDateTime> readLastEventTime()
	{
		try
		{
			Optional<LocalDateTime> value = Optional
					.of(LocalDateTime.parse(Files.readString(lastEventTimeFile), DATE_TIME_FORMAT));

			logger.debug("Read {} from {}", value.get(), lastEventTimeFile);
			return value;
		}
		catch (DateTimeParseException e)
		{
			logger.warn("Error while reading last event time file: {} {}", e.getClass().getName(), e.getMessage());
			return Optional.empty();
		}
		catch (IOException e)
		{
			logger.warn("Error while reading last event time file: {} {}", e.getClass().getName(), e.getMessage());
			return Optional.empty();
		}
	}

	public LocalDateTime writeLastEventTime(LocalDateTime localDateTime)
	{
		try
		{
			String value = localDateTime.format(DATE_TIME_FORMAT);
			logger.debug("Writing {} to {}", value, lastEventTimeFile);

			Files.writeString(lastEventTimeFile, value, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		}
		catch (IOException e)
		{
			logger.warn("Error while writing last event time file: {} {}", e.getClass().getName(), e.getMessage());
		}

		return localDateTime;
	}

	public void writeLastEventTime(Date authoredOn)
	{
		LocalDateTime localDateTime = LocalDateTime.ofInstant(authoredOn.toInstant(), ZoneId.systemDefault());
		writeLastEventTime(localDateTime);
	}
}
