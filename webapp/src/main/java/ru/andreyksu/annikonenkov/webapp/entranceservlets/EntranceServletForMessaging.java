package ru.andreyksu.annikonenkov.webapp.entranceservlets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

import ru.andreyksu.annikonenkov.webapp.commonParameters.ParametersForMessageRequest;
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

    private class InfoAboutAuthor {

        private String _author = null;

        private String _recipient = null;

        private long _timeInMillisec = 0;

        public InfoAboutAuthor(String author, String recipient, long timeInMillisec) {
            _author = author;
            _recipient = recipient;
            _timeInMillisec = timeInMillisec;
        }

        public String getAuthor() {
            return _author;
        }

        public String getRecipient() {
            return _recipient;
        }

        public long getTimeAsMillisecond() {
            return _timeInMillisec;
        }

    }

    private static final long serialVersionUID = 4779791835788040403L;

    private static Logger _log = LogManager.getLogger(EntranceServletForMessaging.class.getCanonicalName());

    private static DataSource _dataSource;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        _log.debug("------------------doGet------------------");

        long timeMillisecParamAsLong = 0;
        String messageForAnswerFromSQLQuery = null;
        IMessage aMessageClass = new Message(_dataSource);

        WrapperMutableHttpServletRequest wrappedRequest = (WrapperMutableHttpServletRequest) request;

        String email = wrappedRequest.getHeader(ParametersForMessageRequest.LoginMemeber.getParameter());

        String directParam = wrappedRequest.getParameter(ParametersForMessageRequest.Direct.getParameter());
        String loginMemberParam = wrappedRequest.getParameter(ParametersForMessageRequest.LoginMemeber.getParameter());
        String interlocutorParam = wrappedRequest.getParameter(ParametersForMessageRequest.Interlocutor.getParameter());
        String timeMillisecParam = wrappedRequest.getParameter(ParametersForMessageRequest.TimeMillisec.getParameter());

        if (directParam == null || loginMemberParam == null || interlocutorParam == null || timeMillisecParam == null) {
            _log.error(String.format(
                    "The one or more parameters is Null or Empty directParam = %s, loginMemberParam = %s, "
                            + "interlocutorParam = %s, timeMillisecParam = %s ",
                    directParam, loginMemberParam, interlocutorParam, timeMillisecParam));
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The one or more parameters is Null or Empty");
            return;
        }
        // ---------------------------------------------//
        try {
            timeMillisecParamAsLong = Long.parseLong(timeMillisecParam);
        } catch (NumberFormatException e) {
            _log.error("В полученном запросе не смогли распарсить миллисекунды как long - данные не будет записаны "
                    + "или прочитаны");
            _log.error(e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "В полученном запросе не смогли распарсить миллисекунды!");
            return;
        }
        // ---------------------------------------------//
        if (directParam.equals("get")) {
            String historical = wrappedRequest.getParameter(ParametersForMessageRequest.Historical.getParameter());
            messageForAnswerFromSQLQuery = aMessageClass.getMessagesFromRecipient(loginMemberParam, interlocutorParam,
                    timeMillisecParamAsLong, historical);

            _log.debug("Для пользователя='{}' Выдернули сообщение = {}", email, messageForAnswerFromSQLQuery);
            _log.debug("Запишем полученное сообщение из БД в ответ т.е. в response");
            try (OutputStream outputStreamForResponse = response.getOutputStream();
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStreamForResponse))) {
                bw.write(messageForAnswerFromSQLQuery);
                bw.flush();
            } catch (IOException e) {
                _log.error("Во время записи в поток RESPONSE(поток ответа) возникла ошибка", e);
                throw e;
            }

        } else if (directParam.equals("send")) {
            String textMessageParam =
                    wrappedRequest.getParameter(ParametersForMessageRequest.TextMessage.getParameter());
            aMessageClass.newMessageToRecipient(loginMemberParam, textMessageParam, interlocutorParam,
                    timeMillisecParamAsLong, false);
        } else {
            _log.error("Unknow got Direct param");
            response.setStatus(400);
            return;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        _log.debug("------------------doPost------------------");
        _log.debug("request.getContentType() = {}, request.getCharacterEncoding() = {}", request.getContentType(),
                request.getCharacterEncoding());

        request.setCharacterEncoding("utf-8");

        List<InfoAboutFile> listOfFile = new ArrayList<>();
        InfoAboutAuthor infoAboutUser = null;

        for (Part part : request.getParts()) {

            _log.debug(
                    "part.getHeader(\"content-disposition\") = {}, part.getName() = {}, "
                            + "part.getContentType() = {}, part.getSubmittedFileName() = {}",
                    part.getHeader("content-disposition"), part.getName(), part.getContentType(),
                    part.getSubmittedFileName());

            if (part.getName().contains("ChooseFile")) {
                fillTheListOfGetedFiles(part, listOfFile);
            } else if (part.getName().contains("InfoAboutUsers")) {
                infoAboutUser = fillInfoAboutUser(part);
                if (infoAboutUser == null) {
                    response.setStatus(400);
                    return;
                }
            } else {
                _log.debug("В полученно запросе содержатся лишние данные. Они будут проигнорированны!");
            }
        }

        String stringOfUUID = null;
        String stringAsJSON = null;
        IMessage aMessageClass = new Message(_dataSource);
        Map<String, String> stringOfUUIDs = new HashMap<>();

        for (InfoAboutFile infoAboutFile : listOfFile) {
            stringOfUUID = aMessageClass.newMessageToRecipientAsFile(infoAboutUser.getAuthor(), infoAboutFile.getName(),
                    infoAboutUser.getRecipient(), infoAboutUser.getTimeAsMillisecond(), infoAboutFile.getInputeStream(),
                    infoAboutFile.getSize());
            stringOfUUIDs.put(infoAboutFile.getName(), stringOfUUID);
        }
        try (PrintWriter printWriter = response.getWriter()) {
            stringAsJSON = JSONObject.toJSONString(stringOfUUIDs);
            printWriter.write(stringAsJSON);
            printWriter.flush();
        } catch (IOException e) {
            _log.error("Во время записи в поток response возникла ошибка");
            _log.catching(e);
            response.setStatus(400);
            return;
        }
    }

    private InfoAboutAuthor fillInfoAboutUser(Part part) throws IOException {
        String author = null;
        String recipient = null;
        long timeInMillisec = 0;

        StringBuffer sb = new StringBuffer();
        String resultOfReadJSON = null;

        try (InputStreamReader is = new InputStreamReader(part.getInputStream());
                BufferedReader br = new BufferedReader(is)) {
            char[] charr = new char[1024];
            while (is.read(charr) != -1) {
                sb.append(charr);
            }
            resultOfReadJSON = sb.toString().trim();
            _log.debug("JSON полученный из запроса = {}", resultOfReadJSON);
        } catch (IOException e) {
            _log.error("Возникла ошибка при чтении JSON из POST запроса");
            _log.catching(e);
            throw e;
        }

        try {
            _log.debug("Начинаем парсить JSON из полученного POST запроса!");
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(resultOfReadJSON);

            author = (String) jsonObject.get("author");
            recipient = (String) jsonObject.get("ricipient");
            timeInMillisec = (long) jsonObject.get("timeMillisec");

            _log.debug("То, что получили из запроса: author = {}, recipient = {}, timeInMillisec = {}", author,
                    recipient, timeInMillisec);
        } catch (ParseException e) {
            _log.error("Ошибка при попытке распарисить полученный сырой JSON. Файл не будтет записан");
            _log.catching(e);
            return null;
        } catch (ClassCastException e) {
            _log.error("Ошибка при попытке получить время в миллисекундах. Видимо получили ");
            _log.catching(e);
            return null;
        }
        // TODO: По времени нужно проверять что бы не было слишком старым и что
        // бы не было вперед.
        if (author == null || recipient == null || timeInMillisec == 0) {
            _log.error("Один из существенных параметров является null. Ни один файл, что был передан не будет сохранен "
                    + "author = {}, recipient = {}, timeInMillisec = {}", author, recipient, timeInMillisec);
            return null;
        } else {
            _log.debug("Все параметры о пользователях для сохранения файлов были получены успешно!");
            return new InfoAboutAuthor(author, recipient, timeInMillisec);
        }
    }

    /**
     * Заполняет список файлов, что будут записаны в БД. Реализация сделана для
     * множества файлов. Но по факту на стороне клиента сделано, что можно
     * отправить только один файл.
     * 
     * @param part - часть из полученного Post запроса.
     * @param listOfFile - List<InfoAboutFile> - коллекция информации о файле.
     */
    private void fillTheListOfGetedFiles(Part part, List<InfoAboutFile> listOfFile) {

        InputStream inputStream = null;
        long sizeOfFile = part.getSize();
        String fileName = getFileNameFromPart(part);

        if (fileName == null) {
            _log.error("Имя файла = null. Данный файл не будет записан в БД");
            return;
        }
        _log.debug("fileName = {} sizeOfFile = {} Получены успешно. Сохраним в List для дальнейше вствки в БД",
                fileName, sizeOfFile);
        try {
            inputStream = part.getInputStream();
            listOfFile.add(new InfoAboutFile(fileName, inputStream, sizeOfFile));
        } catch (IOException e) {
            _log.error("При получении part.getInputStream() для Файла = {} возникла ошибка. "
                    + "Файл не будет добавлен для зписи в БД");
            _log.catching(e);
        }
    }

    private String getFileNameFromPart(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                // TODO: Переделать. Осталось как часть примера из интернета.
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return null;
    }

    @Override
    public void destroy() {
        _log.info("destroy");
        _log = null;
        _dataSource = null;
    }
}
