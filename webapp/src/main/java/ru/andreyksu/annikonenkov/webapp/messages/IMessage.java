package ru.andreyksu.annikonenkov.webapp.messages;

import java.io.IOException;
import java.io.InputStream;

public interface IMessage {

    public void newMessageToRecipient(String authorEmail, String message,
            String recipientEmail, long timeMillisecParamAsLong, boolean isFile)
            throws IOException;

    public void newMessageToRoom(String authorEmail, String message,
            String room);

    public String getMessagesFromRecipient(String authorEmail, String recipient,
            long aDate, String aTypeMessgaes) throws IOException;

    public String getMessagesFromRoom(String authorEmail, String room,
            String startDate, String stopDate);
    
    public byte[] getFileFromDB(String uuid) throws IOException;

    public String newMessageToRecipientAsFile(String authorEmail,
            String fileName, String recipientEmail,
            long timeMillisecParamAsLong, InputStream is, long sizeOfFile)
            throws IOException;
}
