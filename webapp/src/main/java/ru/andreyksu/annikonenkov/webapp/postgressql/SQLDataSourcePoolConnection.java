package ru.andreyksu.annikonenkov.webapp.postgressql;

import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SQLDataSourcePoolConnection {

	private static final Logger _log = LogManager.getLogger(SQLDataSourcePoolConnection.class);

	private static SQLDataSourcePoolConnection _sqlConnection = null;

	private DataSource _dataSource = null;

	private SQLDataSourcePoolConnection() {}

	/**
	 * Статический метод, для создания данного класса, который является
	 * Singleton
	 */
	public static synchronized SQLDataSourcePoolConnection getSQLDataSource() {
		if (_sqlConnection == null) {
			_sqlConnection = new SQLDataSourcePoolConnection();
			return _sqlConnection;
		} else {
			return _sqlConnection;
		}
	}

	/**
	 * @return DataSource - возвращает DataSource полученный через JNDI
	 * @throws SQLException
	 * @throws NamingException
	 */

	private DataSource getInnerDataSource() throws SQLException, NamingException {
		_log.info("Фомируем InitialContext");
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
	// TODO:Вероятно стоит обернуть в synchronized только блок проверки на null и создания класса т.е. создание if
	public synchronized DataSource getDataSource() throws SQLException, NamingException {
		if (_dataSource == null) {
			_dataSource = getInnerDataSource();
			_log.info("Возвращаем DataSource из класса SQLConnection");
			return _dataSource;
		} else {
			_log.info("Возвращаем DataSource из класса SQLConnection");
			return _dataSource;
		}
	}
}
