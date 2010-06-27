package org.lindenb.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ObjectSQLFactory<T>
	{
	public T newInstance(ResultSet row) throws SQLException;
	}
