package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertNotNull;

import java.nio.charset.StandardCharsets;

import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Library;
import org.junit.Test;

public class LibraryIntegrationTest extends AbstractIntegrationTest
{
	@Test
	public void testCreateValidByLocalUser() throws Exception
	{
		Library library = new Library();
		library.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/authorization-role").setCode("REMOTE");
		library.setUrl("https://foo.bar/fhir/Library/30561ba6-106f-4d52-bb8d-e49e20a40d40");
		library.setStatus(Enumerations.PublicationStatus.ACTIVE);
		library.getType().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/library-type")
				.setCode("logic-library");
		library.getContentFirstRep().setContentType("text/cql").setData("Zm9vCg==".getBytes(StandardCharsets.UTF_8));

		Library created = getWebserviceClient().create(library);
		assertNotNull(created);
		assertNotNull(created.getIdElement().getIdPart());
		assertNotNull(created.getIdElement().getVersionIdPart());
	}
}
