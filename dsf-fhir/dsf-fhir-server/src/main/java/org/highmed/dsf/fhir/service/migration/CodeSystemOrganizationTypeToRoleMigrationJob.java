package org.highmed.dsf.fhir.service.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.dao.OrganizationAffiliationDao;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class CodeSystemOrganizationTypeToRoleMigrationJob implements MigrationJob, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(CodeSystemOrganizationTypeToRoleMigrationJob.class);

	private static final String CODESYSTEM_ORGANIZATION_TYPE_URL = "http://highmed.org/fhir/CodeSystem/organization-type";
	private static final String CODESYSTEM_ORGANIZATION_ROLE_URL = "http://highmed.org/fhir/CodeSystem/organization-role";

	private final OrganizationAffiliationDao affiliationDao;

	public CodeSystemOrganizationTypeToRoleMigrationJob(OrganizationAffiliationDao affiliationDao)
	{
		this.affiliationDao = affiliationDao;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(affiliationDao, "affiliationDao");
	}

	@Override
	public void execute() throws Exception
	{
		logger.info("Migrating all OrganizationAffiliations to include codes using the deprecated CodeSystem with URL '"
				+ CODESYSTEM_ORGANIZATION_TYPE_URL + "' and the new CodeSystem with URL '"
				+ CODESYSTEM_ORGANIZATION_ROLE_URL + "'");

		List<OrganizationAffiliation> organizationAffiliations = affiliationDao.readAll();

		for (OrganizationAffiliation affiliation : organizationAffiliations)
		{
			List<String> existingTypes = getExistingCodingValues(affiliation, CODESYSTEM_ORGANIZATION_TYPE_URL);
			List<String> existingRoles = getExistingCodingValues(affiliation, CODESYSTEM_ORGANIZATION_ROLE_URL);

			boolean typeAdded = addMissingCodingToAffiliationIfPresent(affiliation, existingTypes, existingRoles,
					CODESYSTEM_ORGANIZATION_TYPE_URL, CODESYSTEM_ORGANIZATION_ROLE_URL);
			boolean roleAdded = addMissingCodingToAffiliationIfPresent(affiliation, existingRoles, existingTypes,
					CODESYSTEM_ORGANIZATION_ROLE_URL, CODESYSTEM_ORGANIZATION_TYPE_URL);

			if (typeAdded || roleAdded)
				affiliationDao.update(affiliation);
		}
	}

	private List<String> getExistingCodingValues(OrganizationAffiliation affiliation, String codingSystem)
	{
		return affiliation.getCode().stream().flatMap(cc -> cc.getCoding().stream())
				.filter(c -> codingSystem.equals(c.getSystem())).map(Coding::getCode).collect(Collectors.toList());
	}

	private boolean addMissingCodingToAffiliationIfPresent(OrganizationAffiliation affiliation,
			List<String> existingCodingValues, List<String> toCheckCodingValues, String oldCodingSystem,
			String newCodingSystem)
	{
		List<String> notExistingCodingValues = new ArrayList<>(existingCodingValues);
		notExistingCodingValues.removeAll(toCheckCodingValues);

		for (String toAdd : notExistingCodingValues)
		{
			affiliation.getCode().stream().filter(cc -> cc.hasCoding(oldCodingSystem, toAdd)).findFirst()
					.ifPresent(cc -> cc.addCoding(new Coding().setSystem(newCodingSystem).setCode(toAdd)));
		}

		return !notExistingCodingValues.isEmpty();
	}
}
