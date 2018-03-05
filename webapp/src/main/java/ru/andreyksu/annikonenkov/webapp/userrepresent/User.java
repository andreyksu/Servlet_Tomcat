package ru.andreyksu.annikonenkov.webapp.userrepresent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.logging.log4j.Logger;

public class User implements IUser {

	private static Set<String> _setOfAuthUser = null;

	private final static int minLenght = 7;

	private final static int maxLenght = 100;

	private Logger _log = null;

	private DataSource _dataSourcel = null;

	private Connection _conniction = null;

	// TODO: Изначально планировал и сделал через внутренний класс, как возврат
	// не просто boolean а через сложный объект. Удалил класс. Пока решил
	// сделать через map.
	private final Map<String, String> resultOfCheckExistUser = new HashMap<String, String>();

	public User(Logger log, DataSource dataSource) {
		_setOfAuthUser = Collections.synchronizedSet(new HashSet<String>());
		_log = log;
		_dataSourcel = dataSource;
	}

	@Override
	public boolean authorizedInSystem(String email, String password) {
		if (!isExistUserInSystem(email))
			return false;
		if (!(resultOfCheckExistUser.get("password").equals(password)))
			return false;
		if (resultOfCheckExistUser.get("isactive").equals("false"))
			return false;
		_setOfAuthUser.add(email);
		return true;
	}

	@Override
	public boolean isAuthorizedUser(String email) {
		if (_setOfAuthUser.contains(email))
			return true;
		return false;
	}

	@Override
	public void unAuthorizedUser(String email) {
		_setOfAuthUser.remove(email);
	}

	@Override
	public boolean registrateUser(String email, String password, String name) {
		_log.debug("Начинаем регистрировать пользователя!!!");
		PreparedStatement preparedStatement = null;
		if (isExistUserInSystem(email))
			return false;
		try {
			preparedStatement = getPreparedStatementForRegistrateUser();
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, password);
			preparedStatement.setString(3, name);
			int count = preparedStatement.executeUpdate();
			_log.info(String.format("Удалось вставить количество =%4d записей, при регистрации пользователя", count));
		} catch (SQLException e) {
			_log.error("Ошибка при добавлениии пользователя", e);
			throw new RuntimeException(e);
		} finally {
			try {
				preparedStatement.close();
				_conniction.close();
			} catch (SQLException e) {
				_log.error("Схватили ошибку при закрытии коннекта, и подготовленной транзацкции", e);
				throw new RuntimeException(e);
			}
		}
		return true;
	}

	/**
	 * Подготавливает {@code PreparedStatement} для метода
	 * {@link #registrateUser(String email, String password, String name)}
	 * 
	 * @return Подготовелнную транзакцию.
	 * @throws SQLException
	 */
	private PreparedStatement getPreparedStatementForRegistrateUser() throws SQLException {
		_conniction = _dataSourcel.getConnection();
		return _conniction.prepareStatement("INSERT INTO principal (email, password, name) values (?, ?, ?)");
	}

	@Override
	public boolean isExistUserInSystem(String email) {
		_log.info("Проверка наличия пользовтеля в БД");
		PreparedStatement prepStatement = null;
		if (!isValidUserForQuery(email))
			return false;
		try {
			prepStatement = getPrepStatIsExistUserByLogin();
			prepStatement.setString(1, email);
			ResultSet result = prepStatement.executeQuery();
			int count = setResultOfQueryExistUser(result);
			return count == 0 ? false : true;
		} catch (SQLException e) {
			_log.error("Ошибка при проверке наличия пользователя в системе", e);
			throw new RuntimeException(e);
		} finally {
			try {
				prepStatement.close();
				_conniction.close();
			} catch (SQLException e) {
				_log.error("Ошибка при закрытии коннектов или же PreparedStatment", e);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Подготавливает {@code PreparedStatement} для метода
	 * {@link #isExistUserInSystem(String email)}
	 * 
	 * @return Подготовелнную транзакцию.
	 * @throws SQLException
	 */

	private PreparedStatement getPrepStatIsExistUserByLogin() throws SQLException {
		_conniction = _dataSourcel.getConnection();
		return _conniction.prepareStatement("SELECT * FROM principal WHERE email=?");
	}

	/**
	 * Заполняет map результатом запроса. Используется в
	 * {@link #isExistUserInSystem(String email)}
	 */
	private int setResultOfQueryExistUser(ResultSet result) throws SQLException {
		_log.info("Помещаем результат запроса, на существование пользователя в системе");
		int count = 0;
		while (result.next()) {
			resultOfCheckExistUser.put("email", result.getString("email").trim());
			resultOfCheckExistUser.put("password", result.getString("password").trim());
			resultOfCheckExistUser.put("name", result.getString("name").trim());
			resultOfCheckExistUser.put("regdata", result.getTimestamp("regdata").toString());
			if (result.getBoolean("isactive")) {
				resultOfCheckExistUser.put("isactive", "true");
			} else {
				resultOfCheckExistUser.put("isactive", "false");
			}
			count++;
			_log.info("Результат запроса >>> " + count + " >>> " + resultOfCheckExistUser);
		}
		_log.info(String.format("Итого записей = %4d", count));
		return count;
	}

	/**
	 * @param email - Email пользователя, он же и выступает как логин.
	 * @return {@code true} если {@code email} подходит под условия/требования к
	 *         даннному полю. В ином случае возвращает {@code false}
	 */

	private boolean isValidUserForQuery(String email) {
		if ((email == null || email.length() < minLenght || email.length() > maxLenght))
			return false;
		return true;
	}

}
