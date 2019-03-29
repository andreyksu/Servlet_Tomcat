package ru.andreyksu.annikonenkov.webapp.entranceservlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.andreyksu.annikonenkov.webapp.authorization.Authorization;
import ru.andreyksu.annikonenkov.webapp.authorization.IAuthorization;
import ru.andreyksu.annikonenkov.webapp.commonParameters.ParametersOfUser;
import ru.andreyksu.annikonenkov.webapp.postgressql.DataSourceProvider;
import ru.andreyksu.annikonenkov.webapp.worker.SetterAndDeleterCookies;

public class EntranceServletForRegistration extends HttpServlet {

    private static final long serialVersionUID = 3166979004120383008L;

    private static DataSource _dataSource;

    private static Map<String, String> _mapOfAuthorizedUser;

    private static Logger _log = LogManager.getLogger(EntranceServletForRegistration.class);

    @Override
    public void init() throws ServletException {
        _log.debug("Init");
        try {
            _dataSource = DataSourceProvider.getDataSource();
            _mapOfAuthorizedUser = DataSourceProvider.getMapOfAuthorizedUser();
        } catch (SQLException | NamingException e) {
            _log.error("Произошла ошибка при создании/получении DataSource, для PoolConnection", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        _log.debug("doGet");

        String email = request.getParameter(ParametersOfUser.Login.getParameter());
        String password = request.getParameter(ParametersOfUser.Password.getParameter());
        String name = request.getParameter(ParametersOfUser.Name.getParameter());

        IAuthorization authorization = new Authorization(_dataSource, _mapOfAuthorizedUser);
        SetterAndDeleterCookies workerCookies = new SetterAndDeleterCookies();
        HttpServletResponse tmp = (HttpServletResponse) response;

        if (authorization.registrateUserInSystem(email, password, name)) {
            _log.debug("Удалось пройти регистрацию пользователя, теперь проверям, "
                    + "авторизован ли пользователь и добавляем в cookies");
            authorization.isAuthorizedUserInSystem(email, password);
            workerCookies.setCookies((HttpServletResponse) response, email);
            tmp.sendRedirect("/ChatOnServlet/html/messagePage.html");
        } else {
            _log.warn("Зарегистрироваться не смогли, редиректимся в errorRegPage");
            tmp.sendRedirect("/ChatOnServlet/html/errorRegPage.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse responce) throws IOException {
        _log.info("doPost");
    }

    @Override
    public void destroy() {
        _log.info("destroy");
        _log = null;
        _dataSource = null;
    }

}
