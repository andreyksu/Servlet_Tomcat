package ru.andreyksu.annikonenkov.webapp.servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainServlet extends HttpServlet {

	private static final long serialVersionUID = -5373004063297662448L;

	private static final Logger ___log = LogManager.getLogger(MainServlet.class);

	/**
	 * Обрабатывает два исключения, выводит информацию и оборачивает в
	 * RunTimeException. По хорошему нужно было бы обрабоать в исходном классе,
	 * но не понятно что делать на том уровне, по Эккелю в таком случае лучше
	 * бросить вверх Да и смысла дальше работать если не получили DataSource.
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws ServletException {
		___log.info("___MainServlet___Init");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		___log.info("___MainServlet___ выполняем редирект в MainServlet");
		RequestDispatcher requestDispatcher = request.getRequestDispatcher("/html/messagePage.html");
		requestDispatcher.forward(request, response);
		___log.info("___MainServlet___ после выполнения MainServlet");
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
