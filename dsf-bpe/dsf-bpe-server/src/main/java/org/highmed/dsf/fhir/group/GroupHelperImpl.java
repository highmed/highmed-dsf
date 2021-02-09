package org.highmed.dsf.fhir.group;

import static org.highmed.dsf.bpe.ConstantsBase.CODE_TYPE_AQL_QUERY;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_QUERY;

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupHelperImpl implements GroupHelper
{
	private static final Logger logger = LoggerFactory.getLogger(GroupHelperImpl.class);

	@Override
	public String extractAqlQuery(Group group)
	{
		List<Extension> queries = group.getExtension().stream()
				.filter(extension -> extension.getUrl().equals(EXTENSION_HIGHMED_QUERY))
				.filter(extension -> CODE_TYPE_AQL_QUERY
						.compareTo(((Expression) extension.getValue()).getLanguageElement()) == 0)
				.collect(Collectors.toList());

		if (queries.size() != 1)
		{
			logger.error("Number of aql queries is not 1, got {}", queries.size());
			throw new IllegalArgumentException("Number of aql queries is not =1, got " + queries.size());
		}

		return ((Expression) queries.get(0).getValue()).getExpression();
	}
}
