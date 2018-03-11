package ru.andreyksu.annikonenkov.webapp.postgressql;

import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SQLDataSourceProvider {

	private static final Logger _log = LogManager.getLogger(SQLDataSourceProvider.class);

	private static SQLDataSourceProvider _sqlDataSourceProvider = null;

	private DataSource _dataSource = null;

	private SQLDataSourceProvider() {}

	/**
	 * Статический метод, для создания данного класса, который является
	 * Singleton
	 */
	public static synchronized SQLDataSourceProvider getSQLDataSource() {
		if (_sqlDataSourceProvider == null) {
			_sqlDataSourceProvider = new SQLDataSourceProvider();
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
		_log.info("Фомируем InitialContext в методе getInnerDataSource()");
		InitialContext initalContext = new InitialContext();
		_log.info("Выполняем lookup JNDI для базы данных");
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
			_log.info("Возвращаем DataSource из класса SQLConnection - первая инициализация");
			return _dataSource;
		} else {
			_log.info("Возвращаем DataSource из класса SQLConnection - уже проинициализирвоали ранее");
			return _dataSource;
		}
	}
}
