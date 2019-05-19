package ru.andreyksu.annikonenkov.webapp.entranceservlets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.translation.messages_es;

import ru.andreyksu.annikonenkov.webapp.commonParameters.InterfaceRepresentUserFromRequest;
import ru.andreyksu.annikonenkov.webapp.messages.IMessage;
import ru.andreyksu.annikonenkov.webapp.messages.Message;
import ru.andreyksu.annikonenkov.webapp.postgressql.DataSourceProvider;
import ru.andreyksu.annikonenkov.webapp.wrappers.WrapperMutableHttpServletRequest;

public class EntranceServletForMessaging extends HttpServlet {

    private static final long serialVersionUID = 4779791835788040403L;

    private static final Logger _log = LogManager.getLogger(EntranceServletForMessaging.class);

    private static DataSource _dataSource;

    private final String _loginMember = InterfaceRepresentUserFromRequest.Login;

    @Override
    public void init() throws ServletException {
        _log.debug("Init");
        try {
            _dataSource = DataSourceProvider.getDataSource();
        } catch (SQLException | NamingException e) {
            _log.error("Произошла ошибка при создании/получении DataSource, для PoolConnection", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        _log.debug("doGet");

        boolean isPresentError = false;
        long timeMillisecParamAsLong = 0;

        StringBuilder messageError = new StringBuilder();
        IMessage aMessageClass = new Message(_dataSource);

        WrapperMutableHttpServletRequest wrappedRequest = (WrapperMutableHttpServletRequest) request;
        String email = wrappedRequest.getHeader(_loginMember);

        String directParam = wrappedRequest.getParameter("Direct");
        String loginMemberParam = wrappedRequest.getParameter("LoginMemeber");
        String interlocutorParam = wrappedRequest.getParameter("Interlocutor");
        String timeMillisecParam = wrappedRequest.getParameter("timeMillisec");

        String messageFromQuery = null;

        if (directParam == null || loginMemberParam == null || interlocutorParam == null || timeMillisecParam == null) {
            isPresentError = true;
            messageError.append("The one ore more parameters is Null or Empty");
            messageError.append(" : ");
        }
        // ---------------------------------------------//
        try {
            timeMillisecParamAsLong = Long.parseLong(timeMillisecParam);
        } catch (NumberFormatException e) {
            _log.debug("Миллисекунды не смогли распарсить как long");
            _log.debug(e);
            isPresentError = true;
            messageError.append("Error in Millisecond");
            messageError.append(" : ");
        }
        // ---------------------------------------------//
        if (isPresentError == false && directParam.equals("get")) {
            String historical = wrappedRequest.getParameter("historical");
            messageFromQuery = aMessageClass.getMessagesFromRecipient(loginMemberParam, interlocutorParam, timeMillisecParamAsLong, historical);

            _log.debug("Для пользователя='{}' Выдернули сообщение = {}", email, messageFromQuery);
            
            try (OutputStream outputStreamForResponse = response.getOutputStream();
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStreamForResponse))) {
                bw.write(messageFromQuery);
                bw.flush();
            } catch (IOException e) {
                _log.error("Во время записи в поток response возникла ошибка", e);
            }

        } else if (isPresentError == false && directParam.equals("send")) {
            String textMessageParam = wrappedRequest.getParameter("textMessage");
            aMessageClass.newMessageToRecipient(loginMemberParam, textMessageParam, interlocutorParam, timeMillisecParamAsLong);
        } else {
            isPresentError = true;
            messageError.append("Unknow got Direct param");
            messageError.append(" : ");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        _log.info("doPost - реализация отсутствует");
    }

    @Override
    public void destroy() {
        _log.info("destroy - реализация отсутствует");
    }
}
