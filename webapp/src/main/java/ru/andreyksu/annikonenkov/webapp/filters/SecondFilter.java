package ru.andreyksu.annikonenkov.webapp.filters;

import java.io.IOException;
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

public class SecondFilter implements Filter {

	private static final Logger _log = LogManager.getLogger(SecondFilter.class);

	private static Map<String, String> _mapOfAuthUser;

	private static DataSource _dataSourcel;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		_log.debug("___Filter2___Init");
		try {
			DataSourceProvider dataSP = DataSourceProvider.getSQLDataSource();
			_dataSourcel = dataSP.getDataSource();
			_mapOfAuthUser = dataSP.getMapAuthUser();
		} catch (SQLException | NamingException e) {
			_log.error("Произошла ошибка при создании/получении DataSource, для PoolConnection", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		_log.debug("___Filter2___DoFilter");
		String mainParam = request.getParameter("mode");
		if (mainParam != null) {
			_log.debug("___Filter2___ mode != null");
			IAuthorization authorization = new Authorization(_log, _dataSourcel, _mapOfAuthUser);
			SetterAndDeleterCookies workerCookies = new SetterAndDeleterCookies(_log);
			if (mainParam.equals("Authorization")) {
				String email = request.getParameter("LoginMemeber");
				String password = request.getParameter("PasswordMember");
				if (authorization.authorizedInSystem(email, password)) {
					workerCookies.setCookies((HttpServletResponse) response, email);
					chain.doFilter(request, response);
				} else {
					_log.debug("___Filter2___sendRedirect---Authorization");
					HttpServletResponse tmp = (HttpServletResponse) response;
					tmp.sendRedirect("/ChatOnServlet/html/errorAuthPage.html");
				}
			}
			if (mainParam.equals("Registration")) {
				String email = request.getParameter("LoginMemeber");
				String password = request.getParameter("PasswordMember");
				String name = request.getParameter("NameMember");
				if (authorization.registrate(email, password, name)) {
					authorization.authorizedInSystem(email, password);
					workerCookies.setCookies((HttpServletResponse) response, email);
					chain.doFilter(request, response);
				} else {
					_log.debug("___Filter2___sendRedirect---Registration");
					HttpServletResponse tmp = (HttpServletResponse) response;
					tmp.sendRedirect("/ChatOnServlet/html/errorRegPage.html");
				}
			}
			if (mainParam.equals("Message")) {
				String email = request.getParameter("LoginMemeber");
				_log.debug(String.format("email = %s", email));
				if (authorization.isAuthorizedUser(email)) {
					workerCookies.setCookies((HttpServletResponse) response, email);
					chain.doFilter(request, response);
				} else {
					_log.debug("___Filter2___sendRedirect---Message");
					HttpServletResponse tmp = (HttpServletResponse) response;
					tmp.sendRedirect("/ChatOnServlet/loginPage.html");
				}
			}
			if (mainParam.equals("UnLogin")) {
				String email = request.getParameter("LoginMemeber");
				authorization.unAuthorizedUser(email);
				workerCookies.deleteCookies((HttpServletResponse) response, email);
				_log.debug("___Filter2___sendRedirect---UnLogin");
				HttpServletResponse tmp = (HttpServletResponse) response;
				tmp.sendRedirect("/ChatOnServlet/loginPage.html");
			}
		} else {
			_log.debug("___Filter2___sendRedirect                mainParam==null");
			HttpServletResponse tmp = (HttpServletResponse) response;
			tmp.sendRedirect("/ChatOnServlet/err404.html");

			// RequestDispatcher requestDispatcher =
			// request.getRequestDispatcher("/err404.html");
			// requestDispatcher.forward(request, response);

		}
	}

	@Override
	public void destroy() {
		_log.info("___Filter2___Destroy");
	}

}
