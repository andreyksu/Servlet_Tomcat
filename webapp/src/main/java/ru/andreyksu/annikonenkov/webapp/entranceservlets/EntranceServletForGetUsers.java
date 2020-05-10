package ru.andreyksu.annikonenkov.webapp.entranceservlets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import ru.andreyksu.annikonenkov.webapp.commonParameters.ParametersOfUser;
import ru.andreyksu.annikonenkov.webapp.postgressql.DataSourceProvider;
import ru.andreyksu.annikonenkov.webapp.userrepresent.WorkerWithUser;
import ru.andreyksu.annikonenkov.webapp.wrappers.WrapperMutableHttpServletRequest;

public class EntranceServletForGetUsers extends HttpServlet {

    private static final long serialVersionUID = -4138202365466220495L;

    private static DataSource _dataSource;

    private static Logger _log = LogManager.getLogger(EntranceServletForGetUsers.class);

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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        _log.debug("doGet");

        WrapperMutableHttpServletRequest wrappedRequest = (WrapperMutableHttpServletRequest) request;
        String emailOfCurrentUser = wrappedRequest.getHeader(ParametersOfUser.Login.getParameter());

        WorkerWithUser workerWithUser = new WorkerWithUser(_dataSource);
        List<String> listOfUser = workerWithUser.getListOfUsers(true);
        listOfUser.remove(emailOfCurrentUser);

        String listOfActiveUserAsString = listOfUser.toString();
        _log.debug("Получили следующий список пользователей: {}", listOfActiveUserAsString);

        JSONArray array = new JSONArray();
        array.addAll(listOfUser);
        /*
         * for (String str : listOfUser) { array.add(str); }
         */
        listOfActiveUserAsString = array.toJSONString();
        _log.debug("Получили следующий список пользователей JSON: {}", listOfActiveUserAsString);
        try (OutputStream outputStreamForResponse = response.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStreamForResponse))) {
            bw.write(listOfActiveUserAsString);
            bw.flush();
        } catch (IOException e) {
            _log.error("Во время записи в поток response возникла ошибка", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse responce) throws IOException {
        _log.info("Unimplemented method - doPost");
    }

    @Override
    public void destroy() {
        _log.info("destroy");
        _log = null;
        _dataSource = null;
    }

}
