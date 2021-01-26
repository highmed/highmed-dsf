package org.highmed.dsf.fhir.authorization;

import java.util.Optional;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;

final class ResearchStudyHelper
{
	public static final String PARTICIPATING_MEDIC_EXTENSION_URL = "http://highmed.org/fhir/StructureDefinition/extension-participating-medic";
	public static final String PARTICIPATING_TTP_EXTENSION_URL = "http://highmed.org/fhir/StructureDefinition/extension-participating-ttp";

	private ResearchStudyHelper()
	{
	}

	static Stream<Reference> getParticipatingMedicReferences(ResearchStudy resource)
	{
		return resource.getExtensionsByUrl(PARTICIPATING_MEDIC_EXTENSION_URL).stream().map(e -> e.getValue())
				.filter(t -> t instanceof Reference).map(t -> ((Reference) t));
	}

	static Optional<Reference> getParticipatingTtpReference(ResearchStudy resource)
	{
		return Optional.ofNullable(resource.getExtensionByUrl(PARTICIPATING_TTP_EXTENSION_URL)).map(e -> e.getValue())
				.filter(t -> t instanceof Reference).map(t -> ((Reference) t));
	}
}
