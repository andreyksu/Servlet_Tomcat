package ru.andreyksu.annikonenkov.webapp.authorization;

import java.io.IOException;

public interface IAuthorization {

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
	boolean authorizedInSystem(String email, String password) throws IOException;

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

	boolean registrate(String email, String password, String name) throws IOException;

}
