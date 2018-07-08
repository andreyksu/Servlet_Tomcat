package ru.andreyksu.annikonenkov.webapp.servlets;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.andreyksu.annikonenkov.webapp.messages.IMessage;
import ru.andreyksu.annikonenkov.webapp.messages.Message;
import ru.andreyksu.annikonenkov.webapp.postgressql.DataSourceProvider;

public class MainServlet extends HttpServlet {

	private static final long serialVersionUID = -5373004063297662448L;

	private static final Logger ___log = LogManager.getLogger(MainServlet.class);

	private static final String _message = "Message";

	private static DataSource _dataSource;

	@Override
	public void init() throws ServletException {
		___log.info("___MainServlet___Init");
		try {
			DataSourceProvider dataSP = DataSourceProvider.getSQLDataSource();
			_dataSource = dataSP.getDataSource();
		} catch (SQLException | NamingException e) {
			___log.error("Произошла ошибка при создании/получении DataSource, для PoolConnection", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String mainParam = request.getParameter("mode");
		if (mainParam != null && mainParam.equals(_message)) {
			___log.debug("___MainServlet___  mode = Message. Т.е. сюда пришли со странички сообщений!");
			String userListParam = request.getParameter("ListUser");
			if (userListParam != null && userListParam.equals("active")) {
				
			}
			IMessage message = new Message(_dataSource);
			HttpServletResponse tmp = (HttpServletResponse) response;
			String author = request.getParameter("author");
			String recipient = request.getParameter("recipient");
			String messageTxt = request.getParameter("message");
			___log.debug("author = %s, recipient = %s, messageTxt = %s", author, recipient, messageTxt);
			
		} else {
			___log.debug("___MainServlet___  mode != Message. Т.е. сюда пришли после авторизациии или регистрации!");
			HttpServletResponse tmp = (HttpServletResponse) response;
			tmp.sendRedirect("/ChatOnServlet/html/messagePage.html");
			/*
			 * RequestDispatcher requestDispatcher =
			 * request.getRequestDispatcher("/html/messagePage.html");
			 * requestDispatcher.forward(request, response);
			 */
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		___log.info("___MainServlet___DoPost");
	}

	@Override
	public void destroy() {
		___log.info("___MainServlet___Destroy");
	}

}
