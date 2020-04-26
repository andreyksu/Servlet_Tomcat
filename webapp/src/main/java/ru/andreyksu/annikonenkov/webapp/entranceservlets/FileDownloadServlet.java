package ru.andreyksu.annikonenkov.webapp.entranceservlets;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.andreyksu.annikonenkov.webapp.messages.IMessage;
import ru.andreyksu.annikonenkov.webapp.messages.Message;
import ru.andreyksu.annikonenkov.webapp.postgressql.DataSourceProvider;
import ru.andreyksu.annikonenkov.webapp.wrappers.WrapperMutableHttpServletRequest;

public class FileDownloadServlet extends HttpServlet {

    private static final Logger _log = LogManager.getLogger(FileDownloadServlet.class);

    private static final long serialVersionUID = 205242440643911308L;

    private static DataSource _dataSource;

    @Override
    public void init() throws ServletException {
        _log.debug("Init FileDownloadServlet");
        try {
            _dataSource = DataSourceProvider.getDataSource();
        } catch (SQLException | NamingException e) {
            _log.error("Произошла ошибка при создании/получении DataSource, для PoolConnection", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        WrapperMutableHttpServletRequest wrappedRequest = (WrapperMutableHttpServletRequest) request;

        String uuidFromRequest = wrappedRequest.getParameter("FileUUD");
        String directionParam = wrappedRequest.getParameter("Direct");
        String currentMember = wrappedRequest.getParameter("LoginMemeber");
        String textForMessage = wrappedRequest.getParameter("FileName");
        
        if (uuidFromRequest != null && directionParam != null && currentMember != null && textForMessage != null) {

            IMessage aMessageClass = new Message(_dataSource);
            byte[] arrayOfByteForFile = aMessageClass.getFileFromDB(uuidFromRequest);

            if (arrayOfByteForFile != null) {
                response.setContentType("application/octet-stream");
                response.setContentLength((int) arrayOfByteForFile.length);                
                String str = String.format("attachment; filename=\"%s\"", textForMessage);
                _log.info("То что имеем перед возвратом uuidFromRequest = {} directionParam = {} currentMember = {} textForMessage = {} str = {}", uuidFromRequest, directionParam, currentMember, textForMessage, str);
                response.setHeader("Content-Disposition", str);
                try (ServletOutputStream os = response.getOutputStream()) {
                    os.write(arrayOfByteForFile);
                    os.flush();
                }
            } else {
                _log.debug("По указанному uuid = {} видимо в БД ничего не нашли", uuidFromRequest);
                response.setStatus(400);
            }
        } else {
            _log.error("Один из переданных параемтров является null uuidFromRequest = {} directionParam = {} currentMember ={}", uuidFromRequest, directionParam, currentMember);
            response.setStatus(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        _log.warn("Метод post не реализован");
        response.setStatus(404);
    }

}
