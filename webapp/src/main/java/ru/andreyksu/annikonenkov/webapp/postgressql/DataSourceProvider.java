package ru.andreyksu.annikonenkov.webapp.postgressql;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataSourceProvider {

	private static final Logger ___log = LogManager.getLogger(DataSourceProvider.class);

	private final static Map<String, String> _mapOfAuthUser = Collections.synchronizedMap(new HashMap<String, String>());

	private static DataSourceProvider _sqlDataSourceProvider = null;

	private DataSource _dataSource = null;

	private DataSourceProvider() {}

	/**
	 * Статический метод, для создания данного класса, который является
	 * Singleton
	 */
	public static synchronized DataSourceProvider getSQLDataSource() {
		if (_sqlDataSourceProvider == null) {
			_sqlDataSourceProvider = new DataSourceProvider();
			return _sqlDataSourceProvider;
		} else {
			return _sqlDataSourceProvider;
		}
	}

	/**
	 * @return DataSource - возвращает DataSource полученный через JNDI
	 * @throws SQLException
	 * @throws NamingException
	 */

	private DataSource getInnerDataSource() throws SQLException, NamingException {
		___log.info("Фомируем InitialContext в методе getInnerDataSource()");
		InitialContext initalContext = new InitialContext();
		___log.info("Выполняем lookup JNDI для базы данных");
		return (DataSource) initalContext.lookup("java:/comp/env/jdbc/postgres");
	}

	/**
	 * Потоко-безопасный метод, для получения DataSource - коим является
	 * PoolConnection к БД
	 * 
	 * @return DataSource -
	 * @throws SQLException
	 * @throws NamingException
	 */
	public synchronized DataSource getDataSource() throws SQLException, NamingException {
		if (_dataSource == null) {
			_dataSource = getInnerDataSource();
			___log.info("Возвращаем DataSource из класса SQLConnection - первая инициализация");
			return _dataSource;
		} else {
			___log.info("Возвращаем DataSource из класса SQLConnection - уже проинициализирвоали ранее");
			return _dataSource;
		}
	}

	public Map<String, String> getMapAuthUser() {
		return _mapOfAuthUser;
	}
}
