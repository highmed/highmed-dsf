package org.highmed.dsf.fhir.dao.exception;

// This class has been added by Taha Alhersh
// Catching resource not marked deleted before expunging it
public class ResourceNotMarkedDeletedException extends Exception
{
    private static final long serialVersionUID = 1L;

    private final String id;

    public ResourceNotMarkedDeletedException(String id)
    {
        this.id = id;

    }

    public String getId()
    {
        return id;
    }

}
