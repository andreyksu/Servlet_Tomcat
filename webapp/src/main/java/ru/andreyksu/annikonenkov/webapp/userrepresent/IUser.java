package ru.andreyksu.annikonenkov.webapp.userrepresent;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IUser {

	/**
	 * Предварительно проверяет, есть ли пользователь в системе. Поиск ведется
	 * по email. Если нашли такой {@code email}, то проверяем пароль по
	 * равенству строк, что находится в базе и что передали в метод.
	 * <p>
	 * Если пользватель был авторизован, запись добавляется в
	 * {@link java.util.Set}, а именно в
	 * {@link java.util.Collections.SynchronizedSet}
	 * 
	 * @param email - Email пользователя, он же и выступает как логин.
	 * @param password - Пароль пользователя.
	 * @return {@code true} - если удалось авторизовать и такой пользователь был
	 *         найден. {@code false} - в ином случае (не был найден
	 *         пользователь, не совпал пароль)
	 */
	boolean authorizedInSystem(String email, String password);

	/**
	 * Метод проверяет авторизацию во внутреннем статическом поле -
	 * потоко-безопасном Set. При процедуре авторизации, в данный Set заносится
	 * запись, при выходе происходит удаление из Set азписи
	 * 
	 * @param email - Email пользователя, он же и выступает как логин.
	 * @return {@code true} - если Если запись в Set присутствует. {@code false}
	 *         - если запись не найдена. В таком случае предварительно терубется
	 *         авторизация.
	 */
	boolean isAuthorizedUser(String email);

	/**
	 * Метод удаляет запись из внутреннего Set - который представляет из себя
	 * множество авторизованных пользвателей.
	 * 
	 * @param email - Email пользователя, он же и выступает как логин.
	 */
	void unAuthorizedUser(String email);

	/**
	 * Производится регистрация пользователя в системе, путем добавления записи
	 * в БД. Предварительно проверяется есть ли данный пользователь в системе.
	 * <p>
	 * После себя закрывает {@link java.sql.PreparedStatement} и
	 * {@link java.sql.Connection}
	 * 
	 * @param email - Email пользователя, он же и выступает как логин.
	 * @param password - Пароль пользователя
	 * @param name - Имя пользователя.
	 * @return {@code true} - если регистрация прошла успешно. {@code false} -
	 *         если авторизация данный пользователь уже существует в системе или
	 *         же не {@code email} не прошел валидацию
	 */
	boolean registrateUser(String email, String password, String name);

	/**
	 * Проверяет, есть ли пользователь в системе. Проверка ведется по
	 * {@code email}.
	 * <p>
	 * После себя закрывает {@link java.sql.PreparedStatement} и
	 * {@link java.sql.Connection}
	 * 
	 * @return Возвращает {@link java.sql.ResultSet} если пользователь найден в БД. Если по
	 *         {@code email} не прошли валидацию, или если кол. записей в БД для данного email = 0,
	 *         то возвращаем {@code null}
	 * @throws SQLException
	 */
	boolean isExistUserInSystem(String email);
}
