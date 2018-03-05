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

	private static final Logger _log = LogManager.getLogger(MainServlet.class);

	/**
	 * Обрабатывает два исключения, выводит информацию и оборачивает в
	 * RunTimeException. По хорошему нужно было бы обрабоать в исходном классе,
	 * но не понятно что делать на том уровне, по Эккелю в таком случае лучше
	 * бросить вверх Да и смысла дальше работать если не получили DataSource.
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws ServletException {
		_log.info("-------Init_MainServlet");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		_log.info("-----DoFirst of MainServlet");
		RequestDispatcher requestDispatcher = request.getRequestDispatcher("/html/messagePage.html");
		requestDispatcher.forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		_log.info("-------DoPost_MainServlet");
	}

	@Override
	public void destroy() {
		_log.info("-------Destroy_MainServlet");
	}

}
