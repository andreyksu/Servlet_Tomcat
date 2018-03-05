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
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.andreyksu.annikonenkov.webapp.postgressql.SQLDataSourcePoolConnection;
import ru.andreyksu.annikonenkov.webapp.userrepresent.IUser;
import ru.andreyksu.annikonenkov.webapp.userrepresent.User;

public class FirstFilter implements Filter {

	// Планировал как проверку cookie, работа с авторизацией и регистрацией.
	// Возможно это будет сделано по шагам и на втором filter
	// Если уловие выполняется т.е. куки валидные то через RequestDispatcher
	// идем на основной servler и понеслось в ином случае идем дальше по фильтру
	// (регистрация или авторизация)

	private static final Logger _log = LogManager.getLogger(FirstFilter.class);

	private static DataSource _dataSourcel;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		_log.info("-----Init of Filter1");
		try {
			_dataSourcel = SQLDataSourcePoolConnection.getSQLDataSource().getDataSource();
		} catch (SQLException | NamingException e) {
			_log.error("Перехватили в init");
			throw new RuntimeException(e);
		}
		_log.info("Успешно проинициализирован метод init класса FirstFilter");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		_log.info("-----DoFilter of Filter1");
		IUser user = new User(_log, _dataSourcel);
		String mainParam = request.getParameter("auth");
		if (mainParam != null && mainParam.equals("Authorization")) {
			String email = request.getParameter("LoginMemeber");
			String password = request.getParameter("PasswordMember");
			if (user.authorizedInSystem(email, password)) {
				chain.doFilter(request, response);
			} else {
				RequestDispatcher requestDispatcher = request.getRequestDispatcher("/html/errorAuthPage.html");
				requestDispatcher.forward(request, response);
			}
		} else if (mainParam != null && mainParam.equals("Registration")) {
			String email = request.getParameter("LoginMemeber");
			String password = request.getParameter("PasswordMember");
			String name = request.getParameter("NameMember");
			if (user.registrateUser(email, password, name)) {
				user.authorizedInSystem(email, password);
				chain.doFilter(request, response);
			} else {
				RequestDispatcher requestDispatcher = request.getRequestDispatcher("/html/errorRegPage.html");
				requestDispatcher.forward(request, response);
			}
		} else {
			RequestDispatcher requestDispatcher = request.getRequestDispatcher("/html/DebugPage.html");
			requestDispatcher.forward(request, response);
		}
	}

	@Override
	public void destroy() {
		_log.info("-----Destroy of Filter1");
	}

}
