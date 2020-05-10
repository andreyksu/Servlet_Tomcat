package ru.andreyksu.annikonenkov.webapp.entranceservlets;

import java.io.IOException;
import java.sql.SQLException;

import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.andreyksu.annikonenkov.webapp.commonParameters.ParametersForFile;
import ru.andreyksu.annikonenkov.webapp.email.EmailWorker;
import ru.andreyksu.annikonenkov.webapp.messages.IMessage;
import ru.andreyksu.annikonenkov.webapp.messages.Message;
import ru.andreyksu.annikonenkov.webapp.postgressql.DataSourceProvider;
import ru.andreyksu.annikonenkov.webapp.wrappers.WrapperMutableHttpServletRequest;

public class FileDownloadServlet extends HttpServlet {

    private static Logger _log = LogManager.getLogger(FileDownloadServlet.class);

    private static final long serialVersionUID = 205242440643911308L;

    private static DataSource _dataSource;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        _log.debug("Init FileDownloadServlet");
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
        WrapperMutableHttpServletRequest wrappedRequest = (WrapperMutableHttpServletRequest) request;
        String uuidFromRequest = wrappedRequest.getParameter(ParametersForFile.UUID.getParameter());
        String directionParam = wrappedRequest.getParameter(ParametersForFile.Direct.getParameter());
        String currentMember = wrappedRequest.getParameter(ParametersForFile.LoginMemeber.getParameter());
        String textForMessage = wrappedRequest.getParameter(ParametersForFile.FileName.getParameter());

        if (uuidFromRequest != null && directionParam != null && currentMember != null && textForMessage != null) {

            IMessage aMessageClass = new Message(_dataSource);
            byte[] arrayOfByteForFile = aMessageClass.getFileFromDB(uuidFromRequest);

            try {
                EmailWorker emailWorker = new EmailWorker(DataSourceProvider.getMailSession());
                // emailWorker.sendEmail("andreyksu@gmail.com");
                emailWorker.getMessages();
                emailWorker.getMessages("andreyksu@gmail.com");
            } catch (NamingException | MessagingException e) {
                _log.error("В ходе работы с почтной возникли ошибки");
                _log.catching(e);
            }

            if (arrayOfByteForFile != null) {

                ServletConfig servletConfig = this.getServletConfig();
                _log.debug("ServletConfig = {}", servletConfig);
                if (servletConfig != null) {
                    ServletContext servletContext = servletConfig.getServletContext();
                    String mimeType = servletContext.getMimeType(textForMessage);
                    _log.debug("Получили MIME из сонтекста сервлета mimeType = {}", mimeType);
                }

                response.setContentType("application/octet-stream");
                response.setContentLength((int) arrayOfByteForFile.length);
                String str = String.format("attachment; filename=\"%s\"", textForMessage);
                _log.debug(
                        "То что имеем перед возвратом uuidFromRequest = {}, "
                                + "directionParam = {}, currentMember = {}, textForMessage = {}, str = {}",
                        uuidFromRequest, directionParam, currentMember, textForMessage, str);
                response.setHeader("Content-Disposition", str);
                try (ServletOutputStream os = response.getOutputStream()) {
                    os.write(arrayOfByteForFile);
                    os.flush();
                }
            } else {
                _log.warn("По указанному uuid = {} видимо в БД ничего не нашли", uuidFromRequest);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "По переданному uuid ничего не найдено в БД");
            }
        } else {
            _log.warn("Один из переданных параемтров является null uuidFromRequest = {}, directionParam = {}, "
                    + "currentMember = {}", uuidFromRequest, directionParam, currentMember);
            // response.setStatus(404);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Один из переданных параемтров является null");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        _log.warn("Метод post не реализован");
        response.setStatus(404);
    }

    @Override
    public void destroy() {
        _log.info("destroy");
        _log = null;
        _dataSource = null;
    }

}
