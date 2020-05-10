package ru.andreyksu.annikonenkov.webapp.email;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmailWorker {

    private static Logger _log = LogManager.getLogger(EmailWorker.class);

    Session _session = null;

    public EmailWorker(Session session) {
        _session = session;
    }

    public void sendEmail(String toSomeone) throws AddressException, MessagingException {
        Message message = new MimeMessage(_session);

        message.setFrom(new InternetAddress("yyy@yandex.ru"));

        String[] emails = {"yxy@gmail.com"};
        InternetAddress dests[] = new InternetAddress[emails.length];
        for (int i = 0; i < emails.length; i++) {
            dests[i] = new InternetAddress(emails[i].trim().toLowerCase());
        }

        message.setRecipients(Message.RecipientType.TO, dests);
        message.setSubject("Пробное письмо из Java APP Tomcat");

        Multipart mp = new MimeMultipart();
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText("Текст письма!\nЗдесь содержится полезная информация которую "
                + "можно получить из прочтения тела письма!\nЭто пробное письмо!", "KOI8-R");
        mp.addBodyPart(mbp1);
        message.setContent(mp);
        message.setSentDate(new java.util.Date());
        Transport.send(message);
    }

    /**
     * Основной метод для работы с получением почты.
     */

    public void getMessages(String fromSomeone) throws MessagingException {

        _log.debug("Пытаемся получить сообщения");
        Store store = _session.getStore("imap");
        store.connect();
        // --------------------------------------------------------------
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        int messageCount = inbox.getMessageCount();

        _log.info("Количество сообщений в почовом ящике messageCount = {}", messageCount);

        Message[] messages = inbox.getMessages();
        for (Message message : messages) {
            _log.info("Mail Тема/Subject: {} для номера сообщения = {}", message.getSubject(),
                    message.getMessageNumber());
        }
        inbox.close(true);
        store.close();
    }

    /**
     * Побочный метод. Был создан так как инзначально не получалось для imap
     * выполнить store.connect() через JNDI. Пока оставлю - не буду удалять.
     * Удалю чуть позже.
     */

    public void getMessages() throws MessagingException {
        String host = "imap.yandex.ru";
        String username = "yyy@yandex.ru";
        String password = "yyy";
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.starttls.enable", "true");
        props.setProperty("mail.imap.ssl.enable", "true");
        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.port", "993");
        props.setProperty("mail.imap.socketFactory.port", "993");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imap");
        store.connect(host, username, password);

        // --------------------------------------------------------------

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        int messageCount = inbox.getMessageCount();
        _log.debug("Количество сообщений messageCount = {}", messageCount);

        // printAllTopLevelFolders(store);

        Message[] messages = inbox.getMessages();
        for (Message message : messages) {
            _log.debug("---------------------------Начало сообщения сообщения-----------------------------");
            // printHeadersFromMessage(message);
            _log.debug(
                    "\n Сообщение номер = {},\n message.getSubject() = {},\n message.getContentType() = {},\n"
                            + " message.getFileName() = {},\n message.getDescription() = {},\n"
                            + " message.getDisposition() = {}",
                    message.getMessageNumber(), message.getSubject(), message.getContentType(), message.getFileName(),
                    message.getDescription(), message.getDisposition());
            try {
                _log.debug("Пытаемся получить тип Content для сообщения");
                Object objectFromGetContent = message.getContent();
                _log.debug("message.getContent().getClass().getCanonicalName() = {}",
                        objectFromGetContent.getClass().getName());
                if (objectFromGetContent instanceof MimeMultipart) {
                    MimeMultipart mimeMultipart = (MimeMultipart) objectFromGetContent;
                    printInfoAboutMimeMultipart(mimeMultipart);
                } else if (objectFromGetContent instanceof String) {
                    String str = (String) objectFromGetContent;
                    _log.debug("Строка полученная из сообщения на вернхем уровне =\n {}", str);
                } else {
                    _log.debug("Для сообщения получили не верхнему уровне ни MimeMultipart ни String");
                }
            } catch (Exception e) {
                _log.error("В ходе работы с почтной возникал ошибка!!!!!");
                _log.catching(e);
            }
            _log.debug("---------------------------Окончание сообщения-----------------------------");
        }
        inbox.close(true);
        store.close();
    }

    private void printHeadersFromMessage(Message message) throws MessagingException {
        Enumeration<Header> e = message.getAllHeaders();
        while (e.hasMoreElements()) {
            Header header = e.nextElement();
            _log.debug("HEADERS: {} = {}", header.getName(), header.getValue());
        }

    }

    private void printAllTopLevelFolders(Store store) throws MessagingException {
        javax.mail.Folder[] folders = store.getDefaultFolder().list("*");
        for (Folder folder : folders) {
            _log.debug(
                    "Каталог\n folder.getType() = {},\n folder.getName() = {},\n folder.getFullName() = {},\n"
                            + " folder.getSeparator() = {}",
                    folder.getType(), folder.getName(), folder.getFullName(), folder.getSeparator());
        }

    }

    private void methodForPrintTheInfoAboutPart(BodyPart part) {

        try {
            Multipart mp = (Multipart) part.getContent();
            int countOfSubPart = mp.getCount();
            _log.debug("Имеем частей = '{}' для part.getContentType() = {}", countOfSubPart, part.getContentType());

            if (countOfSubPart == 0) {
                _log.debug("Для полученного Part нет Multipart частей. Выходим.");
                return;
            }

            for (int ii = 0; ii < countOfSubPart; ii++) {
                BodyPart bp = mp.getBodyPart(ii);
                _log.debug("Для части = {} получаем тип getContent() = ", ii, bp.getContent().getClass().getName());
                if (bp.isMimeType("text/plain") || bp.isMimeType("text/html")) {
                    String str = (String) bp.getContent();
                    _log.debug("Попали в часть, что является 'text/plain' или 'text/html' str = \n {}", str);
                } else if (bp.isMimeType("multipart/alternative")) {
                    _log.debug(
                            "Провалились в часть 'multipart/alternative' - вероятно из за родителя 'multipart/related'");
                    methodForPrintTheInfoAboutPart(bp);
                    // Не лучшая идея выводить содержмое. Чистый html + css
                } else {
                    _log.debug("Какой-то отличный от 'text' 'multipart/related' тип - а именно = {}",
                            bp.getContentType());
                }
            }
        } catch (IOException | MessagingException e) {
            _log.error("Ошибка при попытке распечатать информацию о полученном сообщении");
            _log.catching(e);
        }

    }

    private void printInfoAboutMimeMultipart(MimeMultipart mimeMultipart)
            throws MessagingException, UnsupportedEncodingException {

        int countOfParts = mimeMultipart.getCount();
        _log.debug("Количество частей в полученном сообщении. mm.getCount() = {}", countOfParts);

        for (int i = 0; i < countOfParts; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            _log.debug("\n Часть номер = {},\n bodyPart.getContentType() = {},\n bodyPart.getDescription() = {},\n "
                    + "bodyPart.getDisposition() = {},\n bodyPart.getFileName() = {},\n bodyPart.getLineCount() = {}",
                    i, bodyPart.getContentType(), bodyPart.getDescription(), bodyPart.getDisposition(),
                    bodyPart.getFileName(), bodyPart.getLineCount());

            if (bodyPart.isMimeType("multipart/related")) {
                _log.debug("Попали в секцию для ---- 'multipart/related'");
                methodForPrintTheInfoAboutPart(bodyPart);

            } else if (bodyPart.isMimeType("multipart/alternative")) {
                _log.debug("Попали в секцию для ---- 'multipart/alternative'");
                methodForPrintTheInfoAboutPart(bodyPart);

            } else if (bodyPart.getDisposition() != null && bodyPart.getDisposition().equals("attachment")) {
                String rawNameOfAttachment = bodyPart.getFileName();
                String plainFileName = MimeUtility.decodeText(rawNameOfAttachment);
                _log.debug("planefileName = {}, rawNameOfAttachment = {} ", plainFileName, rawNameOfAttachment);
                _log.debug("Попытаемся получить bodyPart.getInputStream()");
                
                // TODO: Код для сохранения файлов. Пока сохраняет в /tmp
                // String fullPathName = "/tmp/" + plainFileName;
                // File file = null;
                // FileOutputStream fos = null;
                // BufferedOutputStream bfos = null;
                // try (InputStream is = bodyPart.getInputStream()) {
                // file = new File(fullPathName);
                // if (!file.exists()) {
                // file.createNewFile();
                // }
                //
                // fos = new FileOutputStream(file);
                // bfos = new BufferedOutputStream(fos);
                //
                // byte[] buffer = new byte[1024];
                // while (is.read(buffer) != -1) {
                // bfos.write(buffer);
                // }
                // bfos.flush();
                // _log.debug("Вроде что то смогли записать из приложения");
                // } catch (IOException | SecurityException e) {
                // _log.error("Возникла ошибка при попытке получить/записать
                // содержимое приложений");
                // _log.catching(e);
                // } finally {
                // try {
                // if (bfos != null)
                // bfos.close();
                // } catch (IOException ee) {
                // _log.debug("Ошибка при попытке закрыть
                // BufferedOutputStream");
                // _log.catching(ee);
                // }
                // }

            } else {
                _log.warn("Не подошли ни под одно условие ни multipart/related ни multipart/alternative ни attachment");
            }
        }
    }

}
