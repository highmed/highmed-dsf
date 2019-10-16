package org.highmed.dsf.fhir;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class FhirContextLoaderListener extends ContextLoaderListener
{
	private AnnotationConfigWebApplicationContext contex;

	public FhirContextLoaderListener(AnnotationConfigWebApplicationContext context)
	{
		super(context);

		this.contex = context;
	}

	public AnnotationConfigWebApplicationContext getContex()
	{
		return contex;
	}
}
