package ru.andreyksu.annikonenkov.webapp.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainServlet extends HttpServlet {

	private static final long serialVersionUID = -5373004063297662448L;

	private static final Logger ___log = LogManager.getLogger(MainServlet.class);

	private static final String _message = "Message";

	@Override
	public void init() throws ServletException {
		___log.info("___MainServlet___Init");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String mainParam = request.getParameter("mode");
		if (mainParam != null && mainParam.equals(_message)) {
			HttpServletResponse tmp = (HttpServletResponse) response;
			tmp.sendRedirect("/ChatOnServlet/html/messagePage.html");
		}

		HttpServletResponse tmp = (HttpServletResponse) response;
		tmp.sendRedirect("/ChatOnServlet/html/messagePage.html");

		// RequestDispatcher requestDispatcher =
		// request.getRequestDispatcher("/html/messagePage.html");
		// requestDispatcher.forward(request, response);

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
