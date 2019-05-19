package ru.andreyksu.annikonenkov.webapp.authorization;

import java.io.IOException;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.andreyksu.annikonenkov.webapp.userrepresent.IWorkerWithUser;
import ru.andreyksu.annikonenkov.webapp.userrepresent.WorkerWithUser;
import ru.andreyksu.annikonenkov.webapp.userrepresent.user.IUser;

public class Authorization implements IAuthorization {

    private final DataSource _dataSource;

    private final Map<String, String> _mapOfAuthorizedUser;

    private static final Logger _log = LogManager.getLogger(Authorization.class);

    public Authorization(DataSource dataSource, Map<String, String> mapOfAuthorizedUser) {
        _dataSource = dataSource;
        _mapOfAuthorizedUser = mapOfAuthorizedUser;
    }

    /**
     * Предварительно проверяет, есть ли пользователь в системе. Поиск ведется по email. Если нашли такой {@code email}, то проверяем пароль по равенству строк,
     * что вытащили из базы с тем что передали в метод.
     * <p>
     * Если пользватель был успешно авторизован, то запись добавляется в {@link java.util.Map}. Реализация которого выполнане в виде
     * {@link java.util.Collections.SynchronizedMap}
     * 
     * @param email - Email пользователя, он же и выступает как логин.
     * @param password - Пароль пользователя.
     * @return {@code true} - если удалось авторизовать и такой пользователь был найден. {@code false} - в ином случае (не был найден пользователь, не совпал
     *         пароль)
     */
    @Override
    public boolean isAuthorizedUserInSystem(String email, String password) throws IOException {
        _log.debug("Проверяем, авторизован ли пользователь. Метод isAuthorizedUserInSystem(String, String)");
        if (isPresentUserInLocalMapAsAuthorized(email, password)) {
            _log.debug("Пользователь есть в локальном кэше Members, проверка проводилась по email и password");
            return true;
        }
        _log.debug("Пользователь не прошел проверку по email и password в локальном кэше, будем смотреть в БД");
        IWorkerWithUser workerWithUser = new WorkerWithUser(_dataSource);
        String passwordOfUser = null;
        IUser user = workerWithUser.getUserByEmail(email);
        if (user == null) {
            _log.debug("Похоже, что не удалось создать/вернуть инстанс пользователя.");
            return false;
        }
        passwordOfUser = user.getPassword();
        if (passwordOfUser != null && passwordOfUser.equals(password) && user.isActive()) {
            _log.debug("Пользователь по БД прошел проверку. Помещаем в локальный кэш");
            _mapOfAuthorizedUser.put(email, password);
            return true;
        }
        _log.debug("Пользователь ни по БД ни в локальном кэше не прошел проверку");
        return false;
    }

    /**
     * Метод проверяет авторизован ли пользователь. Проверка ведется во внутреннем статическом поле - потоко-безопасном {@code Map}.
     * <p>
     * При выполнении процедуры авторизации, в данный {@code Map} добавляется запись, при выходе происходит удаление из {@code Map} соответствующей записи.
     * <p>
     * Применяется при обмене сообщениями. Быстрая проверка при каждой отправки/приеме сообщений.
     * 
     * @param email - Email пользователя, он же и выступает как логин.
     * @return {@code true} - если Если запись в Map присутствует. {@code false} - если запись не найдена. В таком случае предварительно терубется авторизация.
     */

    @Override
    public boolean isPresentUserInLocalMapAsAuthorized(String email) {
        _log.debug("Проверяем в локальном кэше наличие авторизованного пользователя. Проверка только по логину!");
        if (_mapOfAuthorizedUser.containsKey(email)) {
            _log.debug("Логин {} есть в локальном кэше", email);
            return true;
        }
        return false;
    }

    /**
     * Метод проверяет авторизацию во внутреннем статическом поле - потоко-безопасном {@code Map}. При процедуре авторизации
     * {@link #isAuthorizedUserInSystem(String, String)}, в данный {@code Map} заносится запись, при выходе происходит удаление из {@code Map} записи.
     * <p>
     * Применяется при переходе со страницы <b>авторизации</b> В частности пользователь был авторизован, закрыл страницу, перешл на основную страинцу
     * приложения, тем самым попав на страницу авторизации. Дабы не идти снова в БД мы проверяем данногопользователя во внутреннем {@code Map}.
     * 
     * @param email - Email пользователя, он же и выступает как логин.
     * @param password - Password пользователя.
     * @return {@code true} - если Если запись в Map присутствует. {@code false} - если запись не найдена. В таком случае предварительно терубется авторизация.
     * @see IAuthorization#isPresentUserInLocalMapAsAuthorized(String, String)
     */
    @Override
    public boolean isPresentUserInLocalMapAsAuthorized(String email, String password) {
        _log.debug(String.format("Проверяем в локальном кэше наличие авторизованного пользователя. Проверка по логину и паролю!"));
        String passOfAuthUser = _mapOfAuthorizedUser.get(email);
        if (passOfAuthUser != null && passOfAuthUser.equals(password)) {
            _log.debug("Полученный пароль = {} И извлеченный пароль = {} Для логина = {}", password, passOfAuthUser, email);
            return true;
        }
        return false;
    }

    /**
     * Метод удаляет запись из внутреннего {@code Map} - который представляет из себя коллекцию авторизованных пользвателей.
     * 
     * @param email - Email пользователя, он же и выступает как логин.
     */
    @Override
    public void unAuthorizedUser(String email) {
        _log.debug("Удаляем из локального кэша авторизованного пользователя. Считаем, что разлогинился!");
        _mapOfAuthorizedUser.remove(email);
    }

    /**
     * Регистрирует (добавляет пользователя в БД). При этом во вунтреннюю {@code Map} запись не добавляется в данном методе. Авторизовывать нужно отдельно
     * 
     * @param email - Email пользователя, он же и выступает как логин.
     * @param password - Password пользователя.
     * @param name - Name имя пользователя.
     */
    @Override
    public boolean registrateUserInSystem(String email, String password, String name) throws IOException {
        IWorkerWithUser userWorker = new WorkerWithUser(_dataSource);
        IUser user = userWorker.registrateUser(email, password, name);
        return user != null ? true : false;
    }

}
