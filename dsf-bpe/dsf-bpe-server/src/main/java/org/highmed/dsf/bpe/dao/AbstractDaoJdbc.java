package org.highmed.dsf.bpe.dao;

import java.util.Objects;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractDaoJdbc implements InitializingBean
{
	protected final BasicDataSource dataSource;

	public AbstractDaoJdbc(BasicDataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(dataSource, "dataSource");
	}
}
