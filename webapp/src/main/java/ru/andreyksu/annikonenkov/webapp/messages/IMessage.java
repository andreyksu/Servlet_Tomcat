package ru.andreyksu.annikonenkov.webapp.messages;

import java.io.IOException;

public interface IMessage {

	public void newMessageToRecipient(String authorEmail, String message, String recipient) throws IOException;

	public void newMessageToRoom(String authorEmail, String message, String room);

//	public String getMessagesFromRecipient(String authorEmail, String recipient, String startDate, String stopDate) throws IOException;
	public String getMessagesFromRecipient(String recipient) throws IOException;

	public String getMessagesFromRoom(String authorEmail, String room, String startDate, String stopDate);

}
