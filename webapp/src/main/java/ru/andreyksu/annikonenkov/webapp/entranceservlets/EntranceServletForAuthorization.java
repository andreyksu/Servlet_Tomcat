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

public class EntranceServletForAuthorization extends HttpServlet {

    private static final long serialVersionUID = -305400761676131269L;

    private final String _passwordMember = ParametersOfUser.Password.getParameter();

    private static DataSource _dataSource;

    private static Map<String, String> _mapOfAuthorizedUser;

    private static Logger _log = LogManager.getLogger(EntranceServletForAuthorization.class);

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        _log.debug("doGet");

        IAuthorization authorization = new Authorization(_dataSource, _mapOfAuthorizedUser);
        SetterAndDeleterCookies workerCookies = new SetterAndDeleterCookies();

        String email = request.getParameter(ParametersOfUser.Login.getParameter());
        String password = request.getParameter(_passwordMember);
        _log.debug(String.format("LoginMemeber = %s  PasswordMember = %s", email, password));
        if (authorization.isAuthorizedUserInSystem(email, password)) {
            workerCookies.setCookies((HttpServletResponse) response, email);
            HttpServletResponse tmp = (HttpServletResponse) response;
            tmp.sendRedirect("/ChatOnServlet/html/messagePage.html");

        } else {
            _log.debug("Авторизация пользователя не успешная. Редиректимся на страницу ошибочной авторизации.");
            HttpServletResponse servletResponce = (HttpServletResponse) response;
            servletResponce.sendRedirect("/ChatOnServlet/html/errorAuthPage.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        _log.info("Unimplemented method - doPost");
    }

    @Override
    public void destroy() {
        _log.info("destroy");
        _log = null;
        _dataSource = null;
    }

}
