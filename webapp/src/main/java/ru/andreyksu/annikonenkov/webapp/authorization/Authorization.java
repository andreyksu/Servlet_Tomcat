package ru.andreyksu.annikonenkov.webapp.authorization;

import java.io.IOException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.logging.log4j.Logger;

import ru.andreyksu.annikonenkov.webapp.userrepresent.IUser;
import ru.andreyksu.annikonenkov.webapp.userrepresent.User;

public class Authorization implements IAuthorization {

	private final Logger ___log;

	private final DataSource _dataSource;

	private final Map<String, String> _mapOfAuthUser;

	public Authorization(Logger log, DataSource dataSource, Map<String, String> mapOfAuthUser) {
		___log = log;
		_dataSource = dataSource;
		_mapOfAuthUser = mapOfAuthUser;
	}

	/**
	 * Предварительно проверяет, есть ли пользователь в системе. Поиск ведется
	 * по email. Если нашли такой {@code email}, то проверяем пароль по
	 * равенству строк, что вытащили из базы с тем что передали в метод.
	 * <p>
	 * Если пользватель был успешно авторизован, то запись добавляется в
	 * {@link java.util.Map}. Реализация которого выполнане в виде
	 * {@link java.util.Collections.SynchronizedMap}
	 * 
	 * @param email - Email пользователя, он же и выступает как логин.
	 * @param password - Пароль пользователя.
	 * @return {@code true} - если удалось авторизовать и такой пользователь был
	 *         найден. {@code false} - в ином случае (не был найден
	 *         пользователь, не совпал пароль)
	 */
	@Override
	public boolean authorizedInSystem(String email, String password) throws IOException {
		___log.debug("Проверяем, авторизован ли пользователь. Метод authorizedInSystem(String, String)");
		if (isAuthorizedUser(email, password)) {
			___log.debug("Пользователь есть в локальном кэше, проверка проводилась по email и password");
			return true;
		}
		___log.debug("Пользователь не прошел проверку по email и password в локальном кэше, будем смотреть в БД");
		IUser user = new User(email, ___log, _dataSource);
		String passwordOfUser = null;
		if (user.isExistUserInSystem()) {
			passwordOfUser = user.getPassword();
		}
		if (passwordOfUser != null && passwordOfUser.equals(password) && user.isUserActive()) {
			___log.debug("Пользователь по БД прошел проверку. Помещаем в локальный кэш");
			_mapOfAuthUser.put(email, password);
			return true;
		}
		___log.debug("Пользователь ни по БД ни в локальном кэше не прошел проверку");
		return false;
	}

	/**
	 * Метод проверяет авторизован ли пользователь. Проверка ведется во
	 * внутреннем статическом поле - потоко-безопасном {@code Map}.
	 * <p>
	 * При выполнении процедуры авторизации, в данный {@code Map} добавляется
	 * запись, при выходе происходит удаление из {@code Map} соответствующей
	 * записи.
	 * <p>
	 * Применяется при обмене сообщениями. Быстрая проверка при каждой
	 * отправки/приеме сообщений.
	 * 
	 * @param email - Email пользователя, он же и выступает как логин.
	 * @return {@code true} - если Если запись в Map присутствует. {@code false}
	 *         - если запись не найдена. В таком случае предварительно терубется
	 *         авторизация.
	 */

	@Override
	public boolean isAuthorizedUser(String email) {
		___log.debug("Проверяем в локальном кэше наличие авторизованного пользователя. Проверка только по логину!");
		if (_mapOfAuthUser.containsKey(email)) {
			___log.debug("Логин {} есть в локальном кэше", email);
			return true;
		}
		return false;
	}

	/**
	 * Метод проверяет авторизацию во внутреннем статическом поле -
	 * потоко-безопасном {@code Map}. При процедуре авторизации
	 * {@link #authorizedInSystem(String, String)}, в данный {@code Map}
	 * заносится запись, при выходе происходит удаление из {@code Map} записи.
	 * <p>
	 * Применяется при переходе со страницы <b>авторизации</b> В частности
	 * пользователь был авторизован, закрыл страницу, перешл на основную
	 * страинцу приложения, тем самым попав на страницу авторизации. Дабы не
	 * идти снова в БД мы проверяем данногопользователя во внутреннем
	 * {@code Map}.
	 * 
	 * @param email - Email пользователя, он же и выступает как логин.
	 * @param password - Password пользователя.
	 * @return {@code true} - если Если запись в Map присутствует. {@code false}
	 *         - если запись не найдена. В таком случае предварительно терубется
	 *         авторизация.
	 * @see IAuthorization#isAuthorizedUser(String, String)
	 */
	@Override
	public boolean isAuthorizedUser(String email, String password) {
		___log.debug("Проверяем в локальном кэше наличие авторизованного пользователя. Проверка по логину и паролю!");
		String passOfAuthUser = _mapOfAuthUser.get(email);
		if (passOfAuthUser != null && passOfAuthUser.equals(password)) {
			___log.debug("Полученный пароль = %s И извлеченный пароль = %s Для логина = %s", password, passOfAuthUser, email);
			return true;
		}
		return false;
	}

	/**
	 * Метод удаляет запись из внутреннего {@code Map} - который представляет из
	 * себя коллекцию авторизованных пользвателей.
	 * 
	 * @param email - Email пользователя, он же и выступает как логин.
	 */
	@Override
	public void unAuthorizedUser(String email) {
		___log.debug("Удаляем из локального кэша авторизованного пользователя. Видимо разлогинился!");
		_mapOfAuthUser.remove(email);
	}

	/**
	 * Регистрирует (добавляет пользователя в БД). При этом во вунтреннюю
	 * {@code Map} запись не добавляется в данном методе. Авторизовывать нужно
	 * отдельно
	 * 
	 * @param email - Email пользователя, он же и выступает как логин.
	 * @param password - Password пользователя.
	 * @param name - Name имя пользователя.
	 */
	@Override
	public boolean registrate(String email, String password, String name) throws IOException {
		IUser user = new User(email, ___log, _dataSource);
		return user.registrateUser(password, name);
	}

}
