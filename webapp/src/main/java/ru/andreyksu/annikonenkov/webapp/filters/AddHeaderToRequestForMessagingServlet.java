package ru.andreyksu.annikonenkov.webapp.filters;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
import ru.andreyksu.annikonenkov.webapp.wrappers.WrapperMutableHttpServletRequest;

/**
 * Второй фильтр в цепочке фильтров.
 * <p>
 * Основная задача данного фильтра, просмотреть запрос. Если в запросе есть параметр "Message" означающий, что идет обмен сообщениями, то проверяет по Cookies -
 * авторизован ли пользователь. Если по Cookies выясняется, что пользователь авторизован, то добавляет необходимые параметры для следующего фильтра чтобы в
 * следующем фильтре как можно быстрее прошла проверка (т.е. с минимальным временем). Добавление параметров выполняется через обертку
 * WrapperMutableHttpServletRequest.
 * <p>
 * В ином случае если по cookies определяется, что пользователь не является авторизованыым а параметр равен "Message", то прокидывается на следующий фильтр, без
 * соответствующих параметров. А там на страницу авторизации.
 */
public class AddHeaderToRequestForMessagingServlet implements Filter {

    private static final Logger _log = LogManager.getLogger(AddHeaderToRequestForMessagingServlet.class);

    private final String _loginMember = InterfaceRepresentUserFromRequest.Login;;

    private static DataSource _dataSource;

    private static Map<String, String> _mapOfAuthorizedUser;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        WrapperMutableHttpServletRequest mutableRequest = new WrapperMutableHttpServletRequest((HttpServletRequest) request);
        printInfo(mutableRequest);

        IAuthorization authorization = new Authorization(_dataSource, _mapOfAuthorizedUser);
        SetterAndDeleterCookies workerCookies = new SetterAndDeleterCookies();

        boolean isExistCookie = workerCookies.isExistsUserByCookies((HttpServletRequest) mutableRequest, (HttpServletResponse) response);
        String extractedEmailFromCookie = workerCookies.getEmail();
        if (isExistCookie && extractedEmailFromCookie != null && authorization.isPresentUserInLocalMapAsAuthorized(extractedEmailFromCookie)) {
            mutableRequest.putHeader(_loginMember, workerCookies.getEmail());
            chain.doFilter(mutableRequest, response);
            _log.debug("Перед возвратом");
        } else {
            _log.debug("Не нашли в cookies информацию о пользователе. Отправляем на страницу авторизации!");
            HttpServletResponse tmp = (HttpServletResponse) response;
            authorization.unAuthorizedUser(extractedEmailFromCookie);// TODO: Нужно ли это тут.
            tmp.sendRedirect("/ChatOnServlet/loginPage.html");
        }
    }

    @Override
    public void destroy() {
        _log.debug("Destroy");
    }

    /**
     * !!!Добавлен для отладки!!! Только выводит в логи информацию из полученного запроса.
     * 
     * @param httpreq - объект представляющий собой запрос.
     */
    private void printInfo(HttpServletRequest httpreq) {
        _log.debug("PrintInfo: Выводим_основные_параметры_запроса_для_отладки!");
        Enumeration<String> enumAtr = httpreq.getAttributeNames();
        _log.debug("	Attrebute - доступные значения из запроса");
        while (enumAtr.hasMoreElements()) {
            String string = (String) enumAtr.nextElement();
            String val = httpreq.getParameter(string);
            _log.debug("{} = {}", string, val);
        }
        Enumeration<String> enumHeader = httpreq.getHeaderNames();
        _log.debug("	Headers - доступные значения из запроса");
        while (enumHeader.hasMoreElements()) {
            String string = (String) enumHeader.nextElement();
            String val = httpreq.getHeader(string);
            _log.debug("{} = {}", string, val);
        }

        Map<String, String[]> param = httpreq.getParameterMap();
        _log.debug("	Parameter - доступные значения из запроса");
        for (String str : param.keySet()) {
            String tmp = str + " :: ";
            String[] par = param.get(str);
            for (String tmpPar : par) {
                tmp = tmp + tmpPar + "; ";
            }
            _log.debug(tmp);
        }

    }

}
