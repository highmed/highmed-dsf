package org.highmed.dsf.fhir.dao.command;

@FunctionalInterface
public interface AuthorizationCommand
{
	static AuthorizationCommand empty()
	{
		return () ->
		{
		};
	}

	static AuthorizationCommand concat(AuthorizationCommand c1, AuthorizationCommand c2)
	{
		return () ->
		{
			c1.execute();
			c2.execute();
		};
	}

	void execute();
}
