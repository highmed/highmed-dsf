package org.highmed.dsf.fhir.search.parameters.basic;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.highmed.dsf.fhir.search.SearchQueryParameterError;

public class TokenValueAndSearchType
{
	static final String NOT = ":not";

	public final String systemValue;
	public final String codeValue;
	public final TokenSearchType type;
	public final boolean negated;

	private TokenValueAndSearchType(String systemValue, String codeValue, TokenSearchType type, boolean negated)
	{
		this.systemValue = systemValue;
		this.codeValue = codeValue;
		this.type = type;
		this.negated = negated;
	}

	public static Optional<TokenValueAndSearchType> fromParamValue(String parameterName,
			Map<String, List<String>> queryParameters, Consumer<SearchQueryParameterError> errors)
	{
		String param = null;
		if (queryParameters.containsKey(parameterName) && !queryParameters.get(parameterName).isEmpty())
			param = queryParameters.get(parameterName).get(0);
		else if (queryParameters.containsKey(parameterName + NOT)
				&& !queryParameters.get(parameterName + NOT).isEmpty())
			param = queryParameters.get(parameterName + NOT).get(0);

		if (param != null && !param.isBlank())
		{
			boolean negated = queryParameters.containsKey(parameterName + NOT)
					&& !queryParameters.get(parameterName + NOT).isEmpty();

			if (param.indexOf('|') == -1)
				return Optional.of(new TokenValueAndSearchType(null, param, TokenSearchType.CODE, negated));
			else if (param.charAt(0) == '|')
				return Optional.of(new TokenValueAndSearchType(null, param.substring(1),
						TokenSearchType.CODE_AND_NO_SYSTEM_PROPERTY, negated));
			else if (param.charAt(param.length() - 1) == '|')
				return Optional.of(new TokenValueAndSearchType(param.substring(0, param.length() - 1), null,
						TokenSearchType.SYSTEM, negated));
			else
			{
				String[] splitAtPipe = param.split("[|]");
				return Optional.of(new TokenValueAndSearchType(splitAtPipe[0], splitAtPipe[1],
						TokenSearchType.CODE_AND_SYSTEM, negated));
			}
		}
		else
			return Optional.empty();
	}
}