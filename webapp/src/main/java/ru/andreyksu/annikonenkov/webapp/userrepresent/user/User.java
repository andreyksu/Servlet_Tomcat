package ru.andreyksu.annikonenkov.webapp.userrepresent.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class User implements IUser {

    private static final Logger _log = LogManager.getLogger(User.class);

    private ResultSet _resultOfQueryFromDataBase = null;

    private String _email, _password, _name, _regdata;

    private boolean _isactive, _isadmin;

    @Override
    public boolean fillUserFromRequest(ResultSet result) throws SQLException {
        _resultOfQueryFromDataBase = result;
        if (_resultOfQueryFromDataBase == null || !setResultOfQueryExistUserInMap(result)) {
            _log.error("Передан пустой ResultSet или кол найденных пользователей в базе равно 0. Пользователь сформирован не будет. Возвращаем false");
            return false;
        }
        return true;
    }

    @Override
    public boolean isAdmin() {
        return _isadmin;
    }

    @Override
    public boolean isActive() {
        return _isactive;
    }

    @Override
    public String getPassword() {
        return _password;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getEmail() {
        return _email;
    }

    @Override
    public String getRegistrateData() {
        return _regdata;
    }

    public static boolean isValidFieldsForCreateUser(String email, String password, String name) {
        _log.debug("Проверяем валидность всех полей: email = {}, password = {}, name = {} пользователя!", email, password, name);
        return (baseValid(email) && baseValid(password) && baseValid(name));
    }

    public static boolean isValidEmail(String email) {
        _log.debug("Проверяем валидность email пользователя!");
        boolean valid = baseValid(email);        
        if (!valid)
            _log.error("Email пользователя НЕ валиден!");
        return valid;
    }

    private static boolean baseValid(String parameter) {
        int minLenght = 7;
        int maxLenght = 100;
        boolean tmp = (parameter != null && parameter.length() >= minLenght && parameter.length() <= maxLenght);
        if (!tmp) {
            _log.error("Параметр {} НЕ прошел проверку!", parameter);
        }else {
        	_log.debug("Параметр {} прошел проверку!", parameter);
        }
        return tmp;
    }

    private boolean setResultOfQueryExistUserInMap(ResultSet result) throws SQLException {
        _log.debug("Разбираем полученный результат запроса по пользователю из БД!");
        int count = 0;
        while (result.next()) {// Почему-то не подходит result.first()
            _email = result.getString("email").trim();
            _password = result.getString("password").trim();
            _name = result.getString("name").trim();
            _regdata = result.getTimestamp("regdata").toString().trim();
            _isactive = result.getBoolean("isactive");
            _isadmin = result.getBoolean("isadmin");
            count++;
        }
        _log.debug("Количество найденных в БД пользователей count = {}", count);
        _log.debug(
                "По результатам запроса в БД сформирован следующий пользователь _email = {}, _password = {}, _name = {}, _regdata = {}, _isactive = {},  _isadmin={} ",
                _email, _password, _name, _regdata, _isactive, _isadmin);

        return count != 0 ? true : false;
    }
}
