package ru.andreyksu.annikonenkov.webapp.entranceservlets;

import java.io.IOException;
import java.io.PrintWriter;
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
import ru.andreyksu.annikonenkov.webapp.commonParameters.InterfaceRepresentUserFromRequest;
import ru.andreyksu.annikonenkov.webapp.postgressql.DataSourceProvider;
import ru.andreyksu.annikonenkov.webapp.worker.SetterAndDeleterCookies;

public class EntranceServletForLogOut extends HttpServlet {

    private static final long serialVersionUID = 753279128343074852L;

    private final String _loginMember = InterfaceRepresentUserFromRequest.Login;

    private static DataSource _dataSource;

    private static Map<String, String> _mapOfAuthorizedUser;

    private static final Logger _log = LogManager.getLogger(EntranceServletForLogOut.class);

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        _log.debug("doGet");

        IAuthorization authorization = new Authorization(_dataSource, _mapOfAuthorizedUser);
        SetterAndDeleterCookies workerCookies = new SetterAndDeleterCookies();

        String email = request.getParameter(_loginMember);
        authorization.unAuthorizedUser(email);
        workerCookies.deleteCookies((HttpServletResponse) response, email);
        _log.debug("UnLogin is Done. Cookies is pure");

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        try (PrintWriter pw = response.getWriter()) {
        	_log.debug("Переводим на стринцу loginPage.html");
            pw.println("/ChatOnServlet/loginPage.html");
            pw.flush();
            // pw.close(); //Больше не нужен, так как создается объект внутри try.
        } catch (Exception e) {
            _log.catching(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        _log.info("doPost");
    }

    @Override
    public void destroy() {
        _log.info("destroy");
    }

}
