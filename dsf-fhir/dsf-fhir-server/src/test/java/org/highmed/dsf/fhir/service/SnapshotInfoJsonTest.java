package org.highmed.dsf.fhir.service;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.highmed.dsf.fhir.spring.config.JsonConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SnapshotInfoJsonTest
{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotInfoJsonTest.class);

	@Test
	public void testWriteRead() throws Exception
	{
		final String profile0 = "profile0";
		final String profile1 = "profile1";
		final String targetProfile0 = "targetProfile0";
		final String targetProfile1 = "targetProfile1";

		SnapshotDependencies dependencies = new SnapshotDependencies(Arrays.asList(profile0, profile1),
				Arrays.asList(targetProfile0, targetProfile1));
		SnapshotInfo info = new SnapshotInfo(dependencies);

		ObjectMapper objectMapper = new JsonConfig().objectMapper();
		String infoString = objectMapper.writer().writeValueAsString(info);
		assertNotNull(infoString);

		logger.info(infoString);

		SnapshotInfo readInfo = objectMapper.reader().forType(SnapshotInfo.class).readValue(infoString);

		assertNotNull(readInfo);
		assertNotNull(readInfo.getDependencies());
		assertNotNull(readInfo.getDependencies().getProfiles());
		assertEquals(2, readInfo.getDependencies().getProfiles().size());
		assertEquals(profile0, readInfo.getDependencies().getProfiles().get(0));
		assertEquals(profile1, readInfo.getDependencies().getProfiles().get(1));
		assertNotNull(readInfo.getDependencies().getTargetProfiles());
		assertEquals(2, readInfo.getDependencies().getTargetProfiles().size());
		assertEquals(targetProfile0, readInfo.getDependencies().getTargetProfiles().get(0));
		assertEquals(targetProfile1, readInfo.getDependencies().getTargetProfiles().get(1));
	}
}
