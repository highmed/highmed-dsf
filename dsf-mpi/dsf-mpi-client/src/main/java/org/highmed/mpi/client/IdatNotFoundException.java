package org.highmed.mpi.client;

public class IdatNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public IdatNotFoundException()
	{
		super();
	}

	public IdatNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public IdatNotFoundException(String message)
	{
		super(message);
	}

	public IdatNotFoundException(Throwable cause)
	{
		super(cause);
	}
}
