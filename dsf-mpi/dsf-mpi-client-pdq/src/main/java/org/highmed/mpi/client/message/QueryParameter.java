package org.highmed.mpi.client.message;

public class QueryParameter
{
    private final String field;
    private final String value;

    public QueryParameter(String field, String value)
    {
        this.field = field;
        this.value = value;
    }

    public String getField()
    {
        return field;
    }

    public String getValue()
    {
        return value;
    }
}
