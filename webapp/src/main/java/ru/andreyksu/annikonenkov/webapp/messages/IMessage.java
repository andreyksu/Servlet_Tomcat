package ru.andreyksu.annikonenkov.webapp.messages;

import java.util.Map;

public interface IMessage {

	public void newMessageToRecipient(String email, String message, String recipient);

	public void newMessageToRoom(String email, String message, String room);

	public Map<String, String> getMessagesFromRecipients(String email, String recipient, String startDate, String stopDate);

	public Map<String, String> getMessagesFromRoom(String email, String room, String startDate, String stopDate);

}
