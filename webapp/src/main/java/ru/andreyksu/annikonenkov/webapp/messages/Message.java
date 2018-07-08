package ru.andreyksu.annikonenkov.webapp.messages;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Message implements IMessage {

	private final DataSource _dataSource;

	private static final Logger ___log = LogManager.getLogger(Message.class);

	private static final String _toInsertNewMessageForRecipient = "INSERT INTO messages(author, uuid, message) values (?, ?, ?)";

	private static final String _toSelectMessageFromRecipient = "SELECT messages.message, messages.messagedate FROM matrix_message_user AS matrix JOIN messages ON matrix.uuid_message = messages.uuid AND email_recipient = ?";
	// select messages.message, messages.messagedate from matrix_message_user as
	// matrix join messages on matrix.uuid_message = messages.uuid and
	// email_recipient = 'test1@yandex.ru';

	public Message(DataSource dataSource) {
		_dataSource = dataSource;
	}

	/**
	 * Вставляем новое сообщение для списка получателей {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	@Override
	public void newMessageToRecipient(String authorEmail, String message, String recipient) throws IOException {
		try (Connection connection = _dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(_toInsertNewMessageForRecipient)) {
			preparedStatement.setString(1, authorEmail);
			preparedStatement.setString(2, message);
			preparedStatement.setString(3, recipient);
			int countRow = preparedStatement.executeUpdate();
			___log.debug("В методе [newMessageToRecipient] удалось вставить в БД %d записей", countRow);
		} catch (SQLException e) {
			___log.error("Ошибка при вставке/записи в БД нового сообщения!", e);
			throw new IOException(e);
		}
	}

	/**
	 * Вставляем новое сообщение для комнаты. {@inheritDoc}
	 */
	@Override
	public void newMessageToRoom(String authorEmail, String message, String room) {
		___log.error("Method 'newMessageToRoom( ... )' не реализован.");
	}

	/**
	 * Получаем 30 новых сообщений. Используется после авторизации.
	 * {@inheritDoc}
	 */
	@Override
	public String getMessagesFromRecipient(String recipient) throws IOException {
		String result = null; 
		try(Connection connection = _dataSource.getConnection(); PreparedStatement preparedStatment = connection.prepareStatement(_toSelectMessageFromRecipient)){
			preparedStatment.setString(1, recipient);
			ResultSet resultSet = preparedStatment.executeQuery();
			result = resultSet.toString();
			___log.debug("Получили запись из базы!");
		}
		catch(SQLException e){
			___log.error("Ошибка при извлечении сообщения из базы.", e);
			throw new IOException(e);
		}
		return result;
	}

	/**
	 * Получаем сообщения за указанный срок (диапозон времени)
	 * <p>
	 * После себя закрывает {@link java.sql.PreparedStatement} и
	 * {@link java.sql.Connection}
	 * 
	 * @param email - Индификатор автора сообщения.
	 * @param startDate - Время начала диапозона для выбираемых сообщений.
	 * @param stopDate - Время окончания диапозона для выбираемых сообщений.
	 * @return {@code String[]} - Список сообщений. {@code null} - если
	 *         авторизация данный пользователь уже существует в системе или же
	 *         {@code email} не прошел валидацию.
	 * @throws SQLException
	 */
	@Override
	public String getMessagesFromRoom(String authorEmail, String room, String startDate, String stopDate) {
		___log.error("Method 'getMessagesFromRoom( ... )' не реализован.");
		return null;
	}

}
