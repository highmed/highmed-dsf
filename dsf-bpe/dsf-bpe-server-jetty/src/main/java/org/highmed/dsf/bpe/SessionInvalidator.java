package org.highmed.dsf.bpe;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionInvalidator implements ServletRequestListener
{
	@Override
	public void requestInitialized(ServletRequestEvent sre)
	{
		// nothing to do
	}

	@Override
	public void requestDestroyed(ServletRequestEvent sre)
	{
		HttpServletRequest servletRequest = (HttpServletRequest) sre.getServletRequest();
		HttpSession session = servletRequest.getSession(false);

		if (session != null)
			session.invalidate();
	}
}
