package ru.andreyksu.annikonenkov.webapp.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FirstFilter implements Filter {

	private static final Logger _log = LogManager.getLogger(FirstFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		_log.info("-----Init of Filter1");
	}

	// TODO: Здесь планировал сделать работу с Cookie - т.е. минуя второй фильтр
	// если есть определенный параметр в Cookie. Пока сделал через второй
	// фильтр, в том числе если даже есть cookie. Дело в том, что нужно
	// продумать, как прокидывать контейнер с авторизованными пользователями.
	// Можно через контекст. Но чуть позже.
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		_log.info("-----DoFilter of Filter1");
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		_log.info("-----Destroy of Filter1");
	}

}
