package ru.andreyksu.annikonenkov.webapp.userrepresent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.andreyksu.annikonenkov.webapp.userrepresent.user.IUser;
import ru.andreyksu.annikonenkov.webapp.userrepresent.user.User;

public class WorkerWithUser implements IWorkerWithUser {

    private static Logger _log = LogManager.getLogger(WorkerWithUser.class);

    private final DataSource _dataSourcel;

    private final Map<String, String> resultOfCheckExistUser = new HashMap<String, String>();

    private static final String _forIsExistUserInSystem = "SELECT * FROM principal WHERE email=?";

    private static final String _forUserList = "SELECT email FROM principal WHERE isactive = ?";

    private static final String _forRegistrateUser = "INSERT INTO principal (email, password, name) VALUES (?, ?, ?)";

    private static final String _forSetUserActive = "UPDATE principal SET isactive = ? WHERE email=?";

    private static final String _forGrandUserAdminRole = "UPDATE principal SET isadmin = ? WHERE email=?";

    /**
     * Конструирует объект представляющий возможность работать с пользователем.
     * 
     * @param dataSource
     */
    public WorkerWithUser(DataSource dataSource) {
        _dataSourcel = dataSource;
    }

    /**
     * Проверяет, есть ли пользователь в системе. Проверка ведется по
     * {@code email}.
     * <p>
     * После себя закрывает {@link java.sql.PreparedStatement} и
     * {@link java.sql.Connection}
     * 
     * @return Возвращает {@code true} если пользователь найден в БД. В ином
     *         случае {@code false}.
     * @throws SQLException
     */
    @Override
    public IUser getUserByEmail(String email) throws IOException {
        _log.debug("Выполняем проверку наличия пользовтеля в БД");
        if (!User.isValidEmail(email))
            return null;
        try (Connection connection = _dataSourcel.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(_forIsExistUserInSystem)) {
            preparedStatement.setString(1, email);
            ResultSet result = preparedStatement.executeQuery();
            IUser user = new User();
            return user.fillUserFromRequest(result) ? user : null;
        } catch (SQLException e) {
            _log.error("Ошибка при проверке наличия пользователя в БД. Ошибка SQLException.", e);
            throw new IOException(e);
        }
    }

    /**
     * Возвращает список всех пользователей (активных или неактивных)
     * <p>
     * 
     * @param activity - <b>true</b> - если хотим получить активных
     *            пользователей, <b>false</b> - если неактивных
     */

    @Override
    public List<String> getListOfUsers(boolean activity) throws IOException {
        _log.debug("Возвращаем список пользователей");
        List<String> innerList = new ArrayList<>();
        try (Connection connection = _dataSourcel.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(_forUserList)) {
            preparedStatement.setBoolean(1, activity);// Интересно, что нужно
                                                      // именно setBoolean и
                                                      // setString не подходит.
            ResultSet result = preparedStatement.executeQuery();
            String tmp = null;
            while (result.next()) {
                tmp = result.getString("email").trim().replaceAll("\\s+", "");
                innerList.add(tmp);
            }
        } catch (SQLException e) {
            _log.error("Ошибка при проверке наличия пользователя в системе. Ошибка SQLException.", e);
            throw new IOException(e);
        }
        return innerList;
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
    public IUser registrateUser(String email, String password, String name) throws IOException {
        _log.debug("Начинаем регистрировать пользователя!!!");
        if (!User.isValidFieldsForCreateUser(email, password, name) || getUserByEmail(email) != null) {
            _log.error("Поля пользователя не прошли проверку, или же пользователь уже есть в системе");
            return null;// Возвращаем null для создаваемого пользователя, так
                        // как он создан не будет.
        }
        try (Connection connection = _dataSourcel.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(_forRegistrateUser)) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, name);
            preparedStatement.executeUpdate();
            _log.debug("Запись успешно добавлена");
        } catch (SQLException e) {
            _log.error("Ошибка при добавлениии пользователя", e);
            throw new IOException(e);
        }
        return getUserByEmail(email);// После вставки еще раз получаем
                                     // пользователя и возвращаем его.
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
    public boolean setUserActive(String email, boolean stat) throws IOException {
        String status = stat ? "true" : "false";
        _log.debug("Устанавливаем пользователю статус Active = {}!!!", status);
        if (resultOfCheckExistUser.get("isactive") == null) {
            if (getUserByEmail(email) == null) {
                return false;
            }
        }
        try (Connection connection = _dataSourcel.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(_forSetUserActive)) {
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, email);
            preparedStatement.executeUpdate();
            _log.debug("Запись '{}' успешно обновлена", email);
        } catch (SQLException e) {
            _log.error("Ошибка при изменении записи пользователя Поле 'isactive' .", e);
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
     * @param stat - true - если даем права Администратора <br>
     *            false - если изымаем права Администратора.
     * @return {@code true} - если предоставление/изъятие прошло успешно.. <br>
     *         {@code false} - если данного пользователя нет в системе, или если
     *         же пользовтеля не прошел валидацию по <b>email</b>.
     * @throws SQLException
     */
    @Override
    public boolean grandUserAdminRole(String email, boolean admin) throws IOException {
        _log.debug("Устанавливаем/изымаем права Администратора!!!");
        String status = admin ? "true" : "false";
        if (resultOfCheckExistUser.get("isadmin") == null) {
            if (getUserByEmail(email) == null) {
                return false;
            }
        }
        try (Connection connection = _dataSourcel.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(_forGrandUserAdminRole)) {
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, email);
            int count = preparedStatement.executeUpdate();
            _log.debug(String.format(
                    "Удалось изменить =%4d записей, при награждении пользователя правадми Администратора", count));
        } catch (SQLException e) {
            _log.error("Ошибка при изменении записи пользователя Поле 'isadmin'.", e);
            throw new IOException(e);
        }
        return true;
    }
}
