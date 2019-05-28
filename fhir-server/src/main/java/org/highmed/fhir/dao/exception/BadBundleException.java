package org.highmed.fhir.dao.exception;

public class BadBundleException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public BadBundleException(String message)
	{
		super(message);
	}
}
