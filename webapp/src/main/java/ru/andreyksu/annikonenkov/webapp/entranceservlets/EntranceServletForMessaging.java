package ru.andreyksu.annikonenkov.webapp.entranceservlets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ru.andreyksu.annikonenkov.webapp.commonParameters.InterfaceRepresentUserFromRequest;
import ru.andreyksu.annikonenkov.webapp.messages.IMessage;
import ru.andreyksu.annikonenkov.webapp.messages.Message;
import ru.andreyksu.annikonenkov.webapp.postgressql.DataSourceProvider;
import ru.andreyksu.annikonenkov.webapp.wrappers.WrapperMutableHttpServletRequest;

@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 5, // 5 MB
        maxFileSize = 1024 * 1024 * 5, // 5 MB
        maxRequestSize = 1024 * 1024 * 10) // 10 MB
public class EntranceServletForMessaging extends HttpServlet {

    private class InfoAboutFile {

        private String _nameOfFile = null;

        private InputStream _is = null;

        private long _sizeOfFile = 0;

        public InfoAboutFile(String nameOfFile, InputStream is, long size) {
            _nameOfFile = nameOfFile;
            _is = is;
            _sizeOfFile = size;
        }

        public String getName() {
            return _nameOfFile;
        }

        public InputStream getInputeStream() {
            return _is;
        }

        public long getSize() {
            return _sizeOfFile;
        }

    }

    private static final long serialVersionUID = 4779791835788040403L;

    private static final Logger _log = LogManager.getLogger(EntranceServletForMessaging.class.getCanonicalName());

    private static DataSource _dataSource;

    private final String _loginMember = InterfaceRepresentUserFromRequest.Login;

    @Override
    public void init() throws ServletException {
        _log.debug("Init EntranceServletForMessaging");
        try {
            _dataSource = DataSourceProvider.getDataSource();
        } catch (SQLException | NamingException e) {
            _log.error("Произошла ошибка при создании/получении DataSource, для PoolConnection", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        _log.debug("------------------doGet------------------");

        long timeMillisecParamAsLong = 0;
        IMessage aMessageClass = new Message(_dataSource);

        WrapperMutableHttpServletRequest wrappedRequest = (WrapperMutableHttpServletRequest) request;
        String email = wrappedRequest.getHeader(_loginMember);

        // TODO:Подумать над созданием Map<String, String> - для требуемых значений и получать эти значени пробегая по элементам Map
        String directParam = wrappedRequest.getParameter("Direct");
        String loginMemberParam = wrappedRequest.getParameter("LoginMemeber");
        String interlocutorParam = wrappedRequest.getParameter("Interlocutor");
        String timeMillisecParam = wrappedRequest.getParameter("timeMillisec");

        String messageForAnswerFromSQLQuery = null;

        // TODO: Не нравится как это сделано, вероятно есть способ лучше. Может быть если перейду на Map - будет лучше и получится избежать. Внутри обхода просто сделать проверку на null.
        if (directParam == null || loginMemberParam == null || interlocutorParam == null || timeMillisecParam == null) {
            _log.error(String.format("The one or more parameters is Null or Empty directParam = %s loginMemberParam = %s interlocutorParam = %s timeMillisecParam = %s ", directParam, loginMemberParam, interlocutorParam,
                    timeMillisecParam));
            response.setStatus(400);
            return;
        }
        // ---------------------------------------------//
        try {
            timeMillisecParamAsLong = Long.parseLong(timeMillisecParam);
        } catch (NumberFormatException e) {
            _log.error("В полученном запросе не смогли распарсить миллисекунды как long - данные не будет записаны или прочитаны");
            _log.error(e);
            response.setStatus(400);
            return;
        }
        // ---------------------------------------------//
        if (directParam.equals("get")) {
            String historical = wrappedRequest.getParameter("historical");
            messageForAnswerFromSQLQuery = aMessageClass.getMessagesFromRecipient(loginMemberParam, interlocutorParam, timeMillisecParamAsLong, historical);

            _log.info("Для пользователя='{}' Выдернули сообщение = {}", email, messageForAnswerFromSQLQuery);
            _log.debug("Запишем полученное сообщение из БД в ответ т.е. в response");
            try (OutputStream outputStreamForResponse = response.getOutputStream(); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStreamForResponse))) {
                bw.write(messageForAnswerFromSQLQuery);
                bw.flush();
            } catch (IOException e) {
                _log.error("Во время записи в поток RESPONSE(поток ответа) возникла ошибка", e);
                throw e;
            }

        } else if (directParam.equals("send")) {
            String textMessageParam = wrappedRequest.getParameter("textMessage");
            aMessageClass.newMessageToRecipient(loginMemberParam, textMessageParam, interlocutorParam, timeMillisecParamAsLong, false);
        } else {
            _log.error("Unknow got Direct param");
            response.setStatus(400);
            return;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        _log.debug("------------------doPost------------------");

        WrapperMutableHttpServletRequest wrappedRequest = (WrapperMutableHttpServletRequest) request;

        List<InfoAboutFile> listOfFile = new ArrayList<>();
        long sizeOfFile = 0;
        String fileName = null;
        InputStream inputStream = null;

        String author = null;
        String recipient = null;
        long timeInMillisec = 0;

        for (Part part : request.getParts()) {

            _log.debug("content-disposition header = {}   part.getName() = {}  part.getContentType() = {}", part.getHeader("content-disposition"), part.getName(), part.getContentType());

            if (part.getName().contains("ChooseFile")) {
                sizeOfFile = part.getSize();
                fileName = getFileNameFromPart(part);
                if (fileName != null) {
                    _log.debug("fileName = {} sizeOfFile = {} Получены успешно. Сохраним в List для дальнейше вствки в БД", fileName, sizeOfFile);
                    inputStream = part.getInputStream();
                    listOfFile.add(new InfoAboutFile(fileName, inputStream, sizeOfFile));
                } else {
                    _log.error("fileName = {} - Не будет добавлен в список на запись в БД", fileName);
                }
            } else if (part.getName().contains("InfoAboutUsers")) {
                StringBuffer sb = new StringBuffer();
                String resultOfReadJSON = null;
                try (InputStreamReader is = new InputStreamReader(part.getInputStream()); BufferedReader br = new BufferedReader(is)) {
                    char[] charr = new char[1024];
                    while (is.read(charr) != -1) {
                        sb.append(charr);
                    }
                    resultOfReadJSON = sb.toString().trim();
                    // TODO: Возможно стоит сделать нормальную обработку в этом месте (т.е. почистить от мусора). Так как JsonParser чувствителен даже к проблема в конце.
                    _log.debug(resultOfReadJSON);
                } catch (IOException e) {
                    _log.error(e);
                }
                // ---------------------------------------------//
                //TODO: Вынести в отдельный класс работы с JSON - может просто в отдельный методы. Здесь я не должен знать как и что разбирать - этот метод doPost не для этого.
                try {
                    _log.debug("Начинаем парсить JSON из полученного POST запроса!");
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(resultOfReadJSON);
                    author = (String) jsonObject.get("author");
                    recipient = (String) jsonObject.get("ricipient");
                    timeInMillisec = (long) jsonObject.get("timeMillisec");
                    // TODO: Может быть ошибка castException. Я сразу получаю Long - а внутри может быть и String.
                    _log.info("То, что получили из запроса: author = {}, recipient = {}, timeInMillisec = {}", author, recipient, timeInMillisec);

                    if (author == null || recipient == null || timeInMillisec == 0) {
                        throw new NullPointerException("Один из параметров является Null");
                    }
                } catch (ParseException | NullPointerException e) {
                    _log.error("Ошибка ParseException/NullPointerException");
                    _log.error(e);
                    response.setStatus(400);
                    try (OutputStream outputStreamForResponse = response.getOutputStream(); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStreamForResponse))) {
                        bw.write("JSON contains invalid data. Raise error while parse that data. Isn`t one value or structure of JSON isn`t correct. Вот часть кириллицы.");
                        // TODO: Подумать, как возвращать кириллицу в читаемом виде для консоли.
                        bw.flush();
                    } catch (IOException ee) {
                        _log.error("Во время записи в поток RESPONSE(поток ответа) возникла ошибка", ee);
                        throw ee;
                    }
                    return;
                }
            }
        }

        IMessage aMessageClass = new Message(_dataSource);
        String stringOfUUID = null;
        Map<String, String> stringOfUUIDs = new HashMap<>();
        String stringAsJSON = null;

        for (InfoAboutFile iab : listOfFile) {
            stringOfUUID = aMessageClass.newMessageToRecipientAsFile(author, iab.getName(), recipient, timeInMillisec, iab.getInputeStream(), iab.getSize());
            stringOfUUIDs.put(iab.getName(), stringOfUUID);
        }
        try (OutputStream outputStreamForResponse = response.getOutputStream(); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStreamForResponse))) {
            JSONObject jsonObject = new JSONObject();
            stringAsJSON = jsonObject.toJSONString(stringOfUUIDs);
            bw.write(stringAsJSON);
            bw.flush();
        } catch (IOException e) {
            _log.error("Во время записи в поток response возникла ошибка", e);
            throw e;
        }
    }

    private String getFileNameFromPart(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                // TODO: Переделать. Осталось как часть примера из интернета.
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            } else {
                _log.info("token = ", token);
            }
        }
        return null;
    }

    @Override
    public void destroy() {
        _log.info("destroy - реализация отсутствует");
    }
}
