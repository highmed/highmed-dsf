package org.highmed.openehr.model.structur;

public class Meta
{
	private String _href;
	private String _type;
	private String _schema_version;
	private String _created;
	private String _generator;
	private String _executed_aql;

	private Meta()
	{}

	public String get_href()
	{
		return _href;
	}

	public void set_href(String _href)
	{
		this._href = _href;
	}

	public String get_type()
	{
		return _type;
	}

	public void set_type(String _type)
	{
		this._type = _type;
	}

	public String get_schema_version()
	{
		return _schema_version;
	}

	public void set_schema_version(String _schema_version)
	{
		this._schema_version = _schema_version;
	}

	public String get_created()
	{
		return _created;
	}

	public void set_created(String _created)
	{
		this._created = _created;
	}

	public String get_generator()
	{
		return _generator;
	}

	public void set_generator(String _generator)
	{
		this._generator = _generator;
	}

	public String get_executed_aql()
	{
		return _executed_aql;
	}

	public void set_executed_aql(String _executed_aql)
	{
		this._executed_aql = _executed_aql;
	}
}
