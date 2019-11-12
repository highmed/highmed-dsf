package org.highmed.dsf.fhir.group;

import java.util.List;
import java.util.stream.Collectors;

import org.highmed.dsf.bpe.Constants;
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
		// TODO: APPLICATION_XFHIRQUERY to support AQL
		List<Extension> queries = group.getExtension().stream()
				.filter(extension -> extension.getUrl().equals(Constants.EXTENSION_QUERY_URI))
				.filter(extension -> Expression.ExpressionLanguage.APPLICATION_XFHIRQUERY.toCode()
						.equals(((Expression) extension.getValue()).getLanguage())).collect(Collectors.toList());

		if (queries.size() != 1)
		{
			logger.error("Number of aql queries is not =1, got {}", queries.size());
			throw new IllegalArgumentException("Number of aql queries is not =1, got " + queries.size());
		}

		return ((Expression) queries.get(0).getValue()).getExpression();
	}
}
