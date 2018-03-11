package ru.andreyksu.annikonenkov.webapp.authorization;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.logging.log4j.Logger;

import ru.andreyksu.annikonenkov.webapp.userrepresent.User;

public class Authorization implements IAuthorization {

	private final Logger _log;

	private final DataSource _dataSource;

	private final static Set<String> _setOfAuthUser = Collections.synchronizedSet(new HashSet<String>());

	public Authorization(Logger log, DataSource dataSource) {
		_log = log;
		_dataSource = dataSource;
	}

	@Override
	public boolean authorizedInSystem(String email, String password) throws IOException {
		_log.debug("Проверяем, авторизован ли пользователь. Метод authorizedInSystem(String, String)");
		if (isAuthorizedUser(email))
			return true;
		_log.debug("Создаем инстанс User в методе authorizedInSystem!");
		User user = new User(email, _log, _dataSource);
		if (user.getPassword() != null && user.getPassword().equals(password) && user.isUserActive()) {
			_setOfAuthUser.add(email);
			return true;
		}
		return false;
	}

	@Override
	public boolean isAuthorizedUser(String email) {
		_log.debug("Проверяем в _setOfAuthUser наличие авторизованного пользователя!");
		if (_setOfAuthUser.contains(email))
			return true;
		return false;
	}

	@Override
	public void unAuthorizedUser(String email) {
		_log.debug("Удаляем из _setOfAuthUser пользователя в результате разАвторизации!");
		_setOfAuthUser.remove(email);
	}

	@Override
	public boolean registrate(String email, String password, String name) throws IOException {
		_log.debug("Создаем инстанс User в методе registrate!");
		User user = new User(email, _log, _dataSource);
		return user.registrateUser(password, name);
	}

}
