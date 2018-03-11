package ru.andreyksu.annikonenkov.webapp.messages;

import java.sql.SQLException;
import java.util.Map;

public class Message implements IMessage {

	/**
	 * Вставляем новое сообщение для списка получателей {@inheritDoc}
	 */
	@Override
	public void newMessageToRecipient(String email, String message, String recipient) {}

	/**
	 * Вставляем новое сообщение для комнаты. {@inheritDoc}
	 */
	@Override
	public void newMessageToRoom(String email, String message, String room) {}

	/**
	 * Получаем 30 новых сообщений. Используется после авторизации.
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getMessagesFromRecipients(String email, String recipient, String startDate, String stopDate) {
		return null;
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
	public Map<String, String> getMessagesFromRoom(String email, String room, String startDate, String stopDate) {
		return null;
	}

}
