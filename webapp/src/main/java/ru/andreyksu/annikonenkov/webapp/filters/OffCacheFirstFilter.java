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

/**
 * Первый фильтр в цепочке. Только отключает кэш, для избежания моментов, когда
 * часть страницы не обновляется. Проявлялось при тестировании (при изменении
 * страницы или скриптов, не подтягивались изменения, а брались из кэша).
 * Редиректит на следующий фильтр, так как цель только отключить кэш. Видимо
 * выполняется каждый раз, даже при получении js, css для страницы. FirstFilter
 * не выполняется для каждого фафйла так как натравлена только на MainServlet
 */
public class OffCacheFirstFilter implements Filter {

    private static Logger _log = LogManager.getLogger(OffCacheFirstFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        _log.info("Method Init in class - OffCacheFirstFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpresp = (HttpServletResponse) response;
        _log.debug("Перед добавлением Header для отключения кэша страниц");
        httpresp.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
        httpresp.setHeader("Cache-Control", "no-cache, must-revalidate");
        httpresp.setHeader("Cache-Control", "post-check=0,pre-check=0");
        httpresp.setHeader("Cache-Control", "max-age=0");
        httpresp.setHeader("Pragma", "no-cache");
        _log.debug("Перед doFilter");
        chain.doFilter(request, httpresp);
        _log.debug("Перед возвратом");
    }

    @Override
    public void destroy() {
        _log.debug("Destroy OffCacheFirstFilter");
        _log = null;

    }

}
