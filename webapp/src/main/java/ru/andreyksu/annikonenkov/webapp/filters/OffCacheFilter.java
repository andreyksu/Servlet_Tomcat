package ru.andreyksu.annikonenkov.webapp.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OffCacheFilter implements Filter {

	private static final Logger ___log = LogManager.getLogger(OffCacheFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletResponse httpresp = (HttpServletResponse) response;
		___log.debug("___OffCacheFilter____Перед добавлением Header для отключения кэша страниц");
		httpresp.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
		httpresp.setHeader("Cache-Control", "no-cache, must-revalidate");
		httpresp.setHeader("Cache-Control", "post-check=0,pre-check=0");
		httpresp.setHeader("Cache-Control", "max-age=0");
		httpresp.setHeader("Pragma", "no-cache");
		___log.debug("___OffCacheFilter____Перед doFilter");
		chain.doFilter(request, httpresp);
		___log.debug("___OffCacheFilter____Перед возвратом");
	}

	@Override
	public void destroy() {
		___log.debug("___OffCacheFilter___Destroy");
	}

}
