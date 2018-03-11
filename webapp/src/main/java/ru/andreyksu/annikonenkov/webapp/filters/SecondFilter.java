package ru.andreyksu.annikonenkov.webapp.filters;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.andreyksu.annikonenkov.webapp.authorization.Authorization;
import ru.andreyksu.annikonenkov.webapp.authorization.IAuthorization;
import ru.andreyksu.annikonenkov.webapp.postgressql.SQLDataSourceProvider;
import ru.andreyksu.annikonenkov.webapp.worker.WorkerCookies;

public class SecondFilter implements Filter {

	private static final Logger _log = LogManager.getLogger(SecondFilter.class);

	private static DataSource _dataSourcel;

	private static IAuthorization authorization;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		_log.info("-----Init of Filter2");
		try {
			_dataSourcel = SQLDataSourceProvider.getSQLDataSource().getDataSource();
			authorization = new Authorization(_log, _dataSourcel);
		} catch (SQLException | NamingException e) {
			_log.error("Произошла ошибка при создании/получении DataSource, для PoolConnection", e);
			throw new RuntimeException(e);
		}
		_log.info("Успешно проинициализирован метод init класса FirstFilter");
	}

	// TODO: Вынести константы пока в Interface, потом сделать чтение из конфига
	// или properties
	
	//TODO: Продумать про админку. Возмжно будет работа с отдельным фильтром/сервлетом минуя текущие-основные.
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		_log.info("-----DoFilter of Filter2");
		WorkerCookies workerCookies = new WorkerCookies();
		String mainParam = request.getParameter("auth");
		if (mainParam != null) {
			if (mainParam.equals("Authorization")) {
				String email = request.getParameter("LoginMemeber");
				String password = request.getParameter("PasswordMember");
				if (authorization.authorizedInSystem(email, password)) {
					workerCookies.setCookies((HttpServletResponse) response, email);
					chain.doFilter(request, response);
				} else {
					RequestDispatcher requestDispatcher = request.getRequestDispatcher("/html/errorAuthPage.html");
					requestDispatcher.forward(request, response);
				}
			} else if (mainParam.equals("Registration")) {
				String email = request.getParameter("LoginMemeber");
				String password = request.getParameter("PasswordMember");
				String name = request.getParameter("NameMember");
				if (authorization.registrate(email, password, name)) {
					authorization.authorizedInSystem(email, password);
					workerCookies.setCookies((HttpServletResponse) response, email);
					chain.doFilter(request, response);
				} else {
					RequestDispatcher requestDispatcher = request.getRequestDispatcher("/html/errorRegPage.html");
					requestDispatcher.forward(request, response);
				}
			} else if (mainParam.equals("Message")) {
				String email = request.getParameter("LoginMemeber");
				if (authorization.isAuthorizedUser(email)) {
					chain.doFilter(request, response);
				}
				RequestDispatcher requestDispatcher = request.getRequestDispatcher("/loginPage.html");
				requestDispatcher.forward(request, response);
			}
		} else {
			RequestDispatcher requestDispatcher = request.getRequestDispatcher("/err404.html");
			requestDispatcher.forward(request, response);
		}
	}

	@Override
	public void destroy() {
		_log.info("-----Destroy of Filter2");
	}

}
