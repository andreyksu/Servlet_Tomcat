package ru.andreyksu.annikonenkov.webapp.postgressql;

import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;
import javax.sql.DataSource;

public interface IDataSourceProvider {

    public DataSource getDataSource() throws SQLException, NamingException;

    public Map<String, String> getMapOfAuthorizedUser();

}
