package ru.andreyksu.annikonenkov.webapp.messages;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Message implements IMessage {

    private final DataSource _dataSource;

    private static final Logger _log = LogManager.getLogger(Message.class);

    private static final String _forInsertNewMessage = "INSERT INTO messages(author, uuid, message, messagedate) values (?, ?, ?, ?)";

    private static final String _forInsertRecipient = "INSERT INTO matrix_message_user (email_recipient, uuid_message) values (?, ?)";

    private static final String _forSelectMessageToPairInterlocutorAsHistory =
            "SELECT messages.author, matrix.email_recipient, messages.message, messages.messagedate FROM matrix_message_user AS matrix JOIN messages ON matrix.uuid_message = messages.uuid WHERE ((matrix.email_recipient = ? AND messages.author = ?) OR (matrix.email_recipient = ? AND messages.author = ?)) and messages.messagedate < ? ORDER BY messages.messagedate ASC LIMIT 50";

    private static final String _forSelectMessageToPairInterlocutorAsNew =
            "SELECT messages.author, matrix.email_recipient, messages.message, messages.messagedate FROM matrix_message_user AS matrix JOIN messages ON matrix.uuid_message = messages.uuid WHERE ((matrix.email_recipient = ? AND messages.author = ?) OR (matrix.email_recipient = ? AND messages.author = ?)) and messages.messagedate > ? ORDER BY messages.messagedate ASC";

    public Message(DataSource dataSource) {
        _dataSource = dataSource;
    }

    /**
     * Вставляем новое сообщение для списка получателей {@inheritDoc}
     * 
     * @throws IOException
     */
    @Override
    public void newMessageToRecipient(String authorEmail, String message, String recipientEmail, long timeMillisecParamAsLong) throws IOException {
        UUID uuid = UUID.randomUUID();
        String uuidAsSring = uuid.toString();
        
        java.sql.Timestamp timeStamp = new java.sql.Timestamp(timeMillisecParamAsLong);
        _log.debug("TimeAsLong = {}", timeMillisecParamAsLong);
        _log.debug("TimeStamp = {}", timeStamp.toString());        
        
        try (Connection connection = _dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(_forInsertNewMessage)) {
            preparedStatement.setString(1, authorEmail);
            preparedStatement.setString(2, uuidAsSring);
            preparedStatement.setString(3, message);
            preparedStatement.setTimestamp(4, timeStamp);
            int countRow = preparedStatement.executeUpdate();
            _log.debug("В методе 'newMessageToRecipient' удалось вставить в БД {} сообщений", countRow);
        } catch (SQLException e) {
            _log.error("Ошибка при вставке/записи в БД нового сообщения!", e);
            throw new IOException(e);
        }

        try (Connection connection = _dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(_forInsertRecipient)) {
            preparedStatement.setString(1, recipientEmail);
            preparedStatement.setString(2, uuidAsSring);
            int countRow = preparedStatement.executeUpdate();
            _log.debug("В методе [newMessageToRecipient] удалось вставить в БД {} сообщений", countRow);

        } catch (SQLException e) {
            _log.error("Ошибка при вставке/записи в БД матрицу полученного сообщения!", e);
            throw new IOException(e);
        }
    }

    @Override
    public String getMessagesFromRecipient(String authorEmail, String recipient, long startDate, String aTypeMessgaes) throws IOException {
        String result = null;
        String aQuery = aTypeMessgaes.equals("new") ? _forSelectMessageToPairInterlocutorAsNew : _forSelectMessageToPairInterlocutorAsHistory;
        
        long startDateTmp = startDate;
        java.sql.Timestamp timeStamp = new java.sql.Timestamp(startDateTmp);
        _log.debug("TimeAsLong = {}", startDateTmp);
        _log.debug("TimeStamp = {}", timeStamp.toString());
        
        
        try (Connection connection = _dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(aQuery)) {

            preparedStatement.setString(1, authorEmail);
            preparedStatement.setString(2, recipient);
            preparedStatement.setString(3, recipient);
            preparedStatement.setString(4, authorEmail);
            preparedStatement.setTimestamp(5, timeStamp);

            ResultSet reSet = preparedStatement.executeQuery();
            result = toStringResultOfQueryMessage(reSet);

        } catch (SQLException e) {
            _log.error("Ошибка при извлечении сообщения из базы.", e);
            throw new IOException(e);
        }
        return result;
    }

    private String toStringResultOfQueryMessage(ResultSet resultSet) throws SQLException {
        String author = null, email_recipient = null, message = null;
        java.sql.Timestamp messagedate = null;
        JSONArray jsonArray = new JSONArray();
        while (resultSet.next()) {
            JSONObject jsonObject = new JSONObject();
            author = resultSet.getString("author").trim().replace("\\s+", "");
            email_recipient = resultSet.getString("email_recipient").trim().replace("\\s+", "");
            message = resultSet.getString("message");
            messagedate = resultSet.getTimestamp("messagedate");

            jsonObject.put("author", author);
            jsonObject.put("email_recipient", email_recipient);
            jsonObject.put("message", message);
            jsonObject.put("messagedate", messagedate.toString());
            jsonArray.add(jsonObject);

        }
        return jsonArray.toJSONString();
    }

    /**
     * Вставляем новое сообщение для комнаты. {@inheritDoc}
     */
    @Override
    public void newMessageToRoom(String authorEmail, String message, String room) {
        _log.error("Method 'newMessageToRoom( ... )' не реализован.");
    }

    /**
     * Получаем сообщения за указанный срок (диапозон времени)
     * <p>
     * После себя закрывает {@link java.sql.PreparedStatement} и {@link java.sql.Connection}
     * 
     * @param email - Индификатор автора сообщения.
     * @param startDate - Время начала диапозона для выбираемых сообщений.
     * @param stopDate - Время окончания диапозона для выбираемых сообщений.
     * @return {@code String[]} - Список сообщений. {@code null} - если авторизация данный пользователь уже существует в системе или же {@code email} не прошел
     *         валидацию.
     * @throws SQLException
     */
    @Override
    public String getMessagesFromRoom(String authorEmail, String room, String startDate, String stopDate) {
        _log.error("Method 'getMessagesFromRoom( ... )' не реализован.");
        return null;
    }
}
