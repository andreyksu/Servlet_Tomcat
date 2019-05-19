package ru.andreyksu.annikonenkov.webapp.postgressql;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: Перевел все в static - так как TomCat сообщал о на нличии leaks. jmap -hist - после выгрузки приложения отображало о наличии в памяти данного класса.
public class DataSourceProvider {

    private static final Logger _log = LogManager.getLogger(DataSourceProvider.class);

    private final static Map<String, String> _mapOfAuthUser = Collections.synchronizedMap(new HashMap<String, String>());

    private static DataSource _dataSource = null;

    /**
     * @return DataSource - возвращает DataSource полученный через JNDI (java:/comp/env/ нода в JNDI дереве где ищутся свойства).
     * @throws SQLException
     * @throws NamingException
     */

    private static DataSource getInnerDataSource() throws SQLException, NamingException {
        _log.info("Фомируем InitialContext в методе getInnerDataSource()");
        InitialContext initalContext = new InitialContext();
        _log.info("Выполняем lookup JNDI для базы данных");
        // Context envContext = (Context) initalContext.lookup("java:/comp/env");
        // DataSource ds = (DataSource) envContext.lookup("jdbc/postgres");
        return (DataSource) initalContext.lookup("java:/comp/env/jdbc/postgres");

    }

    /**
     * Потоко-безопасный метод, для получения DataSource - коим является PoolConnection к БД
     * 
     * @return DataSource -
     * @throws SQLException
     * @throws NamingException
     */
    public static DataSource getDataSource() throws SQLException, NamingException {
        if (_dataSource == null) {
            _dataSource = getInnerDataSource();
            // TODO: Под вопросом, нужно ли это. Вероятно lookup не такой долгий (возможно
            // где-то он кэшируется внутри TomCat)
            _log.info("Возвращаем DataSource из класса SQLConnection - первая инициализация");
            return _dataSource;
        } else {
            _log.info("Возвращаем DataSource из класса SQLConnection - уже проинициализирвоали ранее");
            return _dataSource;
        }
    }

    public static Map<String, String> getMapOfAuthorizedUser() {
        return _mapOfAuthUser;
    }
}
