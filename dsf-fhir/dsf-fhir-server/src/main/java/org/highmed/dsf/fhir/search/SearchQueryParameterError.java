package org.highmed.dsf.fhir.search;

import java.util.List;

public class SearchQueryParameterError
{
	public static enum SearchQueryParameterErrorType
	{
		UNSUPPORTED_PARAMETER, UNSUPPORTED_NUMBER_OF_VALUES, UNPARSABLE_VALUE
	}

	private final SearchQueryParameterErrorType type;
	private final String parameterName;
	private final List<String> parameterValues;
	private final Exception exception;
	private final String message;

	public SearchQueryParameterError(SearchQueryParameterErrorType type, String parameterName,
			List<String> parameterValues)
	{
		this(type, parameterName, parameterValues, null, null);
	}

	public SearchQueryParameterError(SearchQueryParameterErrorType type, String parameterName,
			List<String> parameterValues, String message)
	{
		this(type, parameterName, parameterValues, null, message);
	}

	public SearchQueryParameterError(SearchQueryParameterErrorType type, String parameterName,
			List<String> parameterValues, Exception exception)
	{
		this(type, parameterName, parameterValues, exception, null);
	}

	public SearchQueryParameterError(SearchQueryParameterErrorType type, String parameterName,
			List<String> parameterValues, Exception exception, String message)
	{
		this.type = type;
		this.parameterName = parameterName;
		this.parameterValues = parameterValues;
		this.exception = exception;
		this.message = message;
	}

	public SearchQueryParameterErrorType getType()
	{
		return type;
	}

	public String getParameterName()
	{
		return parameterName;
	}

	public List<String> getParameterValues()
	{
		return parameterValues;
	}

	public Exception getException()
	{
		return exception;
	}

	public String getMessage()
	{
		return message;
	}

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder("parameter: ");
		b.append(parameterName);
		b.append(", error: ");
		b.append(type);

		if (exception != null || message != null)
		{
			b.append(", message: '");
			if (exception != null)
			{
				b.append(exception.getClass().getSimpleName());
				b.append(" - ");
				b.append(exception.getMessage());
				b.append("'");
			}
			else if (message != null)
			{
				b.append(message);
				b.append("'");
			}
		}
		if (parameterValues != null)
		{
			b.append(", values: ");
			b.append(parameterValues);
		}

		return b.toString();
	}
}
