package ru.andreyksu.annikonenkov.webapp.userrepresent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.logging.log4j.Logger;

public class User implements IUser {

	private final static int minLenght = 7;

	private final static int maxLenght = 100;

	private Logger ___log = null;

	private final DataSource _dataSourcel;

	private final String _email;

	private final Map<String, String> resultOfCheckExistUser = new HashMap<String, String>();

	private static final String _forIsExistUserInSystem = "SELECT * FROM principal WHERE email=?";

	private static final String _forRegistrateUser = "INSERT INTO principal (email, password, name) values (?, ?, ?)";

	private static final String _forSetUserActive = "UPDATE principal SET isactive = ? where email=?";

	private static final String _forGrandUserAdminRole = "UPDATE principal SET isadmin = ? where email=?";

	/**
	 * Конструиерут объект представляющего пользователя. Так как основным
	 * отличием пользователя является его имя Конструктор
	 * 
	 * @param log
	 * @param dataSource
	 * @param email - Индификатор пользователя для которого был созданн данный
	 *            класс.
	 */
	public User(String email, Logger log, DataSource dataSource) {
		_email = email;
		_dataSourcel = dataSource;
		___log = log;
	}

	/**
	 * Проверяет, есть ли пользователь в системе. Проверка ведется по
	 * {@code email}, с которым был создан экземпляр данного класса.
	 * <p>
	 * После себя закрывает {@link java.sql.PreparedStatement} и
	 * {@link java.sql.Connection}
	 * 
	 * @return Возвращает {@code true} если пользователь найден в БД.
	 *         {@code false} если по {@code email} не прошел валидацию, или если
	 *         кол. записей в БД для данного email = 0, то возвращаем.
	 * @throws SQLException
	 */
	@Override
	public boolean isExistUserInSystem() throws IOException {
		___log.debug("Проверка наличия пользовтеля в БД User.isExistUserInSystem()");
		if (!isValidUserForQuery(_email))
			return false;
		try (Connection connection = _dataSourcel.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(_forIsExistUserInSystem)) {
			preparedStatement.setString(1, _email);
			ResultSet result = preparedStatement.executeQuery();
			int count = setResultOfQueryExistUser(result);
			return count == 0 ? false : true;
		} catch (SQLException e) {
			___log.error("Ошибка при проверке наличия пользователя в системе", e);
			throw new IOException(e);
		}
	}

	/**
	 * Предварительно проверяет, есть ли пользователь в системе. См.
	 * {@link isExistUserInSystem}
	 * <p>
	 * 
	 * @return Возвращает {@code String-password} если пользователь найден в БД.
	 *         {@code null} если по {@code email} не прошел валидацию, или если
	 *         пользователь не найден в системе.
	 * @throws SQLException
	 */
	@Override
	public String getPassword() throws IOException {
		___log.debug("Ищем пароль для созданного пользователя! ");
		if (resultOfCheckExistUser.get("password") == null) {
			if (!isExistUserInSystem())
				return null;
		}
		return resultOfCheckExistUser.get("password");
	}

	/**
	 * Предварительно проверяет, есть ли пользователь в системе. См.
	 * {@link isExistUserInSystem}
	 * <p>
	 * 
	 * @return Возвращает {@code String-name} если пользователь найден в БД.
	 *         {@code null} если по {@code email} не прошел валидацию, или если
	 *         пользователь не найден в системе.
	 * @throws SQLException
	 */
	@Override
	public String getName() throws IOException {
		___log.debug("Ищем пароль для созданного пользователя!");
		if (resultOfCheckExistUser.get("name") == null) {
			if (!isExistUserInSystem())
				return null;
		}
		return resultOfCheckExistUser.get("name");
	}

	@Override
	public String getData() throws IOException {
		___log.debug("Ищем пароль для созданного пользователя!");
		if (resultOfCheckExistUser.get("regdata") == null) {
			if (!isExistUserInSystem())
				return null;
		}
		return resultOfCheckExistUser.get("regdata");
	}

	@Override
	public boolean isUserAdmin() throws IOException {
		___log.debug("Проверяем, является ли пользователь Админом?");
		if (resultOfCheckExistUser.get("isadmin") == null) {
			if (!isExistUserInSystem())
				return false;
		}
		return resultOfCheckExistUser.get("isadmin").equals("true") ? true : false;
	}

	/**
	 * Проверяет, активен ли пользователь. Т.е. разрешено ли ему работать в
	 * системе.
	 * <p>
	 * Как вариант может быть запрет из за бана(блокировки) или из за удаления
	 * его из сисетмы. Проверка ведется по {@code email}. Предварительно ведется
	 * проверка валидный ли email, есть ли пользователь в системе.
	 * <p>
	 * После себя закрывает {@link java.sql.PreparedStatement} и
	 * {@link java.sql.Connection}
	 * 
	 * @return Возвращает {@code true} если запись пользователя
	 *         активена.{@code false} Если запись не активна, или пользователь
	 *         не существует в системе.
	 * @throws IOExceprion
	 */
	@Override
	public boolean isUserActive() throws IOException {
		___log.debug("Проверяем, активен ли пользователь/не заблокирован ли?");
		if (resultOfCheckExistUser.get("isactive") == null) {
			if (!isExistUserInSystem())
				return false;
		}
		___log.debug("Пользователь активен?");
		return resultOfCheckExistUser.get("isactive").equals("true") ? true : false;
	}

	/**
	 * Производится регистрация пользователя в системе, путем добавления записи
	 * в БД. Предварительно проверяется есть ли данный пользователь в системе. И
	 * не является ли пустыми значения полей.
	 * <p>
	 * После себя закрывает {@link java.sql.PreparedStatement} и
	 * {@link java.sql.Connection}
	 * 
	 * @param password - Пароль пользователя
	 * @param name - Имя пользователя.
	 * @return {@code true} - если регистрация прошла успешно. {@code false} -
	 *         если авторизация данный пользователь уже существует в системе или
	 *         же {@code email} не прошел валидацию.
	 * @throws SQLException
	 */
	@Override
	public boolean registrateUser(String password, String name) throws IOException {
		___log.debug("Начинаем регистрировать пользователя!!!");
		if (isExistUserInSystem() || isValidPasswordAndName(password, name))
			return false;
		try (Connection connection = _dataSourcel.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(_forRegistrateUser)) {
			preparedStatement.setString(1, _email);
			preparedStatement.setString(2, password);
			preparedStatement.setString(3, name);
			preparedStatement.executeUpdate();
			___log.debug("Запись успешно добавлена");
		} catch (SQLException e) {
			___log.error("Ошибка при добавлениии пользователя", e);
			throw new IOException(e);
		}
		return true;
	}

	/**
	 * Устанавливает или сбрасывает активность пользователя.
	 * <p>
	 * После себя закрывает {@link java.sql.PreparedStatement} и
	 * {@link java.sql.Connection}
	 * 
	 * @param stat - true - если нужно активаировать, false - если нужно
	 *            деактивировать.
	 * @return {@code true} - если активация/деактивация прошла успешно.
	 *         {@code false} - если данного пользователя нет в системе, или если
	 *         же пользовтеля не прошел валидацию по {@code email}.
	 * @throws SQLException
	 */
	@Override
	public boolean setUserActive(boolean stat) throws IOException {
		String status = stat ? "true" : "false";
		___log.debug("Устанавливаем пользователю статус Active = {}!!!", status);
		if (resultOfCheckExistUser.get("isactive") == null) {
			if (!isExistUserInSystem()) {
				return false;
			}
		}
		try (Connection connection = _dataSourcel.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(_forSetUserActive)) {
			preparedStatement.setString(1, status);
			preparedStatement.setString(2, _email);
			preparedStatement.executeUpdate();
			___log.debug("Запись '{}' успешно обновлена", _email);
		} catch (SQLException e) {
			___log.error("Ошибка при изменении записи пользователя Поле 'isactive' .", e);
			throw new IOException(e);
		}
		return true;
	}

	/**
	 * Предоставляет/изымает права Администратора.
	 * <p>
	 * После себя закрывает {@link java.sql.PreparedStatement} и
	 * {@link java.sql.Connection}
	 * 
	 * @param stat - true - если даем права Администратора, false - если изымаем
	 *            права Администратора.
	 * @return {@code true} - если предоставление/изъятие прошло успешно..
	 *         {@code false} - если данного пользователя нет в системе, или если
	 *         же пользовтеля не прошел валидацию по {@code email}.
	 * @throws SQLException
	 */
	@Override
	public boolean grandUserAdminRole(boolean admin) throws IOException {
		___log.debug("Устанавливаем/изымаем пользователю права Администратора!!!");
		String status = admin ? "true" : "false";
		if (resultOfCheckExistUser.get("isadmin") == null) {
			if (!isExistUserInSystem()) {
				return false;
			}
		}
		try (Connection connection = _dataSourcel.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(_forGrandUserAdminRole)) {
			preparedStatement.setString(1, status);
			preparedStatement.setString(2, _email);
			int count = preparedStatement.executeUpdate();
			___log.debug(String.format("Удалось изменить =%4d записей, при изменении пользователей", count));
		} catch (SQLException e) {
			___log.error("Ошибка при изменении записи пользователя Поле 'isadmin' .", e);
			throw new IOException(e);
		}
		return true;
	}

	/**
	 * Заполняет map результатом запроса. Используется в
	 * {@link #isExistUserInSystem(String email)}
	 */
	private int setResultOfQueryExistUser(ResultSet result) throws SQLException {
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
			if (result.getBoolean("isadmin")) {
				resultOfCheckExistUser.put("isadmin", "true");
			} else {
				resultOfCheckExistUser.put("isadmin", "false");
			}
			count++;
			___log.debug("Результат запроса >>> " + count + " >>> " + resultOfCheckExistUser);
			___log.debug("Помещаем результат запроса во внутренний Map в методе setResultOfQueryExistUser");
		}
		___log.debug(String.format("Итого записей = %4d", count));
		return count;
	}

	/**
	 * @param email - Email пользователя, он же и выступает как логин.
	 * @return {@code true} если {@code email} подходит под условия/требования к
	 *         даннному полю. В ином случае возвращает {@code false}
	 */

	private boolean isValidUserForQuery(String email) {
		___log.debug("Проверяем валидность email на длину и на null!");
		if ((email == null || email.length() < minLenght || email.length() > maxLenght))
			return false;
		return true;
	}

	private boolean isValidPasswordAndName(String password, String name) {
		___log.debug("Проверяем валидность Password и Name на длину и на null!");
		if ((password == null || password.length() < minLenght || password.length() > maxLenght))
			return false;
		if ((name == null || name.length() < minLenght || name.length() > maxLenght))
			return false;
		return true;
	}
}
