package ru.andreyksu.annikonenkov.webapp.filters;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.andreyksu.annikonenkov.webapp.authorization.Authorization;
import ru.andreyksu.annikonenkov.webapp.authorization.IAuthorization;
import ru.andreyksu.annikonenkov.webapp.postgressql.DataSourceProvider;
import ru.andreyksu.annikonenkov.webapp.worker.SetterAndDeleterCookies;
import ru.andreyksu.annikonenkov.webapp.wrappers.WrapperMutableHttpServletRequest;

/**
 * Третий фильтр в цепочке фильтров.
 * <p>
 * Основная задача данного фильтра, исходя из основного параметра запроса
 * редиректить на те методы и страницы, что соответствуют значению параметра.
 */

public class SecondFilter implements Filter {

	private static final Logger ___log = LogManager.getLogger(SecondFilter.class);

	private static Map<String, String> _mapOfAuthUser;

	private static DataSource _dataSource;

	private static final String _loginMember = "LoginMemeber";

	private static final String _passwordMember = "PasswordMember";

	private static final String _nameMember = "NameMember";

	private static final String _authorization = "Authorization";

	private static final String _registration = "Registration";

	private static final String _message = "Message";

	private static final String _unLogin = "UnLogin";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		___log.debug("___Filter2___Init");
		try {
			DataSourceProvider dataSP = DataSourceProvider.getSQLDataSource();
			_dataSource = dataSP.getDataSource();
			_mapOfAuthUser = dataSP.getMapAuthUser();
		} catch (SQLException | NamingException e) {
			___log.error("Произошла ошибка при создании/получении DataSource, для PoolConnection", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		___log.debug("___Filter2___DoFilter");
		String mainParam = request.getParameter("mode");
		if (mainParam != null) {
			// TODO: Вероятно здесь нужно проверять все остальные параметры на
			// null ибо потом они везде используются и везде добавлять эту
			// проверку на null вероятно не стоит.
			___log.debug(String.format("___Filter2___ mode = %s", mainParam));
			IAuthorization authorization = new Authorization(___log, _dataSource, _mapOfAuthUser);
			SetterAndDeleterCookies workerCookies = new SetterAndDeleterCookies(___log);
			if (mainParam.equals(_authorization)) {
				String email = request.getParameter(_loginMember);
				String password = request.getParameter(_passwordMember);
				___log.debug(String.format("___Filter2___ LoginMemeber = %s  PasswordMember = %s", email, password));
				if (authorization.authorizedInSystem(email, password)) {
					workerCookies.setCookies((HttpServletResponse) response, email);
					chain.doFilter(request, response);
				} else {
					___log.debug("___Filter2___редиректимся в Authorization");
					HttpServletResponse tmp = (HttpServletResponse) response;
					tmp.sendRedirect("/ChatOnServlet/html/errorAuthPage.html");
				}

			} else if (mainParam.equals(_registration)) {
				String email = request.getParameter(_loginMember);
				String password = request.getParameter(_passwordMember);
				String name = request.getParameter(_nameMember);
				___log.debug(String.format("___Filter2___ LoginMemeber = %s  PasswordMember = %s  NameMember = %s", email, password, name));
				if (authorization.registrate(email, password, name)) {
					authorization.authorizedInSystem(email, password);
					workerCookies.setCookies((HttpServletResponse) response, email);
					chain.doFilter(request, response);
				} else {
					___log.debug("___Filter2___редиректимся в Registration");
					HttpServletResponse tmp = (HttpServletResponse) response;
					tmp.sendRedirect("/ChatOnServlet/html/errorRegPage.html");
				}

			} else if (mainParam.equals(_message)) {
				WrapperMutableHttpServletRequest wrappedRequest = (WrapperMutableHttpServletRequest) request;
				String email = wrappedRequest.getHeader(_loginMember);
				___log.debug(String.format("LoginMemeber = %s", email));
				if (email != null && authorization.isAuthorizedUser(email)) {
					workerCookies.setCookies((HttpServletResponse) response, email);
					chain.doFilter(request, response);
				} else {
					___log.debug("___Filter2___редиректимся в Message");
					HttpServletResponse tmp = (HttpServletResponse) response;
					// TODO: Вероятно так не стоит делать, возможно здесь стоит
					// сделать просто возврат url - а в js скрипте проверять.
					tmp.sendRedirect("/ChatOnServlet/loginPage.html");
				}

			} else if (mainParam.equals(_unLogin)) {
				String email = request.getParameter(_loginMember);
				authorization.unAuthorizedUser(email);
				workerCookies.deleteCookies((HttpServletResponse) response, email);
				___log.debug("___Filter2___sendRedirect---UnLogin");

				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/plain");
				try (PrintWriter pw = response.getWriter()) {
					pw.println("/ChatOnServlet/loginPage.html");
					pw.flush();
					pw.close();
				} catch (Exception e) {
					___log.catching(e);
				}

			} else {
				___log.debug("___Filter2___ sendRedirect                Ни по одному if не прошли");
				HttpServletResponse tmp = (HttpServletResponse) response;
				tmp.sendRedirect("/ChatOnServlet/err404.html");
			}

		} else {
			___log.debug("___Filter2___ sendRedirect                mainParam==null");
			HttpServletResponse tmp = (HttpServletResponse) response;
			tmp.sendRedirect("/ChatOnServlet/err404.html");
		}
	}

	@Override
	public void destroy() {
		___log.info("___Filter2___Destroy");
	}

}
