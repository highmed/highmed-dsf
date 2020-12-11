package org.highmed.dsf.fhir.group;

import static org.highmed.dsf.bpe.ConstantsBase.AQL_QUERY_TYPE;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_QUERY_URI;

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
				.filter(extension -> extension.getUrl().equals(EXTENSION_QUERY_URI))
				.filter(extension -> AQL_QUERY_TYPE.compareTo(((Expression) extension.getValue()).getLanguageElement())
						== 0).collect(Collectors.toList());

		if (queries.size() != 1)
		{
			logger.error("Number of aql queries is not 1, got {}", queries.size());
			throw new IllegalArgumentException("Number of aql queries is not =1, got " + queries.size());
		}

		return ((Expression) queries.get(0).getValue()).getExpression();
	}
}
